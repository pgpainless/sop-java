// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.ReadyWithResult;
import sop.Verification;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.InlineVerify;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "inline-verify",
        resourceBundle = "msg_inline-verify",
        exitCodeOnInvalidInput = 37)
public class InlineVerifyCmd extends AbstractSopCmd {

    @CommandLine.Parameters(arity = "0..*",
            paramLabel = "CERT")
    List<String> certificates = new ArrayList<>();

    @CommandLine.Option(names = {"--not-before"},
            paramLabel = "DATE")
    String notBefore = "-";

    @CommandLine.Option(names = {"--not-after"},
            paramLabel = "DATE")
    String notAfter = "now";

    @CommandLine.Option(names = "--verifications-out")
    String verificationsOut;

    @Override
    public void run() {
        InlineVerify inlineVerify = throwIfUnsupportedSubcommand(
                SopCLI.getSop().inlineVerify(), "inline-verify");

        throwIfOutputExists(verificationsOut);

        if (notAfter != null) {
            try {
                inlineVerify.notAfter(parseNotAfter(notAfter));
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--not-after");
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            }
        }
        if (notBefore != null) {
            try {
                inlineVerify.notBefore(parseNotBefore(notBefore));
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--not-before");
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            }
        }

        for (String certInput : certificates) {
            try (InputStream certIn = getInput(certInput)) {
                inlineVerify.cert(certIn);
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            } catch (SOPGPException.UnsupportedAsymmetricAlgo unsupportedAsymmetricAlgo) {
                String errorMsg = getMsg("sop.error.runtime.cert_uses_unsupported_asymmetric_algorithm", certInput);
                throw new SOPGPException.UnsupportedAsymmetricAlgo(errorMsg, unsupportedAsymmetricAlgo);
            } catch (SOPGPException.BadData badData) {
                String errorMsg = getMsg("sop.error.input.not_a_certificate", certInput);
                throw new SOPGPException.BadData(errorMsg, badData);
            }
        }

        List<Verification> verifications = null;
        try {
            ReadyWithResult<List<Verification>> ready = inlineVerify.data(System.in);
            verifications = ready.writeTo(System.out);
        } catch (SOPGPException.NoSignature e) {
            String errorMsg = getMsg("sop.error.runtime.no_verifiable_signature_found");
            throw new SOPGPException.NoSignature(errorMsg, e);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        } catch (SOPGPException.BadData badData) {
            String errorMsg = getMsg("sop.error.input.stdin_not_a_message");
            throw new SOPGPException.BadData(errorMsg, badData);
        }

        if (verificationsOut != null) {
            try (OutputStream outputStream = getOutput(verificationsOut)) {
                PrintWriter pw = new PrintWriter(outputStream);
                for (Verification verification : verifications) {
                    // CHECKSTYLE:OFF
                    pw.println(verification);
                    // CHECKSTYLE:ON
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
