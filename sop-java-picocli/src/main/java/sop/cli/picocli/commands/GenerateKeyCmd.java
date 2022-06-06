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
        resourceBundle = "sop",
        exitCodeOnInvalidInput = 37)
public class GenerateKeyCmd extends AbstractSopCmd {

    @CommandLine.Option(names = "--no-armor",
            descriptionKey = "sop.generate-key.usage.option.armor",
            negatable = true)
    boolean armor = true;

    @CommandLine.Parameters(descriptionKey = "sop.generate-key.usage.option.user_id")
    List<String> userId = new ArrayList<>();

    @CommandLine.Option(names = "--with-key-password",
            descriptionKey = "sop.generate-key.usage.option.with_key_password",
            paramLabel = "PASSWORD")
    String withKeyPassword;

    @Override
    public void run() {
        GenerateKey generateKey = throwIfUnsupportedSubcommand(
                SopCLI.getSop().generateKey(), "generate-key");

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
