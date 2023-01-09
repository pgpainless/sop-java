// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.ExtractCert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ExtractCertExternal implements ExtractCert {

    private final String binary;
    private final Runtime runtime = Runtime.getRuntime();
    private final Properties environment;

    private boolean noArmor;

    public ExtractCertExternal(String binary, Properties properties) {
        this.binary = binary;
        this.environment = properties;
    }

    @Override
    public ExtractCert noArmor() {
        this.noArmor = true;
        return this;
    }

    @Override
    public Ready key(InputStream keyInputStream) throws SOPGPException.BadData {
        List<String> commandList = new ArrayList<>();

        commandList.add(binary);
        commandList.add("extract-cert");

        if (noArmor) {
            commandList.add("--no-armor");
        }

        List<String> envList = ExternalSOP.propertiesToEnv(environment);

        String[] command = commandList.toArray(new String[0]);
        String[] env = envList.toArray(new String[0]);

        try {
            Process process = runtime.exec(command, env);
            OutputStream extractOut = process.getOutputStream();
            InputStream extractIn = process.getInputStream();

            return new Ready() {
                @Override
                public void writeTo(OutputStream outputStream) throws IOException {
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = keyInputStream.read(buf)) > 0) {
                        extractOut.write(buf, 0, r);
                    }

                    keyInputStream.close();
                    extractOut.close();

                    while ((r = extractIn.read(buf)) > 0) {
                        outputStream.write(buf, 0 , r);
                    }

                    extractIn.close();
                    outputStream.close();

                    ExternalSOP.finish(process);
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
