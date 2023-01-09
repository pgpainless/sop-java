// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.ReadyWithResult;
import sop.SigningResult;
import sop.enums.SignAs;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.DetachedSign;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DetachedSignExternal implements DetachedSign {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    private int withKeyPasswordCounter = 0;
    private int keyCounter = 0;

    public DetachedSignExternal(String binary, Properties properties) {
        commandList.add(binary);
        commandList.add("sign");
        envList = ExternalSOP.propertiesToEnv(properties);
    }

    @Override
    public DetachedSign noArmor() {
        commandList.add("--no-armor");
        return this;
    }

    @Override
    public DetachedSign key(InputStream key) throws SOPGPException.KeyCannotSign, SOPGPException.BadData, SOPGPException.UnsupportedAsymmetricAlgo, IOException {
        String envVar = "KEY_" + keyCounter++;
        commandList.add("@ENV:" + envVar);
        envList.add(envVar + "=" + ExternalSOP.readFully(key));
        return this;
    }

    @Override
    public DetachedSign withKeyPassword(byte[] password) throws SOPGPException.UnsupportedOption, SOPGPException.PasswordNotHumanReadable {
        String envVar = "WITH_KEY_PASSWORD_" + withKeyPasswordCounter++;
        commandList.add("--with-key-password=@ENV:" + envVar);
        envList.add(envVar + "=" + new String(password));
        return this;
    }

    @Override
    public DetachedSign mode(SignAs mode) throws SOPGPException.UnsupportedOption {
        commandList.add("--as=" + mode);
        return this;
    }

    @Override
    public ReadyWithResult<SigningResult> data(InputStream data)
            throws IOException, SOPGPException.KeyIsProtected, SOPGPException.ExpectedText {

        String[] command = commandList.toArray(new String[0]);
        String[] env = envList.toArray(new String[0]);
        try {
            Process process = Runtime.getRuntime().exec(command, env);
            OutputStream processOut = process.getOutputStream();
            InputStream processIn = process.getInputStream();

            return new ReadyWithResult<SigningResult>() {
                @Override
                public SigningResult writeTo(OutputStream outputStream) throws IOException {
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = data.read(buf)) > 0) {
                        processOut.write(buf, 0, r);
                    }

                    data.close();
                    processOut.close();

                    while ((r = processIn.read(buf)) > 0) {
                        outputStream.write(buf, 0 , r);
                    }

                    processIn.close();
                    outputStream.close();

                    ExternalSOP.finish(process);

                    return SigningResult.builder().build();
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}