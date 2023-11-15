// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.*
import java.util.*
import sop.ReadyWithResult
import sop.Signatures
import sop.external.ExternalSOP
import sop.external.ExternalSOP.Companion.finish
import sop.operation.InlineDetach

/** Implementation of the [InlineDetach] operation using an external SOP binary. */
class InlineDetachExternal(
    binary: String,
    environment: Properties,
    private val tempDirProvider: ExternalSOP.TempDirProvider
) : InlineDetach {

    private val commandList = mutableListOf(binary, "inline-detach")
    private val envList = ExternalSOP.propertiesToEnv(environment)

    override fun noArmor(): InlineDetach = apply { commandList.add("--no-armor") }

    override fun message(messageInputStream: InputStream): ReadyWithResult<Signatures> {
        val tempDir = tempDirProvider.provideTempDirectory()

        val signaturesOut = File(tempDir, "signatures")
        signaturesOut.delete()
        commandList.add("--signatures-out=${signaturesOut.absolutePath}")

        try {
            val process =
                Runtime.getRuntime().exec(commandList.toTypedArray(), envList.toTypedArray())
            val processOut = process.outputStream
            val processIn = process.inputStream

            return object : ReadyWithResult<Signatures>() {
                override fun writeTo(outputStream: OutputStream): Signatures {
                    val buf = ByteArray(4096)
                    var r: Int
                    while (messageInputStream.read(buf).also { r = it } > 0) {
                        processOut.write(buf, 0, r)
                    }

                    messageInputStream.close()
                    processOut.close()

                    while (processIn.read(buf).also { r = it } > 0) {
                        outputStream.write(buf, 0, r)
                    }

                    processIn.close()
                    outputStream.close()

                    finish(process)

                    val signaturesOutIn = FileInputStream(signaturesOut)
                    val signaturesBuffer = ByteArrayOutputStream()
                    while (signaturesOutIn.read(buf).also { r = it } > 0) {
                        signaturesBuffer.write(buf, 0, r)
                    }
                    signaturesOutIn.close()
                    signaturesOut.delete()

                    val sigBytes = signaturesBuffer.toByteArray()

                    return object : Signatures() {
                        @Throws(IOException::class)
                        override fun writeTo(outputStream: OutputStream) {
                            outputStream.write(sigBytes)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
