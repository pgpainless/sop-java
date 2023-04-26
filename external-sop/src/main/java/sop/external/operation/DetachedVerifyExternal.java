// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Verification;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.DetachedVerify;
import sop.operation.VerifySignatures;
import sop.util.UTCUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Implementation of the {@link DetachedVerify} operation using an external SOP binary.
 */
public class DetachedVerifyExternal implements DetachedVerify {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    private final Set<InputStream> certs = new HashSet<>();
    private InputStream signatures;
    private int certCounter = 0;

    public DetachedVerifyExternal(String binary, Properties environment) {
        commandList.add(binary);
        commandList.add("verify");
        envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    public DetachedVerify notBefore(Date timestamp) throws SOPGPException.UnsupportedOption {
        commandList.add("--not-before=" + UTCUtil.formatUTCDate(timestamp));
        return this;
    }

    @Override
    public DetachedVerify notAfter(Date timestamp) throws SOPGPException.UnsupportedOption {
        commandList.add("--not-after=" + UTCUtil.formatUTCDate(timestamp));
        return this;
    }

    @Override
    public DetachedVerify cert(InputStream cert) throws SOPGPException.BadData {
        this.certs.add(cert);
        return this;
    }

    @Override
    public VerifySignatures signatures(InputStream signatures) throws SOPGPException.BadData {
        this.signatures = signatures;
        return this;
    }

    @Override
    public List<Verification> data(InputStream data) throws IOException, SOPGPException.NoSignature, SOPGPException.BadData {
        commandList.add("@ENV:SIGNATURE");
        envList.add("SIGNATURE=" + ExternalSOP.readString(signatures));

        for (InputStream cert : certs) {
            String envVar = "CERT_" + certCounter++;
            commandList.add("@ENV:" + envVar);
            envList.add(envVar + "=" + ExternalSOP.readString(cert));
        }

        String[] command = commandList.toArray(new String[0]);
        String[] env = envList.toArray(new String[0]);

        try {
            Process process = Runtime.getRuntime().exec(command, env);
            OutputStream processOut = process.getOutputStream();
            InputStream processIn = process.getInputStream();

                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = data.read(buf)) > 0) {
                        processOut.write(buf, 0, r);
                    }

                    data.close();
                    processOut.close();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(processIn));
                    List<Verification> verifications = new ArrayList<>();

                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        verifications.add(Verification.fromString(line));
                    }

                    ExternalSOP.finish(process);

                    return verifications;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
