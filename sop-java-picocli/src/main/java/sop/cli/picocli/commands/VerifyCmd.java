// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.Verification;
import sop.cli.picocli.Print;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.DetachedVerify;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "verify",
        resourceBundle = "msg_detached-verify",
        exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
public class VerifyCmd extends AbstractSopCmd {

    @CommandLine.Parameters(index = "0",
            paramLabel = "SIGNATURE")
    String signature;

    @CommandLine.Parameters(index = "1..*",
            arity = "1..*",
            paramLabel = "CERT")
    List<String> certificates = new ArrayList<>();

    @CommandLine.Option(names = {"--not-before"},
            paramLabel = "DATE")
    String notBefore = "-";

    @CommandLine.Option(names = {"--not-after"},
            paramLabel = "DATE")
    String notAfter = "now";

    @Override
    public void run() {
        DetachedVerify detachedVerify = throwIfUnsupportedSubcommand(
                SopCLI.getSop().detachedVerify(), "verify");

        if (notAfter != null) {
            try {
                detachedVerify.notAfter(parseNotAfter(notAfter));
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--not-after");
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            }
        }
        if (notBefore != null) {
            try {
                detachedVerify.notBefore(parseNotBefore(notBefore));
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--not-before");
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            }
        }

        for (String certInput : certificates) {
            try (InputStream certIn = getInput(certInput)) {
                detachedVerify.cert(certIn);
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            } catch (SOPGPException.BadData badData) {
                String errorMsg = getMsg("sop.error.input.not_a_certificate", certInput);
                throw new SOPGPException.BadData(errorMsg, badData);
            }
        }

        if (signature != null) {
            try (InputStream sigIn = getInput(signature)) {
                detachedVerify.signatures(sigIn);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SOPGPException.BadData badData) {
                String errorMsg = getMsg("sop.error.input.not_a_signature", signature);
                throw new SOPGPException.BadData(errorMsg, badData);
            }
        }

        List<Verification> verifications;
        try {
            verifications = detachedVerify.data(System.in);
        } catch (SOPGPException.NoSignature e) {
            String errorMsg = getMsg("sop.error.runtime.no_verifiable_signature_found");
            throw new SOPGPException.NoSignature(errorMsg, e);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        } catch (SOPGPException.BadData badData) {
            String errorMsg = getMsg("sop.error.input.stdin_not_a_message");
            throw new SOPGPException.BadData(errorMsg, badData);
        }

        for (Verification verification : verifications) {
            Print.outln(verification.toString());
        }
    }
}
