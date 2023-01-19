// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.DecryptionResult;
import sop.ReadyWithResult;
import sop.SessionKey;
import sop.Verification;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.Decrypt;
import sop.util.UTCUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of the {@link Decrypt} operation using an external SOP binary.
 */
public class DecryptExternal implements Decrypt {

    private final ExternalSOP.TempDirProvider tempDirProvider;
    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    private int verifyWithCounter = 0;
    private int withSessionKeyCounter = 0;
    private int withPasswordCounter = 0;
    private int keyCounter = 0;
    private int withKeyPasswordCounter = 0;

    public DecryptExternal(String binary, Properties environment, ExternalSOP.TempDirProvider tempDirProvider) {
        this.tempDirProvider = tempDirProvider;
        this.commandList.add(binary);
        this.commandList.add("decrypt");
        this.envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    public Decrypt verifyNotBefore(Date timestamp)
            throws SOPGPException.UnsupportedOption {
        this.commandList.add("--verify-not-before=" + UTCUtil.formatUTCDate(timestamp));
        return this;
    }

    @Override
    public Decrypt verifyNotAfter(Date timestamp)
            throws SOPGPException.UnsupportedOption {
        this.commandList.add("--verify-not-after=" + UTCUtil.formatUTCDate(timestamp));
        return this;
    }

    @Override
    public Decrypt verifyWithCert(InputStream cert)
            throws SOPGPException.BadData, SOPGPException.UnsupportedAsymmetricAlgo, IOException {
        String envVar = "VERIFY_WITH_" + verifyWithCounter++;
        commandList.add("--verify-with=@ENV:" + envVar);
        envList.add(envVar + "=" + ExternalSOP.readFully(cert));
        return this;
    }

    @Override
    public Decrypt withSessionKey(SessionKey sessionKey)
            throws SOPGPException.UnsupportedOption {
        String envVar = "SESSION_KEY_" + withSessionKeyCounter++;
        commandList.add("--with-session-key=@ENV:" + envVar);
        envList.add(envVar + "=" + sessionKey);
        return this;
    }

    @Override
    public Decrypt withPassword(String password)
            throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption {
        String envVar = "PASSWORD_" + withPasswordCounter++;
        commandList.add("--with-password=@ENV:" + envVar);
        envList.add(envVar + "=" + password);
        return this;
    }

    @Override
    public Decrypt withKey(InputStream key)
            throws SOPGPException.BadData, SOPGPException.UnsupportedAsymmetricAlgo, IOException {
        String envVar = "KEY_" + keyCounter++;
        commandList.add("@ENV:" + envVar);
        envList.add(envVar + "=" + ExternalSOP.readFully(key));
        return this;
    }

    @Override
    public Decrypt withKeyPassword(byte[] password)
            throws SOPGPException.UnsupportedOption, SOPGPException.PasswordNotHumanReadable {
        String envVar = "KEY_PASSWORD_" + withKeyPasswordCounter++;
        commandList.add("--with-key-password=@ENV:" + envVar);
        envList.add(envVar + "=" + new String(password));
        return this;
    }

    @Override
    public ReadyWithResult<DecryptionResult> ciphertext(InputStream ciphertext)
            throws SOPGPException.BadData, SOPGPException.MissingArg, SOPGPException.CannotDecrypt,
            SOPGPException.KeyIsProtected, IOException {
        File tempDir = tempDirProvider.provideTempDirectory();

        File sessionKeyOut = new File(tempDir, "session-key-out");
        sessionKeyOut.delete();
        commandList.add("--session-key-out=" + sessionKeyOut.getAbsolutePath());

        File verifyOut = new File(tempDir, "verifications-out");
        verifyOut.delete();
        if (verifyWithCounter != 0) {
            commandList.add("--verify-out=" + verifyOut.getAbsolutePath());
        }

        String[] command = commandList.toArray(new String[0]);
        String[] env = envList.toArray(new String[0]);
        try {
            Process process = Runtime.getRuntime().exec(command, env);
            OutputStream processOut = process.getOutputStream();
            InputStream processIn = process.getInputStream();

            return new ReadyWithResult<DecryptionResult>() {
                @Override
                public DecryptionResult writeTo(OutputStream outputStream) throws IOException {
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = ciphertext.read(buf)) > 0) {
                        processOut.write(buf, 0, r);
                    }

                    ciphertext.close();
                    processOut.close();

                    while ((r = processIn.read(buf)) > 0) {
                        outputStream.write(buf, 0 , r);
                    }

                    processIn.close();
                    outputStream.close();

                    ExternalSOP.finish(process);

                    FileInputStream sessionKeyOutIn = new FileInputStream(sessionKeyOut);
                    String line = ExternalSOP.readFully(sessionKeyOutIn);
                    SessionKey sessionKey = SessionKey.fromString(line.trim());
                    sessionKeyOutIn.close();
                    sessionKeyOut.delete();

                    List<Verification> verifications = new ArrayList<>();
                    if (verifyWithCounter != 0) {
                        FileInputStream verifyOutIn = new FileInputStream(verifyOut);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(verifyOutIn));
                        while ((line = reader.readLine()) != null) {
                            verifications.add(Verification.fromString(line.trim()));
                        }
                        reader.close();
                    }

                    return new DecryptionResult(sessionKey, verifications);
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
