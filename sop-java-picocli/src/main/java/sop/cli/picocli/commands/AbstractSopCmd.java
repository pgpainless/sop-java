// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import sop.exception.SOPGPException;

import java.io.File;
import java.util.Collection;

public abstract class AbstractSopCmd implements Runnable {

    static final String ERROR_UNSUPPORTED_OPTION = "Option '%s' is not supported.";
    static final String ERROR_FILE_NOT_EXIST = "File '%s' does not exist.";
    static final String ERROR_OUTPUT_OF_OPTION_EXISTS = "Target %s of option %s already exists.";

    void throwIfOutputExists(File outputFile, String optionName) {
        if (outputFile == null) {
            return;
        }

        if (outputFile.exists()) {
            throw new SOPGPException.OutputExists(String.format(ERROR_OUTPUT_OF_OPTION_EXISTS, outputFile.getAbsolutePath(), optionName));
        }
    }

    void throwIfMissingArg(Object arg, String argName) {
        if (arg == null) {
            throw new SOPGPException.MissingArg(argName + " is required.");
        }
    }

    void throwIfEmptyParameters(Collection<?> arg, String parmName) {
        if (arg.isEmpty()) {
            throw new SOPGPException.MissingArg("Parameter '" + parmName + "' is required.");
        }
    }

    <T> T throwIfUnsupportedSubcommand(T subcommand, String subcommandName) {
        if (subcommand == null) {
            throw new SOPGPException.UnsupportedSubcommand("Command '" + subcommandName + "' is not supported.");
        }
        return subcommand;
    }

}
