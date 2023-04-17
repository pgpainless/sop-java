// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.Profile;
import sop.cli.picocli.Print;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.ListProfiles;

@CommandLine.Command(name = "list-profiles",
        resourceBundle = "msg_list-profiles",
        exitCodeOnInvalidInput = 37)
public class ListProfilesCmd extends AbstractSopCmd {

    @CommandLine.Parameters(paramLabel = "COMMAND", arity = "1", descriptionKey = "subcommand")
    String subcommand;

    @Override
    public void run() {
        ListProfiles listProfiles = throwIfUnsupportedSubcommand(
                SopCLI.getSop().listProfiles(), "list-profiles");

        try {
            for (Profile profile : listProfiles.subcommand(subcommand)) {
                Print.outln(profile.toString());
            }
        } catch (SOPGPException.UnsupportedProfile e) {
            String errorMsg = getMsg("sop.error.feature_support.subcommand_does_not_support_profiles", subcommand);
            throw new SOPGPException.UnsupportedProfile(errorMsg, e);
        }
    }
}
