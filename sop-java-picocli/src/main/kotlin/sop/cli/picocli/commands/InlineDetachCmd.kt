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
    name = "inline-detach",
    resourceBundle = "msg_inline-detach",
    exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
class InlineDetachCmd : AbstractSopCmd() {

    @Option(names = [OPT_SIGNATURES_OUT], paramLabel = "SIGNATURES")
    var signaturesOut: String? = null

    @Option(names = [OPT_NO_ARMOR], negatable = true) var armor: Boolean = true

    override fun run() {
        val inlineDetach =
            throwIfUnsupportedSubcommand(SopCLI.getSop().inlineDetach(), "inline-detach")

        throwIfOutputExists(signaturesOut)
        throwIfMissingArg(signaturesOut, OPT_SIGNATURES_OUT)

        if (!armor) {
            throwIfUnsupportedOption(OPT_NO_ARMOR) { inlineDetach.noArmor() }
        }

        try {
            getOutput(signaturesOut).use { sigOut ->
                inlineDetach
                    .message(System.`in`)
                    .writeTo(System.out) // message out
                    .writeTo(sigOut) // signatures out
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        const val OPT_NO_ARMOR = "--no-armor"
        const val OPT_SIGNATURES_OUT = "--signatures-out"
    }
}
