// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import sop.Verification
import sop.Verification.Companion.fromString
import sop.exception.SOPGPException
import sop.external.ExternalSOP
import sop.external.ExternalSOP.Companion.finish
import sop.operation.DetachedVerify
import sop.operation.VerifySignatures
import sop.util.UTCUtil

/** Implementation of the [DetachedVerify] operation using an external SOP binary. */
class DetachedVerifyExternal(binary: String, environment: Properties) : DetachedVerify {

    private val commandList = mutableListOf(binary, "verify")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var signatures: InputStream? = null
    private val certs: MutableSet<InputStream> = mutableSetOf()
    private var argCounter = 0

    override fun signatures(signatures: InputStream): VerifySignatures = apply {
        this.signatures = signatures
    }

    override fun notBefore(timestamp: Date): DetachedVerify = apply {
        commandList.add("--not-before=${UTCUtil.formatUTCDate(timestamp)}")
    }

    override fun notAfter(timestamp: Date): DetachedVerify = apply {
        commandList.add("--not-after=${UTCUtil.formatUTCDate(timestamp)}")
    }

    override fun cert(cert: InputStream): DetachedVerify = apply { this.certs.add(cert) }

    override fun data(data: InputStream): List<Verification> {
        // Signature
        if (signatures == null) {
            throw SOPGPException.MissingArg("Missing argument: signatures cannot be null.")
        }
        commandList.add("@ENV:SIGNATURE")
        envList.add("SIGNATURE=${ExternalSOP.readString(signatures!!)}")

        // Certs
        for (cert in certs) {
            commandList.add("@ENV:CERT_$argCounter")
            envList.add("CERT_$argCounter=${ExternalSOP.readString(cert)}")
            argCounter += 1
        }

        try {
            val process =
                Runtime.getRuntime().exec(commandList.toTypedArray(), envList.toTypedArray())
            val processOut = process.outputStream
            val processIn = process.inputStream

            val buf = ByteArray(4096)
            var r: Int
            while (data.read(buf).also { r = it } > 0) {
                processOut.write(buf, 0, r)
            }

            data.close()
            processOut.close()

            val bufferedReader = BufferedReader(InputStreamReader(processIn))
            val verifications: MutableList<Verification> = ArrayList()

            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                verifications.add(fromString(line!!))
            }

            finish(process)

            return verifications
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
