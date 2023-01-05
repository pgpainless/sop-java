// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.binary.operation;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.operation.ExtractCert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class BinaryExtractCert implements ExtractCert {

    private final String binary;
    private final Runtime runtime = Runtime.getRuntime();

    private boolean noArmor;

    public BinaryExtractCert(String binary) {
        this.binary = binary;
    }

    @Override
    public ExtractCert noArmor() {
        this.noArmor = true;
        return this;
    }

    @Override
    public Ready key(InputStream keyInputStream) throws IOException, SOPGPException.BadData {
        List<String> commandList = new ArrayList<>();

        commandList.add(binary);
        commandList.add("extract-cert");

        if (noArmor) {
            commandList.add("--no-armor");
        }

        String[] command = commandList.toArray(new String[0]);
        try {
            Process process = runtime.exec(command);
            OutputStream stdOut = process.getOutputStream();
            InputStream stdIn = process.getInputStream();

            return new Ready() {
                @Override
                public void writeTo(OutputStream outputStream) throws IOException {
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = keyInputStream.read(buf)) > 0) {
                        stdOut.write(buf, 0, r);
                    }

                    while ((r = stdIn.read(buf)) > 0) {
                        outputStream.write(buf, 0 , r);
                    }
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
