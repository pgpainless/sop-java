// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.*
import java.text.ParseException
import java.util.*
import picocli.CommandLine
import picocli.CommandLine.Help
import picocli.CommandLine.Help.Column
import picocli.CommandLine.Help.TextTable
import picocli.CommandLine.IHelpSectionRenderer
import sop.exception.SOPGPException.*
import sop.util.UTCUtil.Companion.parseUTCDate
import sop.util.UTF8Util.Companion.decodeUTF8

/** Abstract super class of SOP subcommands. */
abstract class AbstractSopCmd(locale: Locale = Locale.getDefault()) : Runnable {

    private val messages: ResourceBundle = ResourceBundle.getBundle("msg_sop", locale)
    var environmentVariableResolver = EnvironmentVariableResolver { name: String ->
        System.getenv(name)
    }

    /** Interface to modularize resolving of environment variables. */
    fun interface EnvironmentVariableResolver {

        /**
         * Resolve the value of the given environment variable. Return null if the variable is not
         * present.
         *
         * @param name name of the variable
         * @return variable value or null
         */
        fun resolveEnvironmentVariable(name: String): String?
    }

    fun throwIfOutputExists(output: String?) {
        output
            ?.let { File(it) }
            ?.let {
                if (it.exists()) {
                    val errorMsg: String =
                        getMsg(
                            "sop.error.indirect_data_type.output_file_already_exists",
                            it.absolutePath)
                    throw OutputExists(errorMsg)
                }
            }
    }

    fun getMsg(key: String): String = messages.getString(key)

    fun getMsg(key: String, vararg args: String): String {
        val msg = messages.getString(key)
        return String.format(msg, *args)
    }

    fun throwIfMissingArg(arg: Any?, argName: String) {
        if (arg == null) {
            val errorMsg = getMsg("sop.error.usage.argument_required", argName)
            throw MissingArg(errorMsg)
        }
    }

    fun throwIfEmptyParameters(arg: Collection<*>, parmName: String) {
        if (arg.isEmpty()) {
            val errorMsg = getMsg("sop.error.usage.parameter_required", parmName)
            throw MissingArg(errorMsg)
        }
    }

    fun <T> throwIfUnsupportedSubcommand(subcommand: T?, subcommandName: String): T {
        if (subcommand == null) {
            val errorMsg =
                getMsg("sop.error.feature_support.subcommand_not_supported", subcommandName)
            throw UnsupportedSubcommand(errorMsg)
        }
        return subcommand
    }

    fun throwIfUnsupportedOption(optionName: String, operation: Runnable) {
        try {
            operation.run()
        } catch (e: UnsupportedOption) {
            val errorMsg = getMsg("sop.error.feature_support.option_not_supported", optionName)
            throw UnsupportedOption(errorMsg, e)
        }
    }

    @Throws(IOException::class)
    fun getInput(indirectInput: String): InputStream {
        val trimmed = indirectInput.trim()
        require(trimmed.isNotBlank()) { "Input cannot be blank." }

        if (trimmed.startsWith(PRFX_ENV)) {
            if (File(trimmed).exists()) {
                val errorMsg = getMsg("sop.error.indirect_data_type.ambiguous_filename", trimmed)
                throw AmbiguousInput(errorMsg)
            }

            val envName = trimmed.substring(PRFX_ENV.length)
            val envValue = environmentVariableResolver.resolveEnvironmentVariable(envName)
            requireNotNull(envValue) {
                getMsg("sop.error.indirect_data_type.environment_variable_not_set", envName)
            }

            require(envValue.trim().isNotEmpty()) {
                getMsg("sop.error.indirect_data_type.environment_variable_empty", envName)
            }

            return envValue.byteInputStream()
        } else if (trimmed.startsWith(PRFX_FD)) {

            if (File(trimmed).exists()) {
                val errorMsg = getMsg("sop.error.indirect_data_type.ambiguous_filename", trimmed)
                throw AmbiguousInput(errorMsg)
            }

            val fdFile: File = fileDescriptorFromString(trimmed)
            return try {
                fdFile.inputStream()
            } catch (e: FileNotFoundException) {
                val errorMsg =
                    getMsg(
                        "sop.error.indirect_data_type.file_descriptor_not_found",
                        fdFile.absolutePath)
                throw IOException(errorMsg, e)
            }
        } else {

            val file = File(trimmed)
            if (!file.exists()) {
                val errorMsg =
                    getMsg(
                        "sop.error.indirect_data_type.input_file_does_not_exist", file.absolutePath)
                throw MissingInput(errorMsg)
            }
            if (!file.isFile()) {
                val errorMsg =
                    getMsg("sop.error.indirect_data_type.input_not_a_file", file.absolutePath)
                throw MissingInput(errorMsg)
            }
            return file.inputStream()
        }
    }

    @Throws(IOException::class)
    fun getOutput(indirectOutput: String?): OutputStream {
        requireNotNull(indirectOutput) { "Output cannot be null." }
        val trimmed = indirectOutput.trim()
        require(trimmed.isNotEmpty()) { "Output cannot be blank." }

        // @ENV not allowed for output
        if (trimmed.startsWith(PRFX_ENV)) {
            val errorMsg = getMsg("sop.error.indirect_data_type.illegal_use_of_env_designator")
            throw UnsupportedSpecialPrefix(errorMsg)
        }

        // File Descriptor
        if (trimmed.startsWith(PRFX_FD)) {
            val fdFile = fileDescriptorFromString(trimmed)
            return try {
                fdFile.outputStream()
            } catch (e: FileNotFoundException) {
                val errorMsg =
                    getMsg(
                        "sop.error.indirect_data_type.file_descriptor_not_found",
                        fdFile.absolutePath)
                throw IOException(errorMsg, e)
            }
        }
        val file = File(trimmed)
        if (file.exists()) {
            val errorMsg =
                getMsg("sop.error.indirect_data_type.output_file_already_exists", file.absolutePath)
            throw OutputExists(errorMsg)
        }
        if (!file.createNewFile()) {
            val errorMsg =
                getMsg(
                    "sop.error.indirect_data_type.output_file_cannot_be_created", file.absolutePath)
            throw IOException(errorMsg)
        }
        return file.outputStream()
    }

    fun fileDescriptorFromString(fdString: String): File {
        val fdDir = File("/dev/fd/")
        if (!fdDir.exists()) {
            val errorMsg = getMsg("sop.error.indirect_data_type.designator_fd_not_supported")
            throw UnsupportedSpecialPrefix(errorMsg)
        }
        val fdNumber = fdString.substring(PRFX_FD.length)
        require(PATTERN_FD.matcher(fdNumber).matches()) {
            "File descriptor must be a positive number."
        }
        return File(fdDir, fdNumber)
    }

    fun parseNotAfter(notAfter: String): Date {
        return when (notAfter) {
            "now" -> Date()
            "-" -> END_OF_TIME
            else ->
                try {
                    parseUTCDate(notAfter)
                } catch (e: ParseException) {
                    val errorMsg = getMsg("sop.error.input.malformed_not_after")
                    throw IllegalArgumentException(errorMsg)
                }
        }
    }

    fun parseNotBefore(notBefore: String): Date {
        return when (notBefore) {
            "now" -> Date()
            "-" -> DAWN_OF_TIME
            else ->
                try {
                    parseUTCDate(notBefore)
                } catch (e: ParseException) {
                    val errorMsg = getMsg("sop.error.input.malformed_not_before")
                    throw IllegalArgumentException(errorMsg)
                }
        }
    }

    /**
     * See
     * [Example](https://github.com/remkop/picocli/blob/main/picocli-examples/src/main/java/picocli/examples/customhelp/EnvironmentVariablesSection.java)
     */
    class InputOutputHelpSectionRenderer(private val argument: Pair<String?, String?>) :
        IHelpSectionRenderer {

        override fun render(help: Help): String {
            return argument.let {
                val calcLen =
                    help.calcLongOptionColumnWidth(
                        help.commandSpec().options(),
                        help.commandSpec().positionalParameters(),
                        help.colorScheme())
                val keyLength =
                    help
                        .commandSpec()
                        .usageMessage()
                        .longOptionsMaxWidth()
                        .coerceAtMost(calcLen - 1)
                val table =
                    TextTable.forColumns(
                        help.colorScheme(),
                        Column(keyLength + 7, 6, Column.Overflow.SPAN),
                        Column(width(help) - (keyLength + 7), 0, Column.Overflow.WRAP))
                table.setAdjustLineBreaksForWideCJKCharacters(adjustCJK(help))
                table.addRowValues("@|yellow ${argument.first}|@", argument.second ?: "")
                table.toString()
            }
        }

        private fun adjustCJK(help: Help) =
            help.commandSpec().usageMessage().adjustLineBreaksForWideCJKCharacters()

        private fun width(help: Help) = help.commandSpec().usageMessage().width()
    }

    fun installIORenderer(cmd: CommandLine) {
        val inputName = getResString(cmd, "standardInput")
        if (inputName != null) {
            cmd.helpSectionMap[SECTION_KEY_STANDARD_INPUT_HEADING] = IHelpSectionRenderer {
                getResString(cmd, "standardInputHeading")
            }
            cmd.helpSectionMap[SECTION_KEY_STANDARD_INPUT_DETAILS] =
                InputOutputHelpSectionRenderer(
                    inputName to getResString(cmd, "standardInputDescription"))
            cmd.helpSectionKeys =
                insertKey(
                    cmd.helpSectionKeys,
                    SECTION_KEY_STANDARD_INPUT_HEADING,
                    SECTION_KEY_STANDARD_INPUT_DETAILS)
        }

        val outputName = getResString(cmd, "standardOutput")
        if (outputName != null) {
            cmd.helpSectionMap[SECTION_KEY_STANDARD_OUTPUT_HEADING] = IHelpSectionRenderer {
                getResString(cmd, "standardOutputHeading")
            }
            cmd.helpSectionMap[SECTION_KEY_STANDARD_OUTPUT_DETAILS] =
                InputOutputHelpSectionRenderer(
                    outputName to getResString(cmd, "standardOutputDescription"))
            cmd.helpSectionKeys =
                insertKey(
                    cmd.helpSectionKeys,
                    SECTION_KEY_STANDARD_OUTPUT_HEADING,
                    SECTION_KEY_STANDARD_OUTPUT_DETAILS)
        }
    }

    private fun insertKey(keys: List<String>, header: String, details: String): List<String> {
        val index =
            keys.indexOf(CommandLine.Model.UsageMessageSpec.SECTION_KEY_EXIT_CODE_LIST_HEADING)
        val result = keys.toMutableList()
        result.add(index, header)
        result.add(index + 1, details)
        return result
    }

    private fun getResString(cmd: CommandLine, key: String): String? =
        try {
                cmd.resourceBundle.getString(key)
            } catch (m: MissingResourceException) {
                try {
                    cmd.parent.resourceBundle.getString(key)
                } catch (m: MissingResourceException) {
                    null
                }
            }
            ?.let { String.format(it) }

    companion object {
        const val PRFX_ENV = "@ENV:"

        const val PRFX_FD = "@FD:"

        const val SECTION_KEY_STANDARD_INPUT_HEADING = "standardInputHeading"
        const val SECTION_KEY_STANDARD_INPUT_DETAILS = "standardInput"
        const val SECTION_KEY_STANDARD_OUTPUT_HEADING = "standardOutputHeading"
        const val SECTION_KEY_STANDARD_OUTPUT_DETAILS = "standardOutput"

        @JvmField val DAWN_OF_TIME = Date(0)

        @JvmField
        @Deprecated("Replace with DAWN_OF_TIME", ReplaceWith("DAWN_OF_TIME"))
        val BEGINNING_OF_TIME = DAWN_OF_TIME

        @JvmField val END_OF_TIME = Date(8640000000000000L)

        @JvmField val PATTERN_FD = "^\\d{1,20}$".toPattern()

        @Throws(IOException::class)
        @JvmStatic
        fun stringFromInputStream(inputStream: InputStream): String {
            return inputStream.use { input ->
                val byteOut = ByteArrayOutputStream()
                val buf = ByteArray(4096)
                var read: Int
                while (input.read(buf).also { read = it } != -1) {
                    byteOut.write(buf, 0, read)
                }
                // TODO: For decrypt operations we MUST accept non-UTF8 passwords
                decodeUTF8(byteOut.toByteArray())
            }
        }
    }
}
