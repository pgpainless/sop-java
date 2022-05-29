// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import picocli.CommandLine;
import sop.Ready;
import sop.cli.picocli.FileUtil;
import sop.cli.picocli.Print;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.GenerateKey;

@CommandLine.Command(name = "generate-key",
        description = "Generate a secret key",
        exitCodeOnInvalidInput = 37)
public class GenerateKeyCmd extends AbstractSopCmd {

    @CommandLine.Option(names = "--no-armor",
            description = "ASCII armor the output",
            negatable = true)
    boolean armor = true;

    @CommandLine.Parameters(description = "User-ID, eg. \"Alice <alice@example.com>\"")
    List<String> userId = new ArrayList<>();

    @CommandLine.Option(names = "--with-key-password",
            description = "Indirect file type pointing to file containing password to protect the key",
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
                String password = FileUtil.stringFromInputStream(FileUtil.getFileInputStream(withKeyPassword));
                generateKey.withKeyPassword(password);
            } catch (SOPGPException.UnsupportedOption e) {
                throw new SOPGPException.UnsupportedOption("Option '--with-key-password' is not supported.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            Ready ready = generateKey.generate();
            ready.writeTo(System.out);
        } catch (SOPGPException.MissingArg missingArg) {
            Print.errln("Missing argument.");
            Print.trace(missingArg);
            System.exit(missingArg.getExitCode());
        } catch (SOPGPException.UnsupportedAsymmetricAlgo unsupportedAsymmetricAlgo) {
            Print.errln("Unsupported asymmetric algorithm.");
            Print.trace(unsupportedAsymmetricAlgo);
            System.exit(unsupportedAsymmetricAlgo.getExitCode());
        } catch (IOException e) {
            Print.errln("IO Error.");
            Print.trace(e);
            System.exit(1);
        }
    }
}
