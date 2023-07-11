// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import sop.Ready;
import sop.SOP;
import sop.exception.SOPGPException;
import sop.external.operation.ArmorExternal;
import sop.external.operation.DearmorExternal;
import sop.external.operation.DecryptExternal;
import sop.external.operation.DetachedSignExternal;
import sop.external.operation.DetachedVerifyExternal;
import sop.external.operation.EncryptExternal;
import sop.external.operation.ExtractCertExternal;
import sop.external.operation.GenerateKeyExternal;
import sop.external.operation.InlineDetachExternal;
import sop.external.operation.InlineSignExternal;
import sop.external.operation.InlineVerifyExternal;
import sop.external.operation.ListProfilesExternal;
import sop.external.operation.VersionExternal;
import sop.operation.Armor;
import sop.operation.Dearmor;
import sop.operation.Decrypt;
import sop.operation.DetachedSign;
import sop.operation.DetachedVerify;
import sop.operation.Encrypt;
import sop.operation.ExtractCert;
import sop.operation.GenerateKey;
import sop.operation.InlineDetach;
import sop.operation.InlineSign;
import sop.operation.InlineVerify;
import sop.operation.ListProfiles;
import sop.operation.RevokeKey;
import sop.operation.Version;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of the {@link SOP} API using an external SOP binary.
 */
public class ExternalSOP implements SOP {

    private final String binaryName;
    private final Properties properties;
    private final TempDirProvider tempDirProvider;

    /**
     * Instantiate an {@link ExternalSOP} object for the given binary and pass it empty environment variables,
     * as well as a default {@link TempDirProvider}.
     *
     * @param binaryName name / path of the SOP binary
     */
    public ExternalSOP(@Nonnull String binaryName) {
        this(binaryName, new Properties());
    }

    /**
     * Instantiate an {@link ExternalSOP} object for the given binary, and pass it the given properties as
     * environment variables, as well as a default {@link TempDirProvider}.
     *
     * @param binaryName name / path of the SOP binary
     * @param properties environment variables
     */
    public ExternalSOP(@Nonnull String binaryName, @Nonnull Properties properties) {
        this(binaryName, properties, defaultTempDirProvider());
    }

    /**
     * Instantiate an {@link ExternalSOP} object for the given binary and the given {@link TempDirProvider}
     * using empty environment variables.
     *
     * @param binaryName name / path of the SOP binary
     * @param tempDirProvider custom tempDirProvider
     */
    public ExternalSOP(@Nonnull String binaryName, @Nonnull TempDirProvider tempDirProvider) {
        this(binaryName, new Properties(), tempDirProvider);
    }

    /**
     * Instantiate an {@link ExternalSOP} object for the given binary using the given properties and
     * custom {@link TempDirProvider}.
     *
     * @param binaryName name / path of the SOP binary
     * @param properties environment variables
     * @param tempDirProvider tempDirProvider
     */
    public ExternalSOP(@Nonnull String binaryName, @Nonnull Properties properties, @Nonnull TempDirProvider tempDirProvider) {
        this.binaryName = binaryName;
        this.properties = properties;
        this.tempDirProvider = tempDirProvider;
    }

    @Override
    public Version version() {
        return new VersionExternal(binaryName, properties);
    }

    @Override
    public GenerateKey generateKey() {
        return new GenerateKeyExternal(binaryName, properties);
    }

    @Override
    public ExtractCert extractCert() {
        return new ExtractCertExternal(binaryName, properties);
    }

    @Override
    public DetachedSign detachedSign() {
        return new DetachedSignExternal(binaryName, properties, tempDirProvider);
    }

    @Override
    public InlineSign inlineSign() {
        return new InlineSignExternal(binaryName, properties);
    }

    @Override
    public DetachedVerify detachedVerify() {
        return new DetachedVerifyExternal(binaryName, properties);
    }

    @Override
    public InlineVerify inlineVerify() {
        return new InlineVerifyExternal(binaryName, properties, tempDirProvider);
    }

    @Override
    public InlineDetach inlineDetach() {
        return new InlineDetachExternal(binaryName, properties, tempDirProvider);
    }

    @Override
    public Encrypt encrypt() {
        return new EncryptExternal(binaryName, properties);
    }

    @Override
    public Decrypt decrypt() {
        return new DecryptExternal(binaryName, properties, tempDirProvider);
    }

    @Override
    public Armor armor() {
        return new ArmorExternal(binaryName, properties);
    }

    @Override
    public ListProfiles listProfiles() {
        return new ListProfilesExternal(binaryName, properties);
    }

    @Override
    public RevokeKey revokeKey() {
        return null;
    }

    @Override
    public Dearmor dearmor() {
        return new DearmorExternal(binaryName, properties);
    }

    public static void finish(@Nonnull Process process) throws IOException {
        try {
            mapExitCodeOrException(process);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wait for the {@link Process} to finish and read out its exit code.
     * If the exit code is {@value "0"}, this method just returns.
     * Otherwise, the exit code gets mapped to a {@link SOPGPException} which then gets thrown.
     * If the exit code does not match any of the known exit codes defined in the SOP specification,
     * this method throws a {@link RuntimeException} instead.
     *
     * @param process process
     * @throws InterruptedException if the thread is interrupted before the process could exit
     * @throws IOException in case of an IO error
     */
    private static void mapExitCodeOrException(@Nonnull Process process) throws InterruptedException, IOException {
        // wait for process termination
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            // we're good, bye
            return;
        }

        // Read error message
        InputStream errIn = process.getErrorStream();
        String errorMessage = readString(errIn);

        switch (exitCode) {
            case SOPGPException.NoSignature.EXIT_CODE:
                throw new SOPGPException.NoSignature("External SOP backend reported error NoSignature (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.UnsupportedAsymmetricAlgo.EXIT_CODE:
                throw new UnsupportedOperationException("External SOP backend reported error UnsupportedAsymmetricAlgo (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.CertCannotEncrypt.EXIT_CODE:
                throw new SOPGPException.CertCannotEncrypt("External SOP backend reported error CertCannotEncrypt (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.MissingArg.EXIT_CODE:
                throw new SOPGPException.MissingArg("External SOP backend reported error MissingArg (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.IncompleteVerification.EXIT_CODE:
                throw new SOPGPException.IncompleteVerification("External SOP backend reported error IncompleteVerification (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.CannotDecrypt.EXIT_CODE:
                throw new SOPGPException.CannotDecrypt("External SOP backend reported error CannotDecrypt (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.PasswordNotHumanReadable.EXIT_CODE:
                throw new SOPGPException.PasswordNotHumanReadable("External SOP backend reported error PasswordNotHumanReadable (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.UnsupportedOption.EXIT_CODE:
                throw new SOPGPException.UnsupportedOption("External SOP backend reported error UnsupportedOption (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.BadData.EXIT_CODE:
                throw new SOPGPException.BadData("External SOP backend reported error BadData (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.ExpectedText.EXIT_CODE:
                throw new SOPGPException.ExpectedText("External SOP backend reported error ExpectedText (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.OutputExists.EXIT_CODE:
                throw new SOPGPException.OutputExists("External SOP backend reported error OutputExists (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.MissingInput.EXIT_CODE:
                throw new SOPGPException.MissingInput("External SOP backend reported error MissingInput (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.KeyIsProtected.EXIT_CODE:
                throw new SOPGPException.KeyIsProtected("External SOP backend reported error KeyIsProtected (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.UnsupportedSubcommand.EXIT_CODE:
                throw new SOPGPException.UnsupportedSubcommand("External SOP backend reported error UnsupportedSubcommand (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.UnsupportedSpecialPrefix.EXIT_CODE:
                throw new SOPGPException.UnsupportedSpecialPrefix("External SOP backend reported error UnsupportedSpecialPrefix (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.AmbiguousInput.EXIT_CODE:
                throw new SOPGPException.AmbiguousInput("External SOP backend reported error AmbiguousInput (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.KeyCannotSign.EXIT_CODE:
                throw new SOPGPException.KeyCannotSign("External SOP backend reported error KeyCannotSign (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.IncompatibleOptions.EXIT_CODE:
                throw new SOPGPException.IncompatibleOptions("External SOP backend reported error IncompatibleOptions (" +
                        exitCode + "):\n" + errorMessage);

            case SOPGPException.UnsupportedProfile.EXIT_CODE:
                throw new SOPGPException.UnsupportedProfile("External SOP backend reported error UnsupportedProfile (" +
                        exitCode + "):\n" + errorMessage);

            default:
                // Did you forget to add a case for a new exception type?
                throw new RuntimeException("External SOP backend reported unknown exit code (" +
                        exitCode + "):\n" + errorMessage);
        }
    }

    /**
     * Return all key-value pairs from the given {@link Properties} object as a list with items of the form
     * <pre>key=value</pre>.
     *
     * @param properties properties
     * @return list of key=value strings
     */
    public static List<String> propertiesToEnv(@Nonnull Properties properties) {
        List<String> env = new ArrayList<>();
        for (Object key : properties.keySet()) {
            env.add(key + "=" + properties.get(key));
        }
        return env;
    }

    /**
     * Read the contents of the {@link InputStream} and return them as a {@link String}.
     *
     * @param inputStream input stream
     * @return string
     * @throws IOException in case of an IO error
     */
    public static String readString(@Nonnull InputStream inputStream) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int r;
        while ((r = inputStream.read(buf)) > 0) {
            bOut.write(buf, 0, r);
        }
        return bOut.toString();
    }

    /**
     * Execute the given command on the given {@link Runtime} with the given list of environment variables.
     * This command does not transform any input data, and instead is purely a producer.
     *
     * @param runtime runtime
     * @param commandList command
     * @param envList environment variables
     * @return ready to read the result from
     */
    public static Ready executeProducingOperation(@Nonnull Runtime runtime,
                                                  @Nonnull List<String> commandList,
                                                  @Nonnull List<String> envList) {
        String[] command = commandList.toArray(new String[0]);
        String[] env = envList.toArray(new String[0]);

        try {
            Process process = runtime.exec(command, env);
            InputStream stdIn = process.getInputStream();

            return new Ready() {
                @Override
                public void writeTo(OutputStream outputStream) throws IOException {
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = stdIn.read(buf)) >= 0) {
                        outputStream.write(buf, 0, r);
                    }

                    outputStream.flush();
                    outputStream.close();

                    ExternalSOP.finish(process);
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute the given command on the given runtime using the given environment variables.
     * The given input stream provides input for the process.
     * This command is a transformation, meaning it is given input data and transforms it into output data.
     *
     * @param runtime runtime
     * @param commandList command
     * @param envList environment variables
     * @param standardIn stream of input data for the process
     * @return ready to read the result from
     */
    public static Ready executeTransformingOperation(@Nonnull Runtime runtime, @Nonnull List<String> commandList, @Nonnull List<String> envList, @Nonnull InputStream standardIn) {
        String[] command = commandList.toArray(new String[0]);
        String[] env = envList.toArray(new String[0]);
        try {
            Process process = runtime.exec(command, env);
            OutputStream processOut = process.getOutputStream();
            InputStream processIn = process.getInputStream();

            return new Ready() {
                @Override
                public void writeTo(OutputStream outputStream) throws IOException {
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = standardIn.read(buf)) > 0) {
                        processOut.write(buf, 0, r);
                    }
                    standardIn.close();

                    try {
                        processOut.flush();
                        processOut.close();
                    } catch (IOException e) {
                        // Perhaps the stream is already closed, in which case we ignore the exception.
                        if (!"Stream closed".equals(e.getMessage())) {
                            throw e;
                        }
                    }

                    while ((r = processIn.read(buf)) > 0) {
                        outputStream.write(buf, 0 , r);
                    }
                    processIn.close();

                    outputStream.flush();
                    outputStream.close();

                    finish(process);
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This interface can be used to provide a directory in which external SOP binaries can temporarily store
     * additional results of OpenPGP operations such that the binding classes can parse them out from there.
     * Unfortunately, on Java you cannot open {@link java.io.FileDescriptor FileDescriptors} arbitrarily, so we
     * have to rely on temporary files to pass results.
     * An example:
     * <pre>sop decrypt</pre> can emit signature verifications via <pre>--verify-out=/path/to/tempfile</pre>.
     * {@link DecryptExternal} will then parse the temp file to make the result available to consumers.
     * Temporary files are deleted after being read, yet creating temp files for sensitive information on disk
     * might pose a security risk. Use with care!
     */
    public interface TempDirProvider {
        File provideTempDirectory() throws IOException;
    }

    /**
     * Default implementation of the {@link TempDirProvider} which stores temporary files in the systems temp dir
     * ({@link Files#createTempDirectory(String, FileAttribute[])}).
     *
     * @return default implementation
     */
    public static TempDirProvider defaultTempDirProvider() {
        return new TempDirProvider() {
            @Override
            public File provideTempDirectory() throws IOException {
                return Files.createTempDirectory("ext-sop").toFile();
            }
        };
    }
}
