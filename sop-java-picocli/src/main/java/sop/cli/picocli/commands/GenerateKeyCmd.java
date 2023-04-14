// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.Ready;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.GenerateKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "generate-key",
        resourceBundle = "msg_generate-key",
        exitCodeOnInvalidInput = 37)
public class GenerateKeyCmd extends AbstractSopCmd {

    @CommandLine.Option(names = "--no-armor",
            negatable = true)
    boolean armor = true;

    @CommandLine.Parameters(paramLabel = "USERID")
    List<String> userId = new ArrayList<>();

    @CommandLine.Option(names = "--with-key-password",
            paramLabel = "PASSWORD")
    String withKeyPassword;

    @CommandLine.Option(names = "--profile",
            paramLabel = "PROFILE")
    String profile;

    @Override
    public void run() {
        GenerateKey generateKey = throwIfUnsupportedSubcommand(
                SopCLI.getSop().generateKey(), "generate-key");

        if (profile != null) {
            try {
                generateKey.profile(profile);
            } catch (SOPGPException.UnsupportedProfile e) {
                String errorMsg = getMsg("sop.error.usage.profile_not_supported", "generate-key", profile);
                throw new SOPGPException.UnsupportedProfile(errorMsg, e);
            }
        }

        for (String userId : userId) {
            generateKey.userId(userId);
        }

        if (!armor) {
            generateKey.noArmor();
        }

        if (withKeyPassword != null) {
            try {
                String password = stringFromInputStream(getInput(withKeyPassword));
                generateKey.withKeyPassword(password);
            } catch (SOPGPException.UnsupportedOption e) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--with-key-password");
                throw new SOPGPException.UnsupportedOption(errorMsg, e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            Ready ready = generateKey.generate();
            ready.writeTo(System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
