// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.ReadyWithResult;
import sop.Signatures;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.InlineDetach;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of the {@link InlineDetach} operation using an external SOP binary.
 */
public class InlineDetachExternal implements InlineDetach {

    private final ExternalSOP.TempDirProvider tempDirProvider;
    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    public InlineDetachExternal(String binary, Properties environment, ExternalSOP.TempDirProvider tempDirProvider) {
        this.tempDirProvider = tempDirProvider;
        commandList.add(binary);
        commandList.add("inline-detach");
        envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    @Nonnull
    public InlineDetach noArmor() {
        commandList.add("--no-armor");
        return this;
    }

    @Override
    @Nonnull
    public ReadyWithResult<Signatures> message(@Nonnull InputStream messageInputStream) throws IOException, SOPGPException.BadData {
        File tempDir = tempDirProvider.provideTempDirectory();

        File signaturesOut = new File(tempDir, "signatures");
        signaturesOut.delete();
        commandList.add("--signatures-out=" + signaturesOut.getAbsolutePath());

        String[] command = commandList.toArray(new String[0]);
        String[] env = envList.toArray(new String[0]);

        try {
            Process process = Runtime.getRuntime().exec(command, env);
            OutputStream processOut = process.getOutputStream();
            InputStream processIn = process.getInputStream();

            return new ReadyWithResult<Signatures>() {
                @Override
                public Signatures writeTo(@Nonnull OutputStream outputStream) throws IOException {
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

                    FileInputStream signaturesOutIn = new FileInputStream(signaturesOut);
                    ByteArrayOutputStream signaturesBuffer = new ByteArrayOutputStream();
                    while ((r = signaturesOutIn.read(buf)) > 0) {
                        signaturesBuffer.write(buf, 0, r);
                    }
                    signaturesOutIn.close();
                    signaturesOut.delete();

                    final byte[] sigBytes = signaturesBuffer.toByteArray();
                    return new Signatures() {
                        @Override
                        public void writeTo(@Nonnull OutputStream signatureOutputStream) throws IOException {
                            signatureOutputStream.write(sigBytes);
                        }
                    };
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
