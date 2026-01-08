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

    @Option(names = [OPT_NO_ARMOR], negatable = true) var armor = true

    @Option(names = [OPT_USERID], required = true, arity = "1..*", paramLabel = "USERID")
    var userIds: List<String> = listOf()

    @Option(names = [OPT_WITH_KEY_PASSWORD], paramLabel = "PASSWORD")
    var withKeyPassword: List<String> = listOf()

    @Option(names = [OPT_NO_REQUIRE_SELF_SIG]) var noRequireSelfSig = false

    @Parameters(paramLabel = "KEYS", arity = "1..*") var keys: List<String> = listOf()

    override fun run() {
        val certifyUserId =
            throwIfUnsupportedSubcommand(SopCLI.getSop().certifyUserId(), "certify-userid")

        if (!armor) {
            throwIfUnsupportedOption(OPT_NO_ARMOR) { certifyUserId.noArmor() }
        }

        if (noRequireSelfSig) {
            throwIfUnsupportedOption(OPT_NO_REQUIRE_SELF_SIG) { certifyUserId.noRequireSelfSig() }
        }

        for (userId in userIds) {
            throwIfUnsupportedOption(OPT_USERID) { certifyUserId.userId(userId) }
        }

        for (passwordFileName in withKeyPassword) {
            try {
                throwIfUnsupportedOption(OPT_WITH_KEY_PASSWORD) {
                    val password = stringFromInputStream(getInput(passwordFileName))
                    certifyUserId.withKeyPassword(password)
                }
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

    companion object {
        const val OPT_NO_ARMOR = "--no-armor"
        const val OPT_USERID = "--userid"
        const val OPT_WITH_KEY_PASSWORD = "--with-key-password"
        const val OPT_NO_REQUIRE_SELF_SIG = "--no-require-self-sig"
    }
}
