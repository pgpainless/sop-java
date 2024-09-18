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

    @CommandLine.Option(names = ["--no-armor"], negatable = true) var armor = false

    @CommandLine.Parameters(paramLabel = "CERTS") var updates: List<String> = listOf()

    override fun run() {
        val mergeCerts = throwIfUnsupportedSubcommand(SopCLI.getSop().mergeCerts(), "merge-certs")

        if (!armor) {
            mergeCerts.noArmor()
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
}
