// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli;

import picocli.AutoComplete;
import picocli.CommandLine;
import sop.SOP;
import sop.cli.picocli.commands.ArmorCmd;
import sop.cli.picocli.commands.ChangeKeyPasswordCmd;
import sop.cli.picocli.commands.DearmorCmd;
import sop.cli.picocli.commands.DecryptCmd;
import sop.cli.picocli.commands.InlineDetachCmd;
import sop.cli.picocli.commands.EncryptCmd;
import sop.cli.picocli.commands.ExtractCertCmd;
import sop.cli.picocli.commands.GenerateKeyCmd;
import sop.cli.picocli.commands.InlineSignCmd;
import sop.cli.picocli.commands.InlineVerifyCmd;
import sop.cli.picocli.commands.ListProfilesCmd;
import sop.cli.picocli.commands.RevokeKeyCmd;
import sop.cli.picocli.commands.SignCmd;
import sop.cli.picocli.commands.VerifyCmd;
import sop.cli.picocli.commands.VersionCmd;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@CommandLine.Command(
        name = "sop",
        resourceBundle = "msg_sop",
        exitCodeOnInvalidInput = 69,
        subcommands = {
                // Meta Subcommands
                VersionCmd.class,
                ListProfilesCmd.class,
                // Key and Certificate Management Subcommands
                GenerateKeyCmd.class,
                ChangeKeyPasswordCmd.class,
                RevokeKeyCmd.class,
                ExtractCertCmd.class,
                // Messaging Subcommands
                SignCmd.class,
                VerifyCmd.class,
                EncryptCmd.class,
                DecryptCmd.class,
                InlineDetachCmd.class,
                InlineSignCmd.class,
                InlineVerifyCmd.class,
                // Transport Subcommands
                ArmorCmd.class,
                DearmorCmd.class,
                // Miscellaneous Subcommands
                CommandLine.HelpCommand.class,
                AutoComplete.GenerateCompletion.class
        }
)
public class SopCLI {
    // Singleton
    static SOP SOP_INSTANCE;
    static ResourceBundle cliMsg = ResourceBundle.getBundle("msg_sop");

    public static String EXECUTABLE_NAME = "sop";

    @CommandLine.Option(names = {"--stacktrace"},
            scope = CommandLine.ScopeType.INHERIT)
    static boolean stacktrace;

    public static void main(String[] args) {
        int exitCode = execute(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public static int execute(String[] args) {

        // Set locale
        new CommandLine(new InitLocale()).parseArgs(args);

        // get error message bundle
        cliMsg = ResourceBundle.getBundle("msg_sop");

        // Prepare CLI
        CommandLine cmd = new CommandLine(SopCLI.class);

        // explicitly set help command resource bundle
        cmd.getSubcommands().get("help").setResourceBundle(ResourceBundle.getBundle("msg_help"));

        // Hide generate-completion command
        cmd.getSubcommands().get("generate-completion").getCommandSpec().usageMessage().hidden(true);

        cmd.setCommandName(EXECUTABLE_NAME)
                .setExecutionExceptionHandler(new SOPExecutionExceptionHandler())
                .setExitCodeExceptionMapper(new SOPExceptionExitCodeMapper())
                .setCaseInsensitiveEnumValuesAllowed(true);

        return cmd.execute(args);
    }

    public static SOP getSop() {
        if (SOP_INSTANCE == null) {
            String errorMsg = cliMsg.getString("sop.error.runtime.no_backend_set");
            throw new IllegalStateException(errorMsg);
        }
        return SOP_INSTANCE;
    }

    public static void setSopInstance(SOP instance) {
        SOP_INSTANCE = instance;
    }
}

/**
 * Control the locale.
 *
 * @see <a href="https://picocli.info/#_controlling_the_locale">Picocli Readme</a>
 */
class InitLocale {
    @CommandLine.Option(names = { "-l", "--locale" }, descriptionKey = "sop.locale")
    void setLocale(String locale) {
        Locale.setDefault(new Locale(locale));
    }

    @CommandLine.Unmatched
    List<String> remainder; // ignore any other parameters and options in the first parsing phase
}
