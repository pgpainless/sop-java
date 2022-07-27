// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.cli.picocli.Print;
import sop.cli.picocli.SopCLI;
import sop.operation.Version;

@CommandLine.Command(name = "version", resourceBundle = "version",
        exitCodeOnInvalidInput = 37)
public class VersionCmd extends AbstractSopCmd {

    @CommandLine.ArgGroup()
    Exclusive exclusive;

    static class Exclusive {
        @CommandLine.Option(names = "--extended",
                descriptionKey = "usage.option.extended")
        boolean extended;

        @CommandLine.Option(names = "--backend",
                descriptionKey = "usage.option.backend")
        boolean backend;
    }



    @Override
    public void run() {
        Version version = throwIfUnsupportedSubcommand(
                SopCLI.getSop().version(), "version");

        if (exclusive == null) {
            Print.outln(version.getName() + " " + version.getVersion());
            return;
        }

        if (exclusive.extended) {
            Print.outln(version.getExtendedVersion());
            return;
        }

        if (exclusive.backend) {
            Print.outln(version.getBackendVersion());
            return;
        }
    }
}
