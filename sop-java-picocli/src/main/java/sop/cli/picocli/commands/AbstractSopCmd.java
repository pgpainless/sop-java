// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import sop.exception.SOPGPException;
import sop.util.UTCUtil;
import sop.util.UTF8Util;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public abstract class AbstractSopCmd implements Runnable {

    public interface EnvironmentVariableResolver {
        /**
         * Resolve the value of the given environment variable.
         * Return null if the variable is not present.
         *
         * @param name name of the variable
         * @return variable value or null
         */
        String resolveEnvironmentVariable(String name);
    }

    public static final String PRFX_ENV = "@ENV:";
    public static final String PRFX_FD = "@FD:";
    public static final Date BEGINNING_OF_TIME = new Date(0);
    public static final Date END_OF_TIME = new Date(8640000000000000L);

    public static final Pattern PATTERN_FD = Pattern.compile("^\\d{1,3}$");

    protected final ResourceBundle messages;
    protected EnvironmentVariableResolver envResolver = System::getenv;

    public AbstractSopCmd() {
        this(Locale.getDefault());
    }

    public AbstractSopCmd(@Nonnull Locale locale) {
        messages = ResourceBundle.getBundle("msg_sop", locale);
    }

    void throwIfOutputExists(String output) {
        if (output == null) {
            return;
        }

        File outputFile = new File(output);
        if (outputFile.exists()) {
            String errorMsg = getMsg("sop.error.indirect_data_type.output_file_already_exists", outputFile.getAbsolutePath());
            throw new SOPGPException.OutputExists(errorMsg);
        }
    }

    public String getMsg(String key) {
        return messages.getString(key);
    }

    public String getMsg(String key, String arg1) {
        return String.format(messages.getString(key), arg1);
    }

    public String getMsg(String key, String arg1, String arg2) {
        return String.format(messages.getString(key), arg1, arg2);
    }

    void throwIfMissingArg(Object arg, String argName) {
        if (arg == null) {
            String errorMsg = getMsg("sop.error.usage.argument_required", argName);
            throw new SOPGPException.MissingArg(errorMsg);
        }
    }

    void throwIfEmptyParameters(Collection<?> arg, String parmName) {
        if (arg.isEmpty()) {
            String errorMsg = getMsg("sop.error.usage.parameter_required", parmName);
            throw new SOPGPException.MissingArg(errorMsg);
        }
    }

    <T> T throwIfUnsupportedSubcommand(T subcommand, String subcommandName) {
        if (subcommand == null) {
            String errorMsg = getMsg("sop.error.feature_support.subcommand_not_supported", subcommandName);
            throw new SOPGPException.UnsupportedSubcommand(errorMsg);
        }
        return subcommand;
    }

    void setEnvironmentVariableResolver(EnvironmentVariableResolver envResolver) {
        if (envResolver == null) {
            throw new NullPointerException("Variable envResolver cannot be null.");
        }
        this.envResolver = envResolver;
    }

    public InputStream getInput(String indirectInput) throws IOException {
        if (indirectInput == null) {
            throw new IllegalArgumentException("Input cannot not be null.");
        }

        String trimmed = indirectInput.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be blank.");
        }

        if (trimmed.startsWith(PRFX_ENV)) {
            if (new File(trimmed).exists()) {
                String errorMsg = getMsg("sop.error.indirect_data_type.ambiguous_filename", trimmed);
                throw new SOPGPException.AmbiguousInput(errorMsg);
            }

            String envName = trimmed.substring(PRFX_ENV.length());
            String envValue = envResolver.resolveEnvironmentVariable(envName);
            if (envValue == null) {
                String errorMsg = getMsg("sop.error.indirect_data_type.environment_variable_not_set", envName);
                throw new IllegalArgumentException(errorMsg);
            }

            if (envValue.trim().isEmpty()) {
                String errorMsg = getMsg("sop.error.indirect_data_type.environment_variable_empty", envName);
                throw new IllegalArgumentException(errorMsg);
            }

            return new ByteArrayInputStream(envValue.getBytes("UTF8"));

        } else if (trimmed.startsWith(PRFX_FD)) {

            if (new File(trimmed).exists()) {
                String errorMsg = getMsg("sop.error.indirect_data_type.ambiguous_filename", trimmed);
                throw new SOPGPException.AmbiguousInput(errorMsg);
            }

            File fdFile = fileDescriptorFromString(trimmed);
            try {
                FileInputStream fileIn = new FileInputStream(fdFile);
                return fileIn;
            } catch (FileNotFoundException e) {
                String errorMsg = getMsg("sop.error.indirect_data_type.file_descriptor_not_found", fdFile.getAbsolutePath());
                throw new IOException(errorMsg, e);
            }
        } else {
            File file = new File(trimmed);
            if (!file.exists()) {
                String errorMsg = getMsg("sop.error.indirect_data_type.input_file_does_not_exist", file.getAbsolutePath());
                throw new SOPGPException.MissingInput(errorMsg);
            }

            if (!file.isFile()) {
                String errorMsg = getMsg("sop.error.indirect_data_type.input_not_a_file", file.getAbsolutePath());
                throw new SOPGPException.MissingInput(errorMsg);
            }

            return new FileInputStream(file);
        }
    }

    public OutputStream getOutput(String indirectOutput) throws IOException {
        if (indirectOutput == null) {
            throw new IllegalArgumentException("Output cannot be null.");
        }

        String trimmed = indirectOutput.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be blank.");
        }

        // @ENV not allowed for output
        if (trimmed.startsWith(PRFX_ENV)) {
            String errorMsg = getMsg("sop.error.indirect_data_type.illegal_use_of_env_designator");
            throw new SOPGPException.UnsupportedSpecialPrefix(errorMsg);
        }

        // File Descriptor
        if (trimmed.startsWith(PRFX_FD)) {
            File fdFile = fileDescriptorFromString(trimmed);
            try {
                FileOutputStream fout = new FileOutputStream(fdFile);
                return fout;
            } catch (FileNotFoundException e) {
                String errorMsg = getMsg("sop.error.indirect_data_type.file_descriptor_not_found", fdFile.getAbsolutePath());
                throw new IOException(errorMsg, e);
            }
        }

        File file = new File(trimmed);
        if (file.exists()) {
            String errorMsg = getMsg("sop.error.indirect_data_type.output_file_already_exists", file.getAbsolutePath());
            throw new SOPGPException.OutputExists(errorMsg);
        }

        if (!file.createNewFile()) {
            String errorMsg = getMsg("sop.error.indirect_data_type.output_file_cannot_be_created", file.getAbsolutePath());
            throw new IOException(errorMsg);
        }

        return new FileOutputStream(file);
    }

    public File fileDescriptorFromString(String fdString) {
        File fdDir = new File("/dev/fd/");
        if (!fdDir.exists()) {
            String errorMsg = getMsg("sop.error.indirect_data_type.designator_fd_not_supported");
            throw new SOPGPException.UnsupportedSpecialPrefix(errorMsg);
        }
        String fdNumber = fdString.substring(PRFX_FD.length());
        if (!PATTERN_FD.matcher(fdNumber).matches()) {
            throw new IllegalArgumentException("File descriptor must be a 1-3 digit, positive number.");
        }
        File descriptor = new File(fdDir, fdNumber);
        return descriptor;
    }

    public static String stringFromInputStream(InputStream inputStream) throws IOException {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            byte[] buf = new byte[4096]; int read;
            while ((read = inputStream.read(buf)) != -1) {
                byteOut.write(buf, 0, read);
            }
            // TODO: For decrypt operations we MUST accept non-UTF8 passwords
            return UTF8Util.decodeUTF8(byteOut.toByteArray());
        } finally {
            inputStream.close();
        }
    }

    public Date parseNotAfter(String notAfter) {
        Date date = notAfter.equals("now") ? new Date() : notAfter.equals("-") ? END_OF_TIME : UTCUtil.parseUTCDate(notAfter);
        if (date == null) {
            String errorMsg = getMsg("sop.error.input.malformed_not_after");
            throw new IllegalArgumentException(errorMsg);
        }
        return date;
    }

    public Date parseNotBefore(String notBefore) {
        Date date = notBefore.equals("now") ? new Date() : notBefore.equals("-") ? BEGINNING_OF_TIME : UTCUtil.parseUTCDate(notBefore);
        if (date == null) {
            String errorMsg = getMsg("sop.error.input.malformed_not_before");
            throw new IllegalArgumentException(errorMsg);
        }
        return date;
    }

}
