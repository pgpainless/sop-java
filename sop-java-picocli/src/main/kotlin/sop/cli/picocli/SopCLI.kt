// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli

import java.util.*
import kotlin.system.exitProcess
import picocli.AutoComplete.GenerateCompletion
import picocli.CommandLine
import picocli.CommandLine.*
import sop.SOP
import sop.cli.picocli.commands.*
import sop.exception.SOPGPException

@Command(
    name = "sop",
    resourceBundle = "msg_sop",
    exitCodeOnInvalidInput = SOPGPException.UnsupportedSubcommand.EXIT_CODE,
    subcommands =
        [
            // Meta subcommands
            VersionCmd::class,
            ListProfilesCmd::class,
            // Key and certificate management
            GenerateKeyCmd::class,
            ChangeKeyPasswordCmd::class,
            RevokeKeyCmd::class,
            ExtractCertCmd::class,
            UpdateKeyCmd::class,
            MergeCertsCmd::class,
            CertifyUserIdCmd::class,
            ValidateUserIdCmd::class,
            // Messaging subcommands
            SignCmd::class,
            VerifyCmd::class,
            EncryptCmd::class,
            DecryptCmd::class,
            InlineDetachCmd::class,
            InlineSignCmd::class,
            InlineVerifyCmd::class,
            // Transport
            ArmorCmd::class,
            DearmorCmd::class,
            // misc
            HelpCommand::class,
            GenerateCompletion::class])
class SopCLI {

    companion object {
        @JvmStatic private var sopInstance: SOP? = null

        @JvmStatic
        fun getSop(): SOP =
            checkNotNull(sopInstance) { cliMsg.getString("sop.error.runtime.no_backend_set") }

        @JvmStatic
        fun setSopInstance(sop: SOP?) {
            sopInstance = sop
        }

        @JvmField var cliMsg: ResourceBundle = ResourceBundle.getBundle("msg_sop")

        @JvmField var EXECUTABLE_NAME = "sop"

        @JvmField
        @Option(names = ["--stacktrace"], scope = ScopeType.INHERIT)
        var stacktrace = false

        @JvmStatic
        fun main(vararg args: String) {
            val exitCode = execute(*args)
            if (exitCode != 0) {
                exitProcess(exitCode)
            }
        }

        @JvmStatic
        fun execute(vararg args: String): Int {
            // Set locale
            CommandLine(InitLocale()).setUnmatchedArgumentsAllowed(true).parseArgs(*args)

            // Re-set bundle with updated locale
            cliMsg = ResourceBundle.getBundle("msg_sop")

            return CommandLine(SopCLI::class.java)
                .apply {
                    // Hide generate-completion command
                    subcommands["generate-completion"]?.commandSpec?.usageMessage()?.hidden(true)
                    // render Input/Output sections in help command
                    subcommands.values
                        .filter {
                            (it.getCommand() as Any) is AbstractSopCmd
                        } // Only for AbstractSopCmd objects
                        .forEach { (it.getCommand() as AbstractSopCmd).installIORenderer(it) }
                    // overwrite executable name
                    commandName = EXECUTABLE_NAME
                    // setup exception handling
                    executionExceptionHandler = SOPExecutionExceptionHandler()
                    exitCodeExceptionMapper = SOPExceptionExitCodeMapper()
                    isCaseInsensitiveEnumValuesAllowed = true
                }
                .execute(*args)
        }
    }

    /**
     * Control the locale.
     *
     * @see <a href="https://picocli.info/#_controlling_the_locale">Picocli Readme</a>
     */
    @Command
    class InitLocale {
        @Option(names = ["-l", "--locale"], descriptionKey = "sop.locale")
        fun setLocale(locale: String) = Locale.setDefault(Locale(locale))

        @Unmatched
        var remainder: MutableList<String> =
            mutableListOf() // ignore any other parameters and options in the first parsing phase
    }
}
