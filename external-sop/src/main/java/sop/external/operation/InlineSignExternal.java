// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Ready;
import sop.enums.InlineSignAs;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.InlineSign;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class InlineSignExternal implements InlineSign {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    private int keyCounter = 0;
    private int withKeyPasswordCounter = 0;

    public InlineSignExternal(String binary, Properties environment) {
        commandList.add(binary);
        commandList.add("inline-sign");
        envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    public InlineSign noArmor() {
        commandList.add("--no-armor");
        return this;
    }

    @Override
    public InlineSign key(InputStream key) throws SOPGPException.KeyCannotSign, SOPGPException.BadData, SOPGPException.UnsupportedAsymmetricAlgo, IOException {
        String envVar = "KEY_" + keyCounter++;
        commandList.add("@ENV:" + envVar);
        envList.add(envVar + "=" + ExternalSOP.readFully(key));
        return this;
    }

    @Override
    public InlineSign withKeyPassword(byte[] password) throws SOPGPException.UnsupportedOption, SOPGPException.PasswordNotHumanReadable {
        String envVar = "WITH_KEY_PASSWORD_" + withKeyPasswordCounter++;
        commandList.add("--with-key-password=@ENV:" + envVar);
        envList.add(envVar + "=" + new String(password));
        return this;
    }

    @Override
    public InlineSign mode(InlineSignAs mode) throws SOPGPException.UnsupportedOption {
        commandList.add("--as=" + mode);
        return this;
    }

    @Override
    public Ready data(InputStream data) throws IOException, SOPGPException.KeyIsProtected, SOPGPException.ExpectedText {
        return ExternalSOP.ready(Runtime.getRuntime(), commandList, envList, data);
    }
}
