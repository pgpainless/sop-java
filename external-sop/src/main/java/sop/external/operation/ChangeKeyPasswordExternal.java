// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.ChangeKeyPassword;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ChangeKeyPasswordExternal implements ChangeKeyPassword {
    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    private int keyPasswordCounter = 0;

    public ChangeKeyPasswordExternal(String binary, Properties environment) {
        this.commandList.add(binary);
        this.commandList.add("decrypt");
        this.envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    @Nonnull
    public ChangeKeyPassword noArmor() {
        this.commandList.add("--no-armor");
        return this;
    }

    @Override
    @Nonnull
    public ChangeKeyPassword oldKeyPassphrase(@Nonnull String oldPassphrase) {
        this.commandList.add("--old-key-password=@ENV:KEY_PASSWORD_" + keyPasswordCounter);
        this.envList.add("KEY_PASSWORD_" + keyPasswordCounter + "=" + oldPassphrase);
        keyPasswordCounter++;

        return this;
    }

    @Override
    @Nonnull
    public ChangeKeyPassword newKeyPassphrase(@Nonnull String newPassphrase) {
        this.commandList.add("--new-key-password=@ENV:KEY_PASSWORD_" + keyPasswordCounter);
        this.envList.add("KEY_PASSWORD_" + keyPasswordCounter + "=" + newPassphrase);
        keyPasswordCounter++;

        return this;
    }

    @Override
    @Nonnull
    public Ready keys(@Nonnull InputStream inputStream) throws SOPGPException.KeyIsProtected, SOPGPException.BadData {
        return ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, inputStream);
    }
}
