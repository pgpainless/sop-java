// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.operation.GenerateKey;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class GenerateKeyExternal implements GenerateKey {

    private final String binary;
    private boolean noArmor = false;
    private List<String> userIds = new ArrayList<>();
    private String keyPassword;

    private final Runtime runtime = Runtime.getRuntime();

    public GenerateKeyExternal(String binary) {
        this.binary = binary;
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
            commandList.add(keyPassword);
        }

        for (String userId : userIds) {
            commandList.add(userId);
        }

        String[] command = commandList.toArray(new String[0]);
        try {
            Process process = runtime.exec(command);
            InputStream stdIn = process.getInputStream();

            return new Ready() {
                @Override
                public void writeTo(OutputStream outputStream) throws IOException {
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = stdIn.read(buf)) >= 0) {
                        outputStream.write(buf, 0, r);
                    }
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
