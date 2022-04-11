// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli;

import picocli.AutoComplete;
import picocli.CommandLine;
import sop.SOP;
import sop.cli.picocli.commands.ArmorCmd;
import sop.cli.picocli.commands.DearmorCmd;
import sop.cli.picocli.commands.DecryptCmd;
import sop.cli.picocli.commands.DetachInbandSignatureAndMessageCmd;
import sop.cli.picocli.commands.EncryptCmd;
import sop.cli.picocli.commands.ExtractCertCmd;
import sop.cli.picocli.commands.GenerateKeyCmd;
import sop.cli.picocli.commands.SignCmd;
import sop.cli.picocli.commands.VerifyCmd;
import sop.cli.picocli.commands.VersionCmd;

@CommandLine.Command(
        exitCodeOnInvalidInput = 69,
        subcommands = {
                CommandLine.HelpCommand.class,
                ArmorCmd.class,
                DearmorCmd.class,
                DecryptCmd.class,
                DetachInbandSignatureAndMessageCmd.class,
                EncryptCmd.class,
                ExtractCertCmd.class,
                GenerateKeyCmd.class,
                SignCmd.class,
                VerifyCmd.class,
                VersionCmd.class,
                AutoComplete.GenerateCompletion.class
        },
        exitCodeListHeading = "Exit Codes:%n",
        exitCodeList = {
                " 0:Successful program execution",
                " 1:Generic program error",
                " 3:Verification requested but no verifiable signature found",
                "13:Unsupported asymmetric algorithm",
                "17:Certificate is not encryption capable",
                "19:Usage error: Missing argument",
                "23:Incomplete verification instructions",
                "29:Unable to decrypt",
                "31:Password is not human-readable",
                "37:Unsupported Option",
                "41:Invalid data or data of wrong type encountered",
                "53:Non-text input received where text was expected",
                "59:Output file already exists",
                "61:Input file does not exist",
                "67:Key is password protected",
                "69:Unsupported subcommand",
                "71:Unsupported special prefix (e.g. \"@env/@fd\") of indirect parameter",
                "73:Ambiguous input (a filename matching the designator already exists)",
                "79:Key is not signing capable"
        }
)
public class SopCLI {
    // Singleton
    static SOP SOP_INSTANCE;

    public static String EXECUTABLE_NAME = "sop";

    public static void main(String[] args) {
        int exitCode = execute(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public static int execute(String[] args) {
        CommandLine cmd = new CommandLine(SopCLI.class);
        // Hide generate-completion command
        CommandLine gen = cmd.getSubcommands().get("generate-completion");
        gen.getCommandSpec().usageMessage().hidden(true);

        cmd.setCommandName(EXECUTABLE_NAME)
                .setExecutionExceptionHandler(new SOPExecutionExceptionHandler())
                .setExitCodeExceptionMapper(new SOPExceptionExitCodeMapper())
                .setCaseInsensitiveEnumValuesAllowed(true);

        return cmd.execute(args);
    }

    public static SOP getSop() {
        if (SOP_INSTANCE == null) {
            throw new IllegalStateException("No SOP backend set.");
        }
        return SOP_INSTANCE;
    }

    public static void setSopInstance(SOP instance) {
        SOP_INSTANCE = instance;
    }
}
