// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.Command
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException
import sop.exception.SOPGPException.BadData

@Command(
    name = "dearmor",
    resourceBundle = "msg_dearmor",
    exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
class DearmorCmd : AbstractSopCmd() {

    override fun run() {
        val dearmor = throwIfUnsupportedSubcommand(SopCLI.getSop().dearmor(), "dearmor")

        try {
            dearmor.data(System.`in`).writeTo(System.out)
        } catch (badData: BadData) {
            val errorMsg = getMsg("sop.error.input.stdin_not_openpgp_data")
            throw BadData(errorMsg, badData)
        } catch (e: IOException) {
            e.message?.let {
                val errorMsg = getMsg("sop.error.input.stdin_not_openpgp_data")
                if (it == "invalid armor" ||
                    it == "invalid armor header" ||
                    it == "inconsistent line endings in headers" ||
                    it.startsWith("unable to decode base64 data")) {
                    throw BadData(errorMsg, e)
                }
            }
                ?: throw RuntimeException(e)
        }
    }
}
