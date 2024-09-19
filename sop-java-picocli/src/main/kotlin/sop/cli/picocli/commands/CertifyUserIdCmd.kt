// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException.BadData
import sop.exception.SOPGPException.UnsupportedOption

@Command(
    name = "certify-userid",
    resourceBundle = "msg_certify-userid",
    exitCodeOnInvalidInput = UnsupportedOption.EXIT_CODE,
    showEndOfOptionsDelimiterInUsageHelp = true)
class CertifyUserIdCmd : AbstractSopCmd() {

    @Option(names = ["--no-armor"], negatable = true) var armor = true

    @Option(names = ["--userid"], required = true, arity = "1..*", paramLabel = "USERID")
    var userIds: List<String> = listOf()

    @Option(names = ["--with-key-password"], paramLabel = "PASSWORD")
    var withKeyPassword: List<String> = listOf()

    @Option(names = ["--no-require-self-sig"]) var noRequireSelfSig = false

    @Parameters(paramLabel = "KEYS", arity = "1..*") var keys: List<String> = listOf()

    override fun run() {
        val certifyUserId =
            throwIfUnsupportedSubcommand(SopCLI.getSop().certifyUserId(), "certify-userid")

        if (!armor) {
            certifyUserId.noArmor()
        }

        if (noRequireSelfSig) {
            certifyUserId.noRequireSelfSig()
        }

        for (userId in userIds) {
            certifyUserId.userId(userId)
        }

        for (passwordFileName in withKeyPassword) {
            try {
                val password = stringFromInputStream(getInput(passwordFileName))
                certifyUserId.withKeyPassword(password)
            } catch (unsupportedOption: UnsupportedOption) {
                val errorMsg =
                    getMsg("sop.error.feature_support.option_not_supported", "--with-key-password")
                throw UnsupportedOption(errorMsg, unsupportedOption)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        for (keyInput in keys) {
            try {
                getInput(keyInput).use { certifyUserId.keys(it) }
            } catch (e: IOException) {
                throw RuntimeException(e)
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.not_a_private_key", keyInput)
                throw BadData(errorMsg, badData)
            }
        }

        try {
            val ready = certifyUserId.certs(System.`in`)
            ready.writeTo(System.out)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (badData: BadData) {
            val errorMsg = getMsg("sop.error.input.not_a_private_key", "STDIN")
            throw BadData(errorMsg, badData)
        }
    }
}
