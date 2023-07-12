// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import java.io.IOException;

import picocli.CommandLine;
import sop.Ready;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.ExtractCert;

@CommandLine.Command(name = "extract-cert",
        resourceBundle = "msg_extract-cert",
        exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
public class ExtractCertCmd extends AbstractSopCmd {

    @CommandLine.Option(names = "--no-armor",
            negatable = true)
    boolean armor = true;

    @Override
    public void run() {
        ExtractCert extractCert = throwIfUnsupportedSubcommand(
                SopCLI.getSop().extractCert(), "extract-cert");

        if (!armor) {
            extractCert.noArmor();
        }

        try {
            Ready ready = extractCert.key(System.in);
            ready.writeTo(System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SOPGPException.BadData badData) {
            String errorMsg = getMsg("sop.error.input.stdin_not_a_private_key");
            throw new SOPGPException.BadData(errorMsg, badData);
        }
    }
}
