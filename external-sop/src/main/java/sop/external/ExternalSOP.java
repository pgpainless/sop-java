// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import sop.SOP;
import sop.exception.SOPGPException;
import sop.external.operation.ExtractCertExternal;
import sop.external.operation.GenerateKeyExternal;
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
import sop.operation.Version;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class ExternalSOP implements SOP {

    private final String binaryName;
    private final Properties properties;

    public ExternalSOP(String binaryName) {
        this(binaryName, new Properties());
    }

    public ExternalSOP(String binaryName, Properties properties) {
        this.binaryName = binaryName;
        this.properties = properties;
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
        return null;
    }

    @Override
    public InlineSign inlineSign() {
        return null;
    }

    @Override
    public DetachedVerify detachedVerify() {
        return null;
    }

    @Override
    public InlineVerify inlineVerify() {
        return null;
    }

    @Override
    public InlineDetach inlineDetach() {
        return null;
    }

    @Override
    public Encrypt encrypt() {
        return null;
    }

    @Override
    public Decrypt decrypt() {
        return null;
    }

    @Override
    public Armor armor() {
        return null;
    }

    @Override
    public Dearmor dearmor() {
        return null;
    }

    public static void finish(Process process) throws IOException {
        try {
            mapExitCodeOrException(process);
        } catch (SOPGPException e) {
            InputStream errIn = process.getErrorStream();
            ByteArrayOutputStream errOut = new ByteArrayOutputStream();
            byte[] buf = new byte[512];
            int r;
            while ((r = errIn.read(buf)) > 0 ) {
                errOut.write(buf, 0, r);
            }

            e.initCause(new IOException(errOut.toString()));
            throw e;
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mapExitCodeOrException(Process process) throws InterruptedException, IOException {
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            return;
        }

        InputStream errIn = process.getErrorStream();
        ByteArrayOutputStream errOut = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int r;
        while ((r = errIn.read(buf)) > 0 ) {
            errOut.write(buf, 0, r);
        }

        String errorMessage = errOut.toString();

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

            default:
                throw new RuntimeException("External SOP backend reported unknown exit code (" +
                        exitCode + "):\n" + errorMessage);
        }
    }

    public static List<String> propertiesToEnv(Properties properties) {
        List<String> env = new ArrayList<>();
        for (Object key : properties.keySet()) {
            env.add(key + "=" + properties.get(key));
        }
        return env;
    }
}
