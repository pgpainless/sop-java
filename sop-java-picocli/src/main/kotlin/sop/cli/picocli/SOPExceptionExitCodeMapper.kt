// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli

import picocli.CommandLine.*
import sop.exception.SOPGPException

class SOPExceptionExitCodeMapper : IExitCodeExceptionMapper {

    override fun getExitCode(exception: Throwable): Int =
        when (exception) {
            // SOPGPExceptions have well-defined exit codes
            is SOPGPException -> exception.getExitCode()
            is UnmatchedArgumentException -> {
                // Unmatched option flag (e.g. `--unknown`)
                if (exception.isUnknownOption) SOPGPException.UnsupportedOption.EXIT_CODE
                // Unmatched subcommand (e.g. `sop unknown`)
                else SOPGPException.UnsupportedSubcommand.EXIT_CODE
            }

            // Missing mandatory positional parameter
            is MissingParameterException -> SOPGPException.MissingArg.EXIT_CODE

            // Unmatched option flag value (e.g. `--as invalid`)
            is ParameterException -> SOPGPException.UnsupportedOption.EXIT_CODE

            // Others, like IOException, NullPointerException etc.
            else -> 1
        }
}
