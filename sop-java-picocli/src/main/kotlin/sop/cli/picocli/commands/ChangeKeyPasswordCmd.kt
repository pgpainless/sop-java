// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import java.lang.RuntimeException
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException

@Command(
    name = "change-key-password",
    resourceBundle = "msg_change-key-password",
    exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
class ChangeKeyPasswordCmd : AbstractSopCmd() {

    @Option(names = [OPT_NO_ARMOR], negatable = true) var armor: Boolean = true

    @Option(names = [OPT_OLD_KEY_PASSWORD], paramLabel = "PASSWORD")
    var oldKeyPasswords: List<String> = listOf()

    @Option(names = [OPT_NEW_KEY_PASSWORD], arity = "0..1", paramLabel = "PASSWORD")
    var newKeyPassword: String? = null

    override fun run() {
        val changeKeyPassword =
            throwIfUnsupportedSubcommand(SopCLI.getSop().changeKeyPassword(), "change-key-password")

        if (!armor) {
            throwIfUnsupportedOption(OPT_NO_ARMOR) { changeKeyPassword.noArmor() }
        }

        oldKeyPasswords.forEach {
            val password = stringFromInputStream(getInput(it))
            changeKeyPassword.oldKeyPassphrase(password)
        }

        newKeyPassword?.let {
            val password = stringFromInputStream(getInput(it))
            changeKeyPassword.newKeyPassphrase(password)
        }

        try {
            changeKeyPassword.keys(System.`in`).writeTo(System.out)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        const val OPT_NO_ARMOR = "--no-armor"
        const val OPT_OLD_KEY_PASSWORD = "--old-key-password"
        const val OPT_NEW_KEY_PASSWORD = "--new-key-password"
    }
}
