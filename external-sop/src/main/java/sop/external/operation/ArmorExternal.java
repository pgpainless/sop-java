// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Ready;
import sop.enums.ArmorLabel;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.Armor;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of the {@link Armor} operation using an external SOP binary.
 */
public class ArmorExternal implements Armor {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    public ArmorExternal(String binary, Properties environment) {
        commandList.add(binary);
        commandList.add("armor");
        envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    @Deprecated
    @Nonnull
    public Armor label(@Nonnull ArmorLabel label) throws SOPGPException.UnsupportedOption {
        commandList.add("--label=" + label);
        return this;
    }

    @Override
    @Nonnull
    public Ready data(@Nonnull InputStream data) throws SOPGPException.BadData {
        return ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, data);
    }
}
