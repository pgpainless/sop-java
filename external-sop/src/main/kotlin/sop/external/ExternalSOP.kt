// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external

import java.io.*
import java.nio.file.Files
import java.util.*
import javax.annotation.Nonnull
import sop.Ready
import sop.SOP
import sop.exception.SOPGPException.*
import sop.external.ExternalSOP.TempDirProvider
import sop.external.operation.*
import sop.operation.*

/**
 * Implementation of the [SOP] API using an external SOP binary.
 *
 * Instantiate an [ExternalSOP] object for the given binary and the given [TempDirProvider] using
 * empty environment variables.
 *
 * @param binaryName name / path of the SOP binary
 * @param tempDirProvider custom tempDirProvider
 */
class ExternalSOP(
    private val binaryName: String,
    private val properties: Properties = Properties(),
    private val tempDirProvider: TempDirProvider = defaultTempDirProvider()
) : SOP {

    constructor(
        binaryName: String,
        properties: Properties
    ) : this(binaryName, properties, defaultTempDirProvider())

    override fun version(): Version = VersionExternal(binaryName, properties)

    override fun generateKey(): GenerateKey = GenerateKeyExternal(binaryName, properties)

    override fun extractCert(): ExtractCert = ExtractCertExternal(binaryName, properties)

    override fun detachedSign(): DetachedSign =
        DetachedSignExternal(binaryName, properties, tempDirProvider)

    override fun inlineSign(): InlineSign = InlineSignExternal(binaryName, properties)

    override fun detachedVerify(): DetachedVerify = DetachedVerifyExternal(binaryName, properties)

    override fun inlineVerify(): InlineVerify =
        InlineVerifyExternal(binaryName, properties, tempDirProvider)

    override fun inlineDetach(): InlineDetach =
        InlineDetachExternal(binaryName, properties, tempDirProvider)

    override fun encrypt(): Encrypt = EncryptExternal(binaryName, properties, tempDirProvider)

    override fun decrypt(): Decrypt = DecryptExternal(binaryName, properties, tempDirProvider)

    override fun armor(): Armor = ArmorExternal(binaryName, properties)

    override fun dearmor(): Dearmor = DearmorExternal(binaryName, properties)

    override fun listProfiles(): ListProfiles = ListProfilesExternal(binaryName, properties)

    override fun revokeKey(): RevokeKey = RevokeKeyExternal(binaryName, properties)

    override fun changeKeyPassword(): ChangeKeyPassword =
        ChangeKeyPasswordExternal(binaryName, properties)

    override fun updateKey(): UpdateKey = UpdateKeyExternal(binaryName, properties)

    override fun mergeCerts(): MergeCerts = MergeCertsExternal(binaryName, properties)

    override fun certifyUserId(): CertifyUserId = CertifyUserIdExternal(binaryName, properties)

    override fun validateUserId(): ValidateUserId = ValidateUserIdExternal(binaryName, properties)

    /**
     * This interface can be used to provide a directory in which external SOP binaries can
     * temporarily store additional results of OpenPGP operations such that the binding classes can
     * parse them out from there. Unfortunately, on Java you cannot open
     * [FileDescriptors][java.io.FileDescriptor] arbitrarily, so we have to rely on temporary files
     * to pass results. An example: `sop decrypt` can emit signature verifications via
     * `--verify-out=/path/to/tempfile`. [DecryptExternal] will then parse the temp file to make the
     * result available to consumers. Temporary files are deleted after being read, yet creating
     * temp files for sensitive information on disk might pose a security risk. Use with care!
     */
    fun interface TempDirProvider {

        @Throws(IOException::class) fun provideTempDirectory(): File
    }

    companion object {

        @JvmStatic
        @Throws(IOException::class)
        fun finish(process: Process) {
            try {
                mapExitCodeOrException(process)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        @Throws(InterruptedException::class, IOException::class)
        private fun mapExitCodeOrException(process: Process) {
            // wait for process termination
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                // we're good, bye
                return
            }

            // Read error message
            val errIn = process.errorStream
            val errorMessage = readString(errIn)

            when (exitCode) {
                NoSignature.EXIT_CODE ->
                    throw NoSignature(
                        "External SOP backend reported error NoSignature ($exitCode):\n$errorMessage")
                UnsupportedAsymmetricAlgo.EXIT_CODE ->
                    throw UnsupportedOperationException(
                        "External SOP backend reported error UnsupportedAsymmetricAlgo ($exitCode):\n$errorMessage")
                CertCannotEncrypt.EXIT_CODE ->
                    throw CertCannotEncrypt(
                        "External SOP backend reported error CertCannotEncrypt ($exitCode):\n$errorMessage")
                MissingArg.EXIT_CODE ->
                    throw MissingArg(
                        "External SOP backend reported error MissingArg ($exitCode):\n$errorMessage")
                IncompleteVerification.EXIT_CODE ->
                    throw IncompleteVerification(
                        "External SOP backend reported error IncompleteVerification ($exitCode):\n$errorMessage")
                CannotDecrypt.EXIT_CODE ->
                    throw CannotDecrypt(
                        "External SOP backend reported error CannotDecrypt ($exitCode):\n$errorMessage")
                PasswordNotHumanReadable.EXIT_CODE ->
                    throw PasswordNotHumanReadable(
                        "External SOP backend reported error PasswordNotHumanReadable ($exitCode):\n$errorMessage")
                UnsupportedOption.EXIT_CODE ->
                    throw UnsupportedOption(
                        "External SOP backend reported error UnsupportedOption ($exitCode):\n$errorMessage")
                BadData.EXIT_CODE ->
                    throw BadData(
                        "External SOP backend reported error BadData ($exitCode):\n$errorMessage")
                ExpectedText.EXIT_CODE ->
                    throw ExpectedText(
                        "External SOP backend reported error ExpectedText ($exitCode):\n$errorMessage")
                OutputExists.EXIT_CODE ->
                    throw OutputExists(
                        "External SOP backend reported error OutputExists ($exitCode):\n$errorMessage")
                MissingInput.EXIT_CODE ->
                    throw MissingInput(
                        "External SOP backend reported error MissingInput ($exitCode):\n$errorMessage")
                KeyIsProtected.EXIT_CODE ->
                    throw KeyIsProtected(
                        "External SOP backend reported error KeyIsProtected ($exitCode):\n$errorMessage")
                UnsupportedSubcommand.EXIT_CODE ->
                    throw UnsupportedSubcommand(
                        "External SOP backend reported error UnsupportedSubcommand ($exitCode):\n$errorMessage")
                UnsupportedSpecialPrefix.EXIT_CODE ->
                    throw UnsupportedSpecialPrefix(
                        "External SOP backend reported error UnsupportedSpecialPrefix ($exitCode):\n$errorMessage")
                AmbiguousInput.EXIT_CODE ->
                    throw AmbiguousInput(
                        "External SOP backend reported error AmbiguousInput ($exitCode):\n$errorMessage")
                KeyCannotSign.EXIT_CODE ->
                    throw KeyCannotSign(
                        "External SOP backend reported error KeyCannotSign ($exitCode):\n$errorMessage")
                IncompatibleOptions.EXIT_CODE ->
                    throw IncompatibleOptions(
                        "External SOP backend reported error IncompatibleOptions ($exitCode):\n$errorMessage")
                UnsupportedProfile.EXIT_CODE ->
                    throw UnsupportedProfile(
                        "External SOP backend reported error UnsupportedProfile ($exitCode):\n$errorMessage")
                NoHardwareKeyFound.EXIT_CODE ->
                    throw NoHardwareKeyFound(
                        "External SOP backend reported error NoHardwareKeyFound ($exitCode):\n$errorMessage")
                HardwareKeyFailure.EXIT_CODE ->
                    throw HardwareKeyFailure(
                        "External SOP backend reported error HardwareKeyFailure ($exitCode):\n$errorMessage")
                PrimaryKeyBad.EXIT_CODE ->
                    throw PrimaryKeyBad(
                        "External SOP backend reported error PrimaryKeyBad ($exitCode):\n$errorMessage")
                CertUserIdNoMatch.EXIT_CODE ->
                    throw CertUserIdNoMatch(
                        "External SOP backend reported error CertUserIdNoMatch ($exitCode):\n$errorMessage")

                // Did you forget to add a case for a new exception type?
                else ->
                    throw RuntimeException(
                        "External SOP backend reported unknown exit code ($exitCode):\n$errorMessage")
            }
        }

        /**
         * Return all key-value pairs from the given [Properties] object as a list with items of the
         * form `key=value`.
         *
         * @param properties properties
         * @return list of key=value strings
         */
        @JvmStatic
        fun propertiesToEnv(properties: Properties): List<String> =
            properties.map { "${it.key}=${it.value}" }

        /**
         * Read the contents of the [InputStream] and return them as a [String].
         *
         * @param inputStream input stream
         * @return string
         * @throws IOException in case of an IO error
         */
        @JvmStatic
        @Throws(IOException::class)
        fun readString(inputStream: InputStream): String {
            val bOut = ByteArrayOutputStream()
            val buf = ByteArray(4096)
            var r: Int
            while (inputStream.read(buf).also { r = it } > 0) {
                bOut.write(buf, 0, r)
            }
            return bOut.toString()
        }

        /**
         * Execute the given command on the given [Runtime] with the given list of environment
         * variables. This command does not transform any input data, and instead is purely a
         * producer.
         *
         * @param runtime runtime
         * @param commandList command
         * @param envList environment variables
         * @return ready to read the result from
         */
        @JvmStatic
        fun executeProducingOperation(
            runtime: Runtime,
            commandList: List<String>,
            envList: List<String>
        ): Ready {
            try {
                val process = runtime.exec(commandList.toTypedArray(), envList.toTypedArray())
                val stdIn = process.inputStream

                return object : Ready() {
                    @Throws(IOException::class)
                    override fun writeTo(@Nonnull outputStream: OutputStream) {
                        val buf = ByteArray(4096)
                        var r: Int
                        while (stdIn.read(buf).also { r = it } >= 0) {
                            outputStream.write(buf, 0, r)
                        }
                        outputStream.flush()
                        outputStream.close()
                        finish(process)
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        /**
         * Execute the given command on the given runtime using the given environment variables. The
         * given input stream provides input for the process. This command is a transformation,
         * meaning it is given input data and transforms it into output data.
         *
         * @param runtime runtime
         * @param commandList command
         * @param envList environment variables
         * @param standardIn stream of input data for the process
         * @return ready to read the result from
         */
        @JvmStatic
        fun executeTransformingOperation(
            runtime: Runtime,
            commandList: List<String>,
            envList: List<String>,
            standardIn: InputStream
        ): Ready {
            try {
                val process = runtime.exec(commandList.toTypedArray(), envList.toTypedArray())
                val processOut = process.outputStream
                val processIn = process.inputStream

                return object : Ready() {
                    override fun writeTo(outputStream: OutputStream) {
                        val buf = ByteArray(4096)
                        var r: Int
                        while (standardIn.read(buf).also { r = it } > 0) {
                            processOut.write(buf, 0, r)
                        }
                        standardIn.close()

                        try {
                            processOut.flush()
                            processOut.close()
                        } catch (e: IOException) {
                            // Perhaps the stream is already closed, in which case we ignore the
                            // exception.
                            if ("Stream closed" != e.message) {
                                throw e
                            }
                        }

                        while (processIn.read(buf).also { r = it } > 0) {
                            outputStream.write(buf, 0, r)
                        }
                        processIn.close()

                        outputStream.flush()
                        outputStream.close()

                        finish(process)
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        /**
         * Default implementation of the [TempDirProvider] which stores temporary files in the
         * systems temp dir ([Files.createTempDirectory]).
         *
         * @return default implementation
         */
        @JvmStatic
        fun defaultTempDirProvider(): TempDirProvider {
            return TempDirProvider { Files.createTempDirectory("ext-sop").toFile() }
        }
    }
}
