// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.Command
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException.BadData
import sop.exception.SOPGPException.UnsupportedOption

@Command(
    name = "armor",
    resourceBundle = "msg_armor",
    exitCodeOnInvalidInput = UnsupportedOption.EXIT_CODE)
class ArmorCmd : AbstractSopCmd() {

    override fun run() {
        val armor = throwIfUnsupportedSubcommand(SopCLI.getSop().armor(), "armor")

        try {
            val ready = armor.data(System.`in`)
            ready.writeTo(System.out)
        } catch (badData: BadData) {
            val errorMsg = getMsg("sop.error.input.stdin_not_openpgp_data")
            throw BadData(errorMsg, badData)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
