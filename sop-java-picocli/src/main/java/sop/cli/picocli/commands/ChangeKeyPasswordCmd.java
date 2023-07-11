// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.ChangeKeyPassword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "change-key-password",
        resourceBundle = "msg_change-key-password",
        exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
public class ChangeKeyPasswordCmd extends AbstractSopCmd {

    @CommandLine.Option(names = "--no-armor",
            negatable = true)
    boolean armor = true;

    @CommandLine.Option(names = {"--old-key-password"})
    List<String> oldKeyPasswords = new ArrayList<>();

    @CommandLine.Option(names = {"--new-key-password"}, arity = "0..1")
    String newKeyPassword = null;

    @Override
    public void run() {
        ChangeKeyPassword changeKeyPassword = throwIfUnsupportedSubcommand(
                SopCLI.getSop().changeKeyPassword(), "change-key-password");

        if (!armor) {
            changeKeyPassword.noArmor();
        }

        for (String oldKeyPassword : oldKeyPasswords) {
            changeKeyPassword.oldKeyPassphrase(oldKeyPassword);
        }

        if (newKeyPassword != null) {
            changeKeyPassword.newKeyPassphrase(newKeyPassword);
        }

        try {
            changeKeyPassword.keys(System.in).writeTo(System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
