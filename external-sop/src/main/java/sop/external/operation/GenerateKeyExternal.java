// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.GenerateKey;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GenerateKeyExternal implements GenerateKey {

    private final String binary;
    private boolean noArmor = false;
    private List<String> userIds = new ArrayList<>();
    private String keyPassword;

    private final Runtime runtime = Runtime.getRuntime();
    private final Properties properties;

    public GenerateKeyExternal(String binary, Properties environment) {
        this.binary = binary;
        this.properties = environment;
    }

    @Override
    public GenerateKey noArmor() {
        this.noArmor = true;
        return this;
    }

    @Override
    public GenerateKey userId(String userId) {
        this.userIds.add(userId);
        return this;
    }

    @Override
    public GenerateKey withKeyPassword(String password)
            throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption {
        this.keyPassword = password;
        return this;
    }

    @Override
    public Ready generate()
            throws SOPGPException.MissingArg, SOPGPException.UnsupportedAsymmetricAlgo {
        List<String> commandList = new ArrayList<>();

        commandList.add(binary);
        commandList.add("generate-key");

        if (noArmor) {
            commandList.add("--no-armor");
        }

        if (keyPassword != null) {
            commandList.add("--with-key-password");
            commandList.add("@ENV:key_password");
        }

        for (String userId : userIds) {
            commandList.add(userId);
        }

        List<String> envList = ExternalSOP.propertiesToEnv(properties);
        if (keyPassword != null) {
            envList.add("key_password=" + keyPassword);
        }

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

                    ExternalSOP.finish(process);
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
