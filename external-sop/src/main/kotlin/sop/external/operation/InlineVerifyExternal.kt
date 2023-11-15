// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.*
import java.util.*
import sop.ReadyWithResult
import sop.Verification
import sop.Verification.Companion.fromString
import sop.external.ExternalSOP
import sop.external.ExternalSOP.Companion.finish
import sop.operation.InlineVerify
import sop.util.UTCUtil

/** Implementation of the [InlineVerify] operation using an external SOP binary. */
class InlineVerifyExternal(
    binary: String,
    environment: Properties,
    private val tempDirProvider: ExternalSOP.TempDirProvider
) : InlineVerify {

    private val commandList = mutableListOf(binary, "inline-verify")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var argCounter = 0

    override fun data(data: InputStream): ReadyWithResult<List<Verification>> {
        val tempDir = tempDirProvider.provideTempDirectory()

        val verificationsOut = File(tempDir, "verifications-out")
        verificationsOut.delete()
        commandList.add("--verifications-out=${verificationsOut.absolutePath}")

        try {
            val process =
                Runtime.getRuntime().exec(commandList.toTypedArray(), envList.toTypedArray())
            val processOut = process.outputStream
            val processIn = process.inputStream

            return object : ReadyWithResult<List<Verification>>() {
                override fun writeTo(outputStream: OutputStream): List<Verification> {
                    val buf = ByteArray(4096)
                    var r: Int
                    while (data.read(buf).also { r = it } > 0) {
                        processOut.write(buf, 0, r)
                    }

                    data.close()
                    processOut.close()

                    while (processIn.read(buf).also { r = it } > 0) {
                        outputStream.write(buf, 0, r)
                    }

                    processIn.close()
                    outputStream.close()

                    finish(process)

                    val verificationsOutIn = FileInputStream(verificationsOut)
                    val reader = BufferedReader(InputStreamReader(verificationsOutIn))
                    val verificationList: MutableList<Verification> = mutableListOf()
                    var line: String
                    while (reader.readLine().also { line = it } != null) {
                        verificationList.add(fromString(line.trim()))
                    }

                    return verificationList
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun notBefore(timestamp: Date): InlineVerify = apply {
        commandList.add("--not-before=${UTCUtil.formatUTCDate(timestamp)}")
    }

    override fun notAfter(timestamp: Date): InlineVerify = apply {
        commandList.add("--not-after=${UTCUtil.formatUTCDate(timestamp)}")
    }

    override fun cert(cert: InputStream): InlineVerify = apply {
        commandList.add("@ENV:CERT_$argCounter")
        envList.add("CERT_$argCounter=${ExternalSOP.readString(cert)}")
        argCounter += 1
    }
}
