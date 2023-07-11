// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.GenerateKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of the {@link GenerateKey} operation using an external SOP binary.
 */
public class GenerateKeyExternal implements GenerateKey {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    private int keyPasswordCounter = 0;

    public GenerateKeyExternal(String binary, Properties environment) {
        this.commandList.add(binary);
        this.commandList.add("generate-key");
        this.envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    public GenerateKey noArmor() {
        this.commandList.add("--no-armor");
        return this;
    }

    @Override
    public GenerateKey userId(String userId) {
        this.commandList.add(userId);
        return this;
    }

    @Override
    public GenerateKey withKeyPassword(String password)
            throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption {
        this.commandList.add("--with-key-password=@ENV:KEY_PASSWORD_" + keyPasswordCounter);
        this.envList.add("KEY_PASSWORD_" + keyPasswordCounter + "=" + password);
        keyPasswordCounter++;

        return this;
    }

    @Override
    public GenerateKey profile(String profile) {
        commandList.add("--profile=" + profile);
        return this;
    }

    @Override
    public GenerateKey signingOnly() {
        commandList.add("--signing-only");
        return this;
    }

    @Override
    public Ready generate()
            throws SOPGPException.MissingArg, SOPGPException.UnsupportedAsymmetricAlgo {
        return ExternalSOP.executeProducingOperation(Runtime.getRuntime(), commandList, envList);
    }
}
