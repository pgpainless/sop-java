// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli

import picocli.CommandLine
import picocli.CommandLine.IExecutionExceptionHandler

class SOPExecutionExceptionHandler : IExecutionExceptionHandler {
    override fun handleExecutionException(
        ex: Exception,
        commandLine: CommandLine,
        parseResult: CommandLine.ParseResult
    ): Int {
        val exitCode =
            if (commandLine.exitCodeExceptionMapper != null)
                commandLine.exitCodeExceptionMapper.getExitCode(ex)
            else commandLine.commandSpec.exitCodeOnExecutionException()

        val colorScheme = commandLine.colorScheme
        if (ex.message != null) {
            commandLine.getErr().println(colorScheme.errorText(ex.message))
        } else {
            commandLine.getErr().println(ex.javaClass.getName())
        }
        // Print second line with detailed cause
        if (ex.cause?.message != null) {
            commandLine.err.println(ex.cause?.message!!)
        }

        if (SopCLI.stacktrace) {
            ex.printStackTrace(commandLine.getErr())
        }

        return exitCode
    }
}
