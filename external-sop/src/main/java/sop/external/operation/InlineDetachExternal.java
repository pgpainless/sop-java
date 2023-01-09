// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.ReadyWithResult;
import sop.Signatures;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.InlineDetach;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class InlineDetachExternal implements InlineDetach {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    public InlineDetachExternal(String binary, Properties environment) {
        commandList.add(binary);
        commandList.add("inline-detach");
        envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    public InlineDetach noArmor() {
        commandList.add("--no-armor");
        return this;
    }

    @Override
    public ReadyWithResult<Signatures> message(InputStream messageInputStream) throws IOException, SOPGPException.BadData {
        String[] command = commandList.toArray(new String[0]);
        String[] env = envList.toArray(new String[0]);

        try {
            Process process = Runtime.getRuntime().exec(command, env);
            OutputStream processOut = process.getOutputStream();
            InputStream processIn = process.getInputStream();

            return new ReadyWithResult<Signatures>() {
                @Override
                public Signatures writeTo(OutputStream outputStream) throws IOException {
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = messageInputStream.read(buf)) > 0) {
                        processOut.write(buf, 0, r);
                    }

                    messageInputStream.close();
                    processOut.close();

                    while ((r = processIn.read(buf)) > 0) {
                        outputStream.write(buf, 0 , r);
                    }

                    processIn.close();
                    outputStream.close();

                    ExternalSOP.finish(process);

                    return null; // TODO
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
