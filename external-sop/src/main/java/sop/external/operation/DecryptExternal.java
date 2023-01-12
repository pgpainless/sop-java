// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.DecryptionResult;
import sop.ReadyWithResult;
import sop.SessionKey;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.Decrypt;
import sop.util.UTCUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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
        commandList.add("--session-key-out=" + sessionKeyOut.getAbsolutePath());

        File verifyOut = new File(tempDir, "verify-out");
        commandList.add("--verify-out=" + verifyOut.getAbsolutePath());

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

                    return new DecryptionResult(null, Collections.emptyList()); // TODO
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
