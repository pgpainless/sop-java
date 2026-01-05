// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import sop.EncryptionResult
import sop.ReadyWithResult
import sop.SessionKey.Companion.fromString
import sop.enums.EncryptAs
import sop.enums.EncryptFor
import sop.external.ExternalSOP
import sop.external.ExternalSOP.Companion.finish
import sop.external.ExternalSOP.Companion.readString
import sop.operation.Encrypt

/** Implementation of the [Encrypt] operation using an external SOP binary. */
class EncryptExternal(
    binary: String,
    environment: Properties,
    private val tempDirProvider: ExternalSOP.TempDirProvider
) : Encrypt {

    private val commandList = mutableListOf(binary, "encrypt")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var argCounter = 0

    override fun noArmor(): Encrypt = apply { commandList.add("--no-armor") }

    override fun mode(mode: EncryptAs): Encrypt = apply { commandList.add("--as=$mode") }

    override fun encryptFor(purpose: EncryptFor): Encrypt = apply {
        commandList.add("--for=$purpose")
    }

    override fun signWith(key: InputStream): Encrypt = apply {
        commandList.add("--sign-with=@ENV:SIGN_WITH_$argCounter")
        envList.add("SIGN_WITH_$argCounter=${readString(key)}")
        argCounter += 1
    }

    override fun withKeyPassword(password: ByteArray): Encrypt = apply {
        commandList.add("--with-key-password=@ENV:KEY_PASSWORD_$argCounter")
        envList.add("KEY_PASSWORD_$argCounter=${String(password)}")
        argCounter += 1
    }

    override fun withPassword(password: String): Encrypt = apply {
        commandList.add("--with-password=@ENV:PASSWORD_$argCounter")
        envList.add("PASSWORD_$argCounter=$password")
        argCounter += 1
    }

    override fun withCert(cert: InputStream): Encrypt = apply {
        commandList.add("@ENV:CERT_$argCounter")
        envList.add("CERT_$argCounter=${readString(cert)}")
        argCounter += 1
    }

    override fun profile(profileName: String): Encrypt = apply {
        commandList.add("--profile=$profileName")
    }

    override fun plaintext(plaintext: InputStream): ReadyWithResult<EncryptionResult> {
        val tempDir = tempDirProvider.provideTempDirectory()

        val sessionKeyOut = File(tempDir, "session-key-out")
        sessionKeyOut.delete()
        commandList.add("--session-key-out=${sessionKeyOut.absolutePath}")
        try {
            val process =
                Runtime.getRuntime().exec(commandList.toTypedArray(), envList.toTypedArray())
            val processOut = process.outputStream
            val processIn = process.inputStream

            return object : ReadyWithResult<EncryptionResult>() {
                override fun writeTo(outputStream: OutputStream): EncryptionResult {
                    val buf = ByteArray(4096)
                    var r: Int
                    while (plaintext.read(buf).also { r = it } > 0) {
                        processOut.write(buf, 0, r)
                    }

                    plaintext.close()
                    processOut.close()

                    while (processIn.read(buf).also { r = it } > 0) {
                        outputStream.write(buf, 0, r)
                    }

                    processIn.close()
                    outputStream.close()

                    finish(process)

                    val sessionKeyOutIn = FileInputStream(sessionKeyOut)
                    val line = readString(sessionKeyOutIn)
                    val sessionKey = fromString(line.trim())
                    sessionKeyOutIn.close()
                    sessionKeyOut.delete()

                    return EncryptionResult(sessionKey)
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
