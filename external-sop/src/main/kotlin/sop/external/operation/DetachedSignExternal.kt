// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.*
import java.util.*
import sop.MicAlg
import sop.ReadyWithResult
import sop.SigningResult
import sop.SigningResult.Companion.builder
import sop.enums.SignAs
import sop.external.ExternalSOP
import sop.external.ExternalSOP.Companion.finish
import sop.operation.DetachedSign

/** Implementation of the [DetachedSign] operation using an external SOP binary. */
class DetachedSignExternal(
    binary: String,
    environment: Properties,
    private val tempDirProvider: ExternalSOP.TempDirProvider
) : DetachedSign {

    private val commandList = mutableListOf(binary, "sign")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var argCounter = 0

    override fun mode(mode: SignAs): DetachedSign = apply { commandList.add("--as=$mode") }

    override fun data(data: InputStream): ReadyWithResult<SigningResult> {
        val tempDir = tempDirProvider.provideTempDirectory()
        val micAlgOut = File(tempDir, "micAlgOut")
        micAlgOut.delete()
        commandList.add("--micalg-out=${micAlgOut.absolutePath}")

        try {
            val process =
                Runtime.getRuntime().exec(commandList.toTypedArray(), envList.toTypedArray())
            val processOut = process.outputStream
            val processIn = process.inputStream

            return object : ReadyWithResult<SigningResult>() {
                override fun writeTo(outputStream: OutputStream): SigningResult {
                    val buf = ByteArray(4096)
                    var r: Int
                    while (data.read(buf).also { r = it } > 0) {
                        processOut.write(buf, 0, r)
                    }

                    data.close()
                    try {
                        processOut.close()
                    } catch (e: IOException) {
                        // Ignore Stream closed
                        if ("Stream closed" != e.message) {
                            throw e
                        }
                    }

                    while (processIn.read(buf).also { r = it } > 0) {
                        outputStream.write(buf, 0, r)
                    }

                    processIn.close()
                    outputStream.close()

                    finish(process)

                    val builder = builder()
                    if (micAlgOut.exists()) {
                        val reader = BufferedReader(InputStreamReader(FileInputStream(micAlgOut)))
                        val line = reader.readLine()
                        if (line != null && line.isNotBlank()) {
                            val micAlg = MicAlg(line.trim())
                            builder.setMicAlg(micAlg)
                        }
                        reader.close()
                        micAlgOut.delete()
                    }

                    return builder.build()
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun noArmor(): DetachedSign = apply { commandList.add("--no-armor") }

    override fun key(key: InputStream): DetachedSign = apply {
        commandList.add("@ENV:KEY_$argCounter")
        envList.add("KEY_$argCounter=${ExternalSOP.readString(key)}")
        argCounter += 1
    }

    override fun withKeyPassword(password: ByteArray): DetachedSign = apply {
        commandList.add("--with-key-password=@ENV:WITH_KEY_PASSWORD_$argCounter")
        envList.add("WITH_KEY_PASSWORD_$argCounter=${String(password)}")
        argCounter += 1
    }
}
