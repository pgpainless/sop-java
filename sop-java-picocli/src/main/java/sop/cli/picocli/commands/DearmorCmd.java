// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.Dearmor;

import java.io.IOException;

@CommandLine.Command(name = "dearmor",
        resourceBundle = "dearmor",
        exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
public class DearmorCmd extends AbstractSopCmd {

    @Override
    public void run() {
        Dearmor dearmor = throwIfUnsupportedSubcommand(
                SopCLI.getSop().dearmor(), "dearmor");

        try {
            dearmor.data(System.in)
                    .writeTo(System.out);
        } catch (SOPGPException.BadData e) {
            String errorMsg = getMsg("sop.error.input.stdin_not_openpgp_data");
            throw new SOPGPException.BadData(errorMsg, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
