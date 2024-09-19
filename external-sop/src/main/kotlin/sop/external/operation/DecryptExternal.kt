// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.*
import java.util.*
import sop.DecryptionResult
import sop.ReadyWithResult
import sop.SessionKey
import sop.Verification
import sop.external.ExternalSOP
import sop.external.ExternalSOP.Companion.finish
import sop.external.ExternalSOP.Companion.readString
import sop.operation.Decrypt
import sop.util.UTCUtil

/** Implementation of the [Decrypt] operation using an external SOP binary. */
class DecryptExternal(
    binary: String,
    environment: Properties,
    private val tempDirProvider: ExternalSOP.TempDirProvider
) : Decrypt {

    private val commandList = mutableListOf(binary, "decrypt")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var argCounter = 0
    private var requireVerification = false

    override fun verifyNotBefore(timestamp: Date): Decrypt = apply {
        commandList.add("--verify-not-before=${UTCUtil.formatUTCDate(timestamp)}")
    }

    override fun verifyNotAfter(timestamp: Date): Decrypt = apply {
        commandList.add("--verify-not-after=${UTCUtil.formatUTCDate(timestamp)}")
    }

    override fun verifyWithCert(cert: InputStream): Decrypt = apply {
        commandList.add("--verify-with=@ENV:VERIFY_WITH_$argCounter")
        envList.add("VERIFY_WITH_$argCounter=${readString(cert)}")
        argCounter += 1
        requireVerification = true
    }

    override fun withSessionKey(sessionKey: SessionKey): Decrypt = apply {
        commandList.add("--with-session-key=@ENV:SESSION_KEY_$argCounter")
        envList.add("SESSION_KEY_$argCounter=$sessionKey")
        argCounter += 1
    }

    override fun withPassword(password: String): Decrypt = apply {
        commandList.add("--with-password=@ENV:PASSWORD_$argCounter")
        envList.add("PASSWORD_$argCounter=$password")
        argCounter += 1
    }

    override fun withKey(key: InputStream): Decrypt = apply {
        commandList.add("@ENV:KEY_$argCounter")
        envList.add("KEY_$argCounter=${readString(key)}")
        argCounter += 1
    }

    override fun withKeyPassword(password: ByteArray): Decrypt = apply {
        commandList.add("--with-key-password=@ENV:KEY_PASSWORD_$argCounter")
        envList.add("KEY_PASSWORD_$argCounter=${String(password)}")
        argCounter += 1
    }

    override fun ciphertext(ciphertext: InputStream): ReadyWithResult<DecryptionResult> {
        val tempDir = tempDirProvider.provideTempDirectory()

        val sessionKeyOut = File(tempDir, "session-key-out")
        sessionKeyOut.delete()
        commandList.add("--session-key-out=${sessionKeyOut.absolutePath}")

        val verifyOut = File(tempDir, "verifications-out")
        verifyOut.delete()
        if (requireVerification) {
            commandList.add("--verify-out=${verifyOut.absolutePath}")
        }

        try {
            val process =
                Runtime.getRuntime().exec(commandList.toTypedArray(), envList.toTypedArray())
            val processOut = process.outputStream
            val processIn = process.inputStream

            return object : ReadyWithResult<DecryptionResult>() {
                override fun writeTo(outputStream: OutputStream): DecryptionResult {
                    val buf = ByteArray(4096)
                    var r: Int
                    while (ciphertext.read(buf).also { r = it } > 0) {
                        processOut.write(buf, 0, r)
                    }

                    ciphertext.close()
                    processOut.close()

                    while (processIn.read(buf).also { r = it } > 0) {
                        outputStream.write(buf, 0, r)
                    }

                    processIn.close()
                    outputStream.close()

                    finish(process)

                    val sessionKeyOutIn = FileInputStream(sessionKeyOut)
                    var line: String? = readString(sessionKeyOutIn)
                    val sessionKey = line?.let { l -> SessionKey.fromString(l.trim { it <= ' ' }) }
                    sessionKeyOutIn.close()
                    sessionKeyOut.delete()

                    val verifications: MutableList<Verification> = ArrayList()
                    if (requireVerification) {
                        val verifyOutIn = FileInputStream(verifyOut)
                        val reader = BufferedReader(InputStreamReader(verifyOutIn))
                        while (reader.readLine().also { line = it } != null) {
                            line?.let { verifications.add(Verification.fromString(it.trim())) }
                        }
                        reader.close()
                    }

                    return DecryptionResult(sessionKey, verifications)
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
