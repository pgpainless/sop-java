// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.*
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException.*

@Command(
    name = "verify",
    resourceBundle = "msg_detached-verify",
    exitCodeOnInvalidInput = UnsupportedOption.EXIT_CODE)
class VerifyCmd : AbstractSopCmd() {

    @Parameters(index = "0", paramLabel = "SIGNATURE") lateinit var signature: String

    @Parameters(index = "1..*", arity = "1..*", paramLabel = "CERT")
    lateinit var certificates: List<String>

    @Option(names = ["--not-before"], paramLabel = "DATE") var notBefore: String = "-"

    @Option(names = ["--not-after"], paramLabel = "DATE") var notAfter: String = "now"

    override fun run() {
        val detachedVerify =
            throwIfUnsupportedSubcommand(SopCLI.getSop().detachedVerify(), "verify")
        try {
            detachedVerify.notAfter(parseNotAfter(notAfter))
        } catch (unsupportedOption: UnsupportedOption) {
            val errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--not-after")
            throw UnsupportedOption(errorMsg, unsupportedOption)
        }

        try {
            detachedVerify.notBefore(parseNotBefore(notBefore))
        } catch (unsupportedOption: UnsupportedOption) {
            val errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--not-before")
            throw UnsupportedOption(errorMsg, unsupportedOption)
        }

        for (certInput in certificates) {
            try {
                getInput(certInput).use { certIn -> detachedVerify.cert(certIn) }
            } catch (ioException: IOException) {
                throw RuntimeException(ioException)
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.not_a_certificate", certInput)
                throw BadData(errorMsg, badData)
            }
        }

        try {
            getInput(signature).use { sigIn -> detachedVerify.signatures(sigIn) }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (badData: BadData) {
            val errorMsg = getMsg("sop.error.input.not_a_signature", signature)
            throw BadData(errorMsg, badData)
        }

        val verifications =
            try {
                detachedVerify.data(System.`in`)
            } catch (e: NoSignature) {
                val errorMsg = getMsg("sop.error.runtime.no_verifiable_signature_found")
                throw NoSignature(errorMsg, e)
            } catch (ioException: IOException) {
                throw RuntimeException(ioException)
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.stdin_not_a_message")
                throw BadData(errorMsg, badData)
            }

        for (verification in verifications) {
            println(verification.toString())
        }
    }
}
