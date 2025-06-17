// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException.*

@Command(
    name = "update-key",
    resourceBundle = "msg_update-key",
    exitCodeOnInvalidInput = UnsupportedOption.EXIT_CODE)
class UpdateKeyCmd : AbstractSopCmd() {

    @Option(names = ["--no-armor"], negatable = true) var armor = true

    @Option(names = ["--signing-only"]) var signingOnly = false

    @Option(names = ["--no-added-capabilities"]) var noAddedCapabilities = false

    @Option(names = ["--with-key-password"], paramLabel = "PASSWORD")
    var withKeyPassword: List<String> = listOf()

    @Option(names = ["--merge-certs"], paramLabel = "CERTS") var mergeCerts: List<String> = listOf()

    override fun run() {
        val updateKey = throwIfUnsupportedSubcommand(SopCLI.getSop().updateKey(), "update-key")

        if (!armor) {
            updateKey.noArmor()
        }

        if (signingOnly) {
            updateKey.signingOnly()
        }

        if (noAddedCapabilities) {
            updateKey.noAddedCapabilities()
        }

        for (passwordFileName in withKeyPassword) {
            try {
                val password = stringFromInputStream(getInput(passwordFileName))
                updateKey.withKeyPassword(password)
            } catch (unsupportedOption: UnsupportedOption) {
                val errorMsg =
                    getMsg("sop.error.feature_support.option_not_supported", "--with-key-password")
                throw UnsupportedOption(errorMsg, unsupportedOption)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        for (certInput in mergeCerts) {
            try {
                getInput(certInput).use { updateKey.mergeCerts(it) }
            } catch (e: IOException) {
                throw RuntimeException(e)
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.not_a_certificate", certInput)
                throw BadData(errorMsg, badData)
            }
        }

        try {
            val ready = updateKey.key(System.`in`)
            ready.writeTo(System.out)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (badData: BadData) {
            val errorMsg = getMsg("sop.error.input.not_a_private_key", "STDIN")
            throw BadData(errorMsg, badData)
        }
    }
}
