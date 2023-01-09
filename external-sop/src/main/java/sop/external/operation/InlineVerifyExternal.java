// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.ReadyWithResult;
import sop.Verification;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.InlineVerify;
import sop.util.UTCUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class InlineVerifyExternal implements InlineVerify {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    private int certCounter = 0;

    public InlineVerifyExternal(String binary, Properties environment) {
        commandList.add(binary);
        commandList.add("inline-verify");
        envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    public InlineVerify notBefore(Date timestamp) throws SOPGPException.UnsupportedOption {
        commandList.add("--not-before=" + UTCUtil.formatUTCDate(timestamp));
        return this;
    }

    @Override
    public InlineVerify notAfter(Date timestamp) throws SOPGPException.UnsupportedOption {
        commandList.add("--not-after=" + UTCUtil.formatUTCDate(timestamp));
        return this;
    }

    @Override
    public InlineVerify cert(InputStream cert) throws SOPGPException.BadData, IOException {
        String envVar = "CERT_" + certCounter++;
        commandList.add("@ENV:" + envVar);
        envList.add(envVar + "=" + ExternalSOP.readFully(cert));
        return this;
    }

    @Override
    public ReadyWithResult<List<Verification>> data(InputStream data) throws IOException, SOPGPException.NoSignature, SOPGPException.BadData {
        String[] command = commandList.toArray(new String[0]);
        String[] env = envList.toArray(new String[0]);

        try {
            Process process = Runtime.getRuntime().exec(command, env);
            OutputStream processOut = process.getOutputStream();
            InputStream processIn = process.getInputStream();

            return new ReadyWithResult<List<Verification>>() {
                @Override
                public List<Verification> writeTo(OutputStream outputStream) throws IOException, SOPGPException.NoSignature {
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

                    return null; // TODO
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
