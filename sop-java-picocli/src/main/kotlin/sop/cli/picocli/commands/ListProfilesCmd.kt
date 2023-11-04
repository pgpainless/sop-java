// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException
import sop.exception.SOPGPException.UnsupportedProfile

@Command(
    name = "list-profiles",
    resourceBundle = "msg_list-profiles",
    exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
class ListProfilesCmd : AbstractSopCmd() {

    @Parameters(paramLabel = "COMMAND", arity = "1", descriptionKey = "subcommand")
    lateinit var subcommand: String

    override fun run() {
        val listProfiles =
            throwIfUnsupportedSubcommand(SopCLI.getSop().listProfiles(), "list-profiles")

        try {
            listProfiles.subcommand(subcommand).forEach { println(it) }
        } catch (e: UnsupportedProfile) {
            val errorMsg =
                getMsg("sop.error.feature_support.subcommand_does_not_support_profiles", subcommand)
            throw UnsupportedProfile(errorMsg, e)
        }
    }
}
