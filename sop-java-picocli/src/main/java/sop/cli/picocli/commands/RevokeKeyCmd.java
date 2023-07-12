// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.Ready;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.RevokeKey;

import java.io.IOException;

@CommandLine.Command(name = "revoke-key",
        resourceBundle = "msg_revoke-key",
        exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
public class RevokeKeyCmd extends AbstractSopCmd {

    @CommandLine.Option(names = "--no-armor",
            negatable = true)
    boolean armor = true;

    @CommandLine.Option(names = "--with-key-password",
            paramLabel = "PASSWORD")
    String withKeyPassword;

    @Override
    public void run() {
        RevokeKey revokeKey = throwIfUnsupportedSubcommand(
                SopCLI.getSop().revokeKey(), "revoke-key");

        if (!armor) {
            revokeKey.noArmor();
        }

        if (withKeyPassword != null) {
            try {
                String password = stringFromInputStream(getInput(withKeyPassword));
                revokeKey.withKeyPassword(password);
            } catch (SOPGPException.UnsupportedOption e) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--with-key-password");
                throw new SOPGPException.UnsupportedOption(errorMsg, e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Ready ready;
        try {
            ready = revokeKey.keys(System.in);
        } catch (SOPGPException.KeyIsProtected e) {
            String errorMsg = getMsg("sop.error.runtime.cannot_unlock_key", "STANDARD_IN");
            throw new SOPGPException.KeyIsProtected(errorMsg, e);
        }
        try {
            ready.writeTo(System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
