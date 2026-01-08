// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException
import sop.exception.SOPGPException.BadData

@Command(
    name = "extract-cert",
    resourceBundle = "msg_extract-cert",
    exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
class ExtractCertCmd : AbstractSopCmd() {

    @Option(names = [OPT_NO_ARMOR], negatable = true) var armor = true

    override fun run() {
        val extractCert =
            throwIfUnsupportedSubcommand(SopCLI.getSop().extractCert(), "extract-cert")

        if (!armor) {
            throwIfUnsupportedOption(OPT_NO_ARMOR) { extractCert.noArmor() }
        }

        try {
            val ready = extractCert.key(System.`in`)
            ready.writeTo(System.out)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (badData: BadData) {
            val errorMsg = getMsg("sop.error.input.stdin_not_a_private_key")
            throw BadData(errorMsg, badData)
        }
    }

    companion object {
        const val OPT_NO_ARMOR = "--no-armor"
    }
}
