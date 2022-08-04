// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.Signatures;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.InlineDetach;

import java.io.IOException;
import java.io.OutputStream;

@CommandLine.Command(name = "inline-detach",
        resourceBundle = "msg_inline-detach",
        exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
public class InlineDetachCmd extends AbstractSopCmd {

    @CommandLine.Option(
            names = {"--signatures-out"},
            paramLabel = "SIGNATURES")
    String signaturesOut;

    @CommandLine.Option(names = "--no-armor",
            negatable = true)
    boolean armor = true;

    @Override
    public void run() {
        InlineDetach inlineDetach = throwIfUnsupportedSubcommand(
                SopCLI.getSop().inlineDetach(), "inline-detach");

        throwIfOutputExists(signaturesOut);
        throwIfMissingArg(signaturesOut, "--signatures-out");

        if (!armor) {
            inlineDetach.noArmor();
        }

        try (OutputStream outputStream = getOutput(signaturesOut)) {
            Signatures signatures = inlineDetach
                    .message(System.in).writeTo(System.out);
            signatures.writeTo(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
