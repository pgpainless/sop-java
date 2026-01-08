// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import java.io.PrintWriter
import picocli.CommandLine.*
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException.*

@Command(
    name = "inline-verify",
    resourceBundle = "msg_inline-verify",
    exitCodeOnInvalidInput = UnsupportedOption.EXIT_CODE)
class InlineVerifyCmd : AbstractSopCmd() {

    @Parameters(arity = "0..*", paramLabel = "CERT") var certificates: List<String> = listOf()

    @Option(names = [OPT_NOT_BEFORE], paramLabel = "DATE") var notBefore: String = "-"

    @Option(names = [OPT_NOT_AFTER], paramLabel = "DATE") var notAfter: String = "now"

    @Option(names = [OPT_VERIFICATIONS_OUT], paramLabel = "VERIFICATIONS")
    var verificationsOut: String? = null

    override fun run() {
        val inlineVerify =
            throwIfUnsupportedSubcommand(SopCLI.getSop().inlineVerify(), "inline-verify")

        throwIfOutputExists(verificationsOut)

        throwIfUnsupportedOption(OPT_NOT_AFTER) { inlineVerify.notAfter(parseNotAfter(notAfter)) }
        throwIfUnsupportedOption(OPT_NOT_BEFORE) {
            inlineVerify.notBefore(parseNotBefore(notBefore))
        }

        for (certInput in certificates) {
            try {
                getInput(certInput).use { certIn -> inlineVerify.cert(certIn) }
            } catch (ioException: IOException) {
                throw RuntimeException(ioException)
            } catch (unsupportedAsymmetricAlgo: UnsupportedAsymmetricAlgo) {
                val errorMsg =
                    getMsg(
                        "sop.error.runtime.cert_uses_unsupported_asymmetric_algorithm", certInput)
                throw UnsupportedAsymmetricAlgo(errorMsg, unsupportedAsymmetricAlgo)
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.not_a_certificate", certInput)
                throw BadData(errorMsg, badData)
            }
        }

        val verifications =
            try {
                val ready = inlineVerify.data(System.`in`)
                ready.writeTo(System.out)
            } catch (e: NoSignature) {
                val errorMsg = getMsg("sop.error.runtime.no_verifiable_signature_found")
                throw NoSignature(errorMsg, e)
            } catch (ioException: IOException) {
                throw RuntimeException(ioException)
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.stdin_not_a_message")
                throw BadData(errorMsg, badData)
            }

        verificationsOut?.let {
            try {
                getOutput(it).use { outputStream ->
                    val pw = PrintWriter(outputStream)
                    for (verification in verifications) {
                        pw.println(verification)
                    }
                    pw.flush()
                    pw.close()
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    companion object {
        const val OPT_NOT_BEFORE = "--not-before"
        const val OPT_NOT_AFTER = "--not-after"
        const val OPT_VERIFICATIONS_OUT = "--verifications-out"
    }
}
