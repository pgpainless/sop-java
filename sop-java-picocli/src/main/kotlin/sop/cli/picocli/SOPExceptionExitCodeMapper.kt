// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli

import picocli.CommandLine.*
import sop.exception.SOPGPException

class SOPExceptionExitCodeMapper : IExitCodeExceptionMapper {

    override fun getExitCode(exception: Throwable): Int =
        if (exception is SOPGPException) {
            // SOPGPExceptions have well-defined exit code
            exception.getExitCode()
        } else if (exception is UnmatchedArgumentException) {
            if (exception.isUnknownOption) {
                // Unmatched option of subcommand (e.g. `generate-key --unknown`)
                SOPGPException.UnsupportedOption.EXIT_CODE
            } else {
                // Unmatched subcommand
                SOPGPException.UnsupportedSubcommand.EXIT_CODE
            }
        } else if (exception is MissingParameterException) {
            SOPGPException.MissingArg.EXIT_CODE
        } else if (exception is ParameterException) {
            // Invalid option (e.g. `--as invalid`)
            SOPGPException.UnsupportedOption.EXIT_CODE
        } else {
            // Others, like IOException etc.
            1
        }
}
