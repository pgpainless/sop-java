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

    @Option(names = ["--no-armor"], negatable = true) var armor: Boolean = true

    @Option(names = ["--old-key-password"], paramLabel = "PASSWORD")
    var oldKeyPasswords: List<String> = listOf()

    @Option(names = ["--new-key-password"], arity = "0..1", paramLabel = "PASSWORD")
    var newKeyPassword: String? = null

    override fun run() {
        val changeKeyPassword =
            throwIfUnsupportedSubcommand(SopCLI.getSop().changeKeyPassword(), "change-key-password")

        if (!armor) {
            changeKeyPassword.noArmor()
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
}
