// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.ReadyWithResult;
import sop.Verification;
import sop.cli.picocli.DateParser;
import sop.cli.picocli.Print;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.InlineVerify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "inline-verify",
        description = "Verify inline-signed data from standard input",
        exitCodeOnInvalidInput = 37)
public class InlineVerifyCmd extends AbstractSopCmd {

    @CommandLine.Parameters(arity = "1..*",
            description = "Public key certificates",
            paramLabel = "CERT")
    List<File> certificates = new ArrayList<>();

    @CommandLine.Option(names = {"--not-before"},
            description = "ISO-8601 formatted UTC date (eg. '2020-11-23T16:35Z)\n" +
                    "Reject signatures with a creation date not in range.\n" +
                    "Defaults to beginning of time (\"-\").",
            paramLabel = "DATE")
    String notBefore = "-";

    @CommandLine.Option(names = {"--not-after"},
            description = "ISO-8601 formatted UTC date (eg. '2020-11-23T16:35Z)\n" +
                    "Reject signatures with a creation date not in range.\n" +
                    "Defaults to current system time (\"now\").\n" +
                    "Accepts special value \"-\" for end of time.",
            paramLabel = "DATE")
    String notAfter = "now";

    @CommandLine.Option(names = "--verifications-out",
            description = "File to write details over successful verifications to")
    File verificationsOut;

    @Override
    public void run() {
        InlineVerify inlineVerify = throwIfUnsupportedSubcommand(
                SopCLI.getSop().inlineVerify(), "inline-verify");

        throwIfOutputExists(verificationsOut, "--verifications-out");

        if (notAfter != null) {
            try {
                inlineVerify.notAfter(DateParser.parseNotAfter(notAfter));
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                Print.errln("Unsupported option '--not-after'.");
                Print.trace(unsupportedOption);
                System.exit(unsupportedOption.getExitCode());
            }
        }
        if (notBefore != null) {
            try {
                inlineVerify.notBefore(DateParser.parseNotBefore(notBefore));
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                Print.errln("Unsupported option '--not-before'.");
                Print.trace(unsupportedOption);
                System.exit(unsupportedOption.getExitCode());
            }
        }

        for (File certFile : certificates) {
            try (FileInputStream certIn = new FileInputStream(certFile)) {
                inlineVerify.cert(certIn);
            } catch (FileNotFoundException fileNotFoundException) {
                Print.errln("Certificate file " + certFile.getAbsolutePath() + " not found.");

                Print.trace(fileNotFoundException);
                System.exit(1);
            } catch (IOException ioException) {
                Print.errln("IO Error.");
                Print.trace(ioException);
                System.exit(1);
            } catch (SOPGPException.BadData badData) {
                Print.errln("Certificate file " + certFile.getAbsolutePath() + " appears to not contain a valid OpenPGP certificate.");
                Print.trace(badData);
                System.exit(badData.getExitCode());
            }
        }

        List<Verification> verifications = null;
        try {
            ReadyWithResult<List<Verification>> ready = inlineVerify.data(System.in);
            verifications = ready.writeTo(System.out);
        } catch (SOPGPException.NoSignature e) {
            Print.errln("No verifiable signature found.");
            Print.trace(e);
            System.exit(e.getExitCode());
        } catch (IOException ioException) {
            Print.errln("IO Error.");
            Print.trace(ioException);
            System.exit(1);
        } catch (SOPGPException.BadData badData) {
            Print.errln("Standard Input appears not to contain a valid OpenPGP message.");
            Print.trace(badData);
            System.exit(badData.getExitCode());
        }

        if (verificationsOut != null) {
            try {
                verificationsOut.createNewFile();
                PrintWriter pw = new PrintWriter(verificationsOut);
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
