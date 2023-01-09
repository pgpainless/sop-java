// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Ready;
import sop.enums.ArmorLabel;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.Armor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.List;

public class ArmorExternal implements Armor {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    public ArmorExternal(String binary, Properties environment) {
        commandList.add(binary);
        commandList.add("armor");
        envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    public Armor label(ArmorLabel label) throws SOPGPException.UnsupportedOption {
        commandList.add("--label=" + label);
        return this;
    }

    @Override
    public Ready data(InputStream data) throws SOPGPException.BadData, IOException {
        return ExternalSOP.ready(Runtime.getRuntime(), commandList, envList);
    }
}