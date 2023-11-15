// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import picocli.CommandLine.ArgGroup
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException

@Command(
    name = "version",
    resourceBundle = "msg_version",
    exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
class VersionCmd : AbstractSopCmd() {

    @ArgGroup var exclusive: Exclusive? = null

    class Exclusive {
        @Option(names = ["--extended"]) var extended: Boolean = false
        @Option(names = ["--backend"]) var backend: Boolean = false
        @Option(names = ["--sop-spec"]) var sopSpec: Boolean = false
    }

    override fun run() {
        val version = throwIfUnsupportedSubcommand(SopCLI.getSop().version(), "version")

        if (exclusive == null) {
            // No option provided
            println("${version.getName()} ${version.getVersion()}")
            return
        }

        if (exclusive!!.extended) {
            println(version.getExtendedVersion())
            return
        }

        if (exclusive!!.backend) {
            println(version.getBackendVersion())
            return
        }

        if (exclusive!!.sopSpec) {
            println(version.getSopSpecVersion())
            return
        }
    }
}
