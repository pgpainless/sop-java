// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine
import picocli.CommandLine.Command
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException

@Command(
    name = "merge-certs",
    resourceBundle = "msg_merge-certs",
    exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
class MergeCertsCmd : AbstractSopCmd() {

    @CommandLine.Option(names = [OPT_NO_ARMOR], negatable = true) var armor = true

    @CommandLine.Parameters(paramLabel = "CERTS") var updates: List<String> = listOf()

    override fun run() {
        val mergeCerts = throwIfUnsupportedSubcommand(SopCLI.getSop().mergeCerts(), "merge-certs")

        if (!armor) {
            throwIfUnsupportedOption(OPT_NO_ARMOR) { mergeCerts.noArmor() }
        }

        for (certFileName in updates) {
            try {
                getInput(certFileName).use { mergeCerts.updates(it) }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        try {
            val ready = mergeCerts.baseCertificates(System.`in`)
            ready.writeTo(System.out)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        const val OPT_NO_ARMOR = "--no-armor"
    }
}
