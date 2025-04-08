// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException
import sop.util.HexUtil.Companion.bytesToHex
import java.util.*

@Command(
    name = "validate-userid",
    resourceBundle = "msg_validate-userid",
    exitCodeOnInvalidInput = SOPGPException.MissingArg.EXIT_CODE,
    showEndOfOptionsDelimiterInUsageHelp = true)
class ValidateUserIdCmd : AbstractSopCmd() {

    @Option(names = ["--addr-spec-only"]) var addrSpecOnly: Boolean = false

    @Option(names = ["--validate-at"]) var validateAt: Date? = null

    @Parameters(index = "0", arity = "1", paramLabel = "USERID") lateinit var userId: String

    @Parameters(index = "1..*", arity = "1..*", paramLabel = "CERTS")
    var authorities: List<String> = listOf()

    override fun run() {
        val validateUserId =
            throwIfUnsupportedSubcommand(SopCLI.getSop().validateUserId(), "validate-userid")

        if (addrSpecOnly) {
            validateUserId.addrSpecOnly()
        }

        if (validateAt != null) {
            validateUserId.validateAt(validateAt!!)
        }

        validateUserId.userId(userId)

        for (authority in authorities) {
            try {
                getInput(authority).use { validateUserId.authorities(it) }
            } catch (e: IOException) {
                throw RuntimeException(e)
            } catch (b: SOPGPException.BadData) {
                val errorMsg = getMsg("sop.error.input.not_a_certificate", authority)
                throw SOPGPException.BadData(errorMsg, b)
            }
        }

        try {
            val valid = validateUserId.subjects(System.`in`)

            if (!valid) {
                val errorMsg = getMsg("sop.error.runtime.any_cert_user_id_no_match", userId)
                throw SOPGPException.CertUserIdNoMatch(errorMsg)
            }
        } catch (e: SOPGPException.CertUserIdNoMatch) {
            val errorMsg =
                if (e.fingerprint != null) {
                    getMsg(
                        "sop.error.runtime.cert_user_id_no_match",
                        bytesToHex(e.fingerprint!!),
                        userId)
                } else {
                    getMsg("sop.error.runtime.any_cert_user_id_no_match", userId)
                }
            throw SOPGPException.CertUserIdNoMatch(errorMsg, e)
        } catch (e: SOPGPException.BadData) {
            val errorMsg = getMsg("sop.error.input.not_a_certificate", "STDIN")
            throw SOPGPException.BadData(errorMsg, e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
