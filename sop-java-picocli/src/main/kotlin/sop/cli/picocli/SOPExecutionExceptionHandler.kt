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

        commandLine.errln(ex.message ?: ex.javaClass.name)
        // Print second line with detailed cause
        commandLine.errln(ex.cause?.message)

        if (SopCLI.stacktrace) {
            ex.printStackTrace(commandLine.err)
        }

        return exitCode
    }
}

fun CommandLine.errln(text: String?) {
    text?.let { this.err.println(colorScheme.errorText(it)) }
}
