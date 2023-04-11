// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.cli.picocli.SopCLI;
import sop.operation.ListProfiles;

@CommandLine.Command(name = "list-profiles",
        resourceBundle = "msg_list-profiles",
        exitCodeOnInvalidInput = 37)
public class ListProfilesCmd extends AbstractSopCmd {

    @CommandLine.Parameters(paramLabel = "COMMAND", arity="0..1", descriptionKey = "subcommand")
    String subcommand;

    @Override
    public void run() {
        ListProfiles listProfiles = throwIfUnsupportedSubcommand(
                SopCLI.getSop().listProfiles(), "list-profiles");

        if (subcommand == null) {
            for (String profile : listProfiles.global()) {
                // CHECKSTYLE:OFF
                System.out.println(profile);
                // CHECKSTYLE:ON
            }
            return;
        }

        for (String profile : listProfiles.ofCommand(subcommand)) {
            // CHECKSTYLE:OFF
            System.out.println(profile);
            // CHECKSTYLE:ON
        }
    }
}
