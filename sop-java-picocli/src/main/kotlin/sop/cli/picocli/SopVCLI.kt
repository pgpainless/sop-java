// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli

import java.util.*
import kotlin.system.exitProcess
import picocli.AutoComplete
import picocli.CommandLine
import sop.SOPV
import sop.cli.picocli.commands.*
import sop.exception.SOPGPException

@CommandLine.Command(
    name = "sopv",
    resourceBundle = "msg_sop",
    exitCodeOnInvalidInput = SOPGPException.UnsupportedSubcommand.EXIT_CODE,
    subcommands =
        [
            // Meta subcommands
            VersionCmd::class,
            // signature verification subcommands
            VerifyCmd::class,
            InlineVerifyCmd::class,
            // misc
            CommandLine.HelpCommand::class,
            AutoComplete.GenerateCompletion::class])
class SopVCLI {

    companion object {
        @JvmStatic private var sopvInstance: SOPV? = null

        @JvmStatic
        fun getSopV(): SOPV =
            checkNotNull(sopvInstance) { cliMsg.getString("sop.error.runtime.no_backend_set") }

        @JvmStatic
        fun setSopVInstance(sopv: SOPV?) {
            sopvInstance = sopv
        }

        @JvmField var cliMsg: ResourceBundle = ResourceBundle.getBundle("msg_sop")

        @JvmField var EXECUTABLE_NAME = "sopv"

        @JvmField
        @CommandLine.Option(names = ["--stacktrace"], scope = CommandLine.ScopeType.INHERIT)
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
            CommandLine(InitLocale()).parseArgs(*args)

            // Re-set bundle with updated locale
            cliMsg = ResourceBundle.getBundle("msg_sop")

            return CommandLine(SopVCLI::class.java)
                .apply {
                    // explicitly set help command resource bundle
                    subcommands["help"]?.setResourceBundle(ResourceBundle.getBundle("msg_help"))
                    // Hide generate-completion command
                    subcommands["generate-completion"]?.commandSpec?.usageMessage()?.hidden(true)
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
    @CommandLine.Command
    class InitLocale {
        @CommandLine.Option(names = ["-l", "--locale"], descriptionKey = "sop.locale")
        fun setLocale(locale: String) = Locale.setDefault(Locale(locale))

        @CommandLine.Unmatched
        var remainder: MutableList<String> =
            mutableListOf() // ignore any other parameters and options in the first parsing phase
    }
}
