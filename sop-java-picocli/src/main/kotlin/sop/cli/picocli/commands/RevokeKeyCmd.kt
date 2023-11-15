// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException
import sop.exception.SOPGPException.KeyIsProtected

@Command(
    name = "revoke-key",
    resourceBundle = "msg_revoke-key",
    exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
class RevokeKeyCmd : AbstractSopCmd() {

    @Option(names = ["--no-armor"], negatable = true) var armor = true

    @Option(names = ["--with-key-password"], paramLabel = "PASSWORD")
    var withKeyPassword: String? = null

    override fun run() {
        val revokeKey = throwIfUnsupportedSubcommand(SopCLI.getSop().revokeKey(), "revoke-key")

        if (!armor) {
            revokeKey.noArmor()
        }

        withKeyPassword?.let {
            try {
                val password = stringFromInputStream(getInput(it))
                revokeKey.withKeyPassword(password)
            } catch (e: SOPGPException.UnsupportedOption) {
                val errorMsg =
                    getMsg("sop.error.feature_support.option_not_supported", "--with-key-password")
                throw SOPGPException.UnsupportedOption(errorMsg, e)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        val ready =
            try {
                revokeKey.keys(System.`in`)
            } catch (e: KeyIsProtected) {
                val errorMsg = getMsg("sop.error.runtime.cannot_unlock_key", "STANDARD_IN")
                throw KeyIsProtected(errorMsg, e)
            }
        try {
            ready.writeTo(System.out)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
