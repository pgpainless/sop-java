// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import picocli.CommandLine;
import sop.Signatures;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.InlineDetach;

@CommandLine.Command(name = "inline-detach",
        description = "Split a clearsigned message",
        exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
public class InlineDetachCmd extends AbstractSopCmd {

    @CommandLine.Option(
            names = {"--signatures-out"},
            description = "Destination to which a detached signatures block will be written",
            paramLabel = "SIGNATURES")
    File signaturesOut;

    @CommandLine.Option(names = "--no-armor",
            description = "ASCII armor the output",
            negatable = true)
    boolean armor = true;

    @Override
    public void run() {
        InlineDetach inlineDetach = throwIfUnsupportedSubcommand(
                SopCLI.getSop().inlineDetach(), "inline-detach");

        throwIfOutputExists(signaturesOut, "--signatures-out");
        throwIfMissingArg(signaturesOut, "--signatures-out");

        if (!armor) {
            inlineDetach.noArmor();
        }

        try {
            Signatures signatures = inlineDetach
                    .message(System.in).writeTo(System.out);
            signaturesOut.createNewFile();
            signatures.writeTo(new FileOutputStream(signaturesOut));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
