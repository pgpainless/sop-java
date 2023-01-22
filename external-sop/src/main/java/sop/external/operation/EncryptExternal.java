// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Ready;
import sop.enums.EncryptAs;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.Encrypt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of the {@link Encrypt} operation using an external SOP binary.
 */
public class EncryptExternal implements Encrypt {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;
    private int SIGN_WITH_COUNTER = 0;
    private int KEY_PASSWORD_COUNTER = 0;
    private int PASSWORD_COUNTER = 0;
    private int CERT_COUNTER = 0;

    public EncryptExternal(String binary, Properties environment) {
        this.commandList.add(binary);
        this.commandList.add("encrypt");
        this.envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    public Encrypt noArmor() {
        this.commandList.add("--no-armor");
        return this;
    }

    @Override
    public Encrypt mode(EncryptAs mode)
            throws SOPGPException.UnsupportedOption {
        this.commandList.add("--as=" + mode);
        return this;
    }

    @Override
    public Encrypt signWith(InputStream key)
            throws SOPGPException.KeyCannotSign, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.BadData,
            IOException {
        String envVar = "SIGN_WITH_" + SIGN_WITH_COUNTER++;
        commandList.add("--sign-with=@ENV:" + envVar);
        envList.add(envVar + "=" + ExternalSOP.readString(key));
        return this;
    }

    @Override
    public Encrypt withKeyPassword(byte[] password)
            throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption {
        String envVar = "KEY_PASSWORD_" + KEY_PASSWORD_COUNTER++;
        commandList.add("--with-key-password=@ENV:" + envVar);
        envList.add(envVar + "=" + new String(password));
        return this;
    }

    @Override
    public Encrypt withPassword(String password)
            throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption {
        String envVar = "PASSWORD_" + PASSWORD_COUNTER++;
        commandList.add("--with-password=@ENV:" + envVar);
        envList.add(envVar + "=" + password);
        return this;
    }

    @Override
    public Encrypt withCert(InputStream cert)
            throws SOPGPException.CertCannotEncrypt, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.BadData,
            IOException {
        String envVar = "CERT_" + CERT_COUNTER++;
        commandList.add("@ENV:" + envVar);
        envList.add(envVar + "=" + ExternalSOP.readString(cert));
        return this;
    }

    @Override
    public Ready plaintext(InputStream plaintext)
            throws IOException, SOPGPException.KeyIsProtected {
        return ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, plaintext);
    }
}
