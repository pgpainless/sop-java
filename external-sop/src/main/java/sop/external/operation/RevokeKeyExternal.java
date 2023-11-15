// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.RevokeKey;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RevokeKeyExternal implements RevokeKey {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    private int withKeyPasswordCounter = 0;

    public RevokeKeyExternal(String binary, Properties environment) {
        this.commandList.add(binary);
        this.commandList.add("revoke-key");
        this.envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    @Nonnull
    public RevokeKey noArmor() {
        this.commandList.add("--no-armor");
        return this;
    }

    @Override
    @Nonnull
    public RevokeKey withKeyPassword(@Nonnull byte[] password) throws SOPGPException.UnsupportedOption, SOPGPException.PasswordNotHumanReadable {
        String envVar = "KEY_PASSWORD_" + withKeyPasswordCounter++;
        commandList.add("--with-key-password=@ENV:" + envVar);
        envList.add(envVar + "=" + new String(password));
        return this;
    }

    @Override
    @Nonnull
    public Ready keys(@Nonnull InputStream keys) {
        return ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, keys);
    }
}
