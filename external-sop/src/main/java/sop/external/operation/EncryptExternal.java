// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.EncryptionResult;
import sop.ReadyWithResult;
import sop.SessionKey;
import sop.enums.EncryptAs;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.Encrypt;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of the {@link Encrypt} operation using an external SOP binary.
 */
public class EncryptExternal implements Encrypt {

    private final ExternalSOP.TempDirProvider tempDirProvider;
    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;
    private int SIGN_WITH_COUNTER = 0;
    private int KEY_PASSWORD_COUNTER = 0;
    private int PASSWORD_COUNTER = 0;
    private int CERT_COUNTER = 0;

    public EncryptExternal(String binary, Properties environment, ExternalSOP.TempDirProvider tempDirProvider) {
        this.tempDirProvider = tempDirProvider;
        this.commandList.add(binary);
        this.commandList.add("encrypt");
        this.envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    @Nonnull
    public Encrypt noArmor() {
        this.commandList.add("--no-armor");
        return this;
    }

    @Override
    @Nonnull
    public Encrypt mode(@Nonnull EncryptAs mode)
            throws SOPGPException.UnsupportedOption {
        this.commandList.add("--as=" + mode);
        return this;
    }

    @Override
    @Nonnull
    public Encrypt signWith(@Nonnull InputStream key)
            throws SOPGPException.KeyCannotSign, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.BadData,
            IOException {
        String envVar = "SIGN_WITH_" + SIGN_WITH_COUNTER++;
        commandList.add("--sign-with=@ENV:" + envVar);
        envList.add(envVar + "=" + ExternalSOP.readString(key));
        return this;
    }

    @Override
    @Nonnull
    public Encrypt withKeyPassword(@Nonnull byte[] password)
            throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption {
        String envVar = "KEY_PASSWORD_" + KEY_PASSWORD_COUNTER++;
        commandList.add("--with-key-password=@ENV:" + envVar);
        envList.add(envVar + "=" + new String(password));
        return this;
    }

    @Override
    @Nonnull
    public Encrypt withPassword(@Nonnull String password)
            throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption {
        String envVar = "PASSWORD_" + PASSWORD_COUNTER++;
        commandList.add("--with-password=@ENV:" + envVar);
        envList.add(envVar + "=" + password);
        return this;
    }

    @Override
    @Nonnull
    public Encrypt withCert(@Nonnull InputStream cert)
            throws SOPGPException.CertCannotEncrypt, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.BadData,
            IOException {
        String envVar = "CERT_" + CERT_COUNTER++;
        commandList.add("@ENV:" + envVar);
        envList.add(envVar + "=" + ExternalSOP.readString(cert));
        return this;
    }

    @Override
    @Nonnull
    public Encrypt profile(@Nonnull String profileName) {
        commandList.add("--profile=" + profileName);
        return this;
    }

    @Override
    @Nonnull
    public ReadyWithResult<EncryptionResult> plaintext(@Nonnull InputStream plaintext)
            throws SOPGPException.KeyIsProtected, IOException {
        File tempDir = tempDirProvider.provideTempDirectory();

        File sessionKeyOut = new File(tempDir, "session-key-out");
        sessionKeyOut.delete();
        commandList.add("--session-key-out=" + sessionKeyOut.getAbsolutePath());

        String[] command = commandList.toArray(new String[0]);
        String[] env = envList.toArray(new String[0]);
        try {
            Process process = Runtime.getRuntime().exec(command, env);
            OutputStream processOut = process.getOutputStream();
            InputStream processIn = process.getInputStream();

            return new ReadyWithResult<EncryptionResult>() {
                @Override
                public EncryptionResult writeTo(@Nonnull OutputStream outputStream) throws IOException {
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = plaintext.read(buf)) > 0) {
                        processOut.write(buf, 0, r);
                    }

                    plaintext.close();
                    processOut.close();

                    while ((r = processIn.read(buf)) > 0) {
                        outputStream.write(buf, 0 , r);
                    }

                    processIn.close();
                    outputStream.close();

                    ExternalSOP.finish(process);

                    FileInputStream sessionKeyOutIn = new FileInputStream(sessionKeyOut);
                    String line = ExternalSOP.readString(sessionKeyOutIn);
                    SessionKey sessionKey = SessionKey.fromString(line.trim());
                    sessionKeyOutIn.close();
                    sessionKeyOut.delete();

                    return new EncryptionResult(sessionKey);
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
