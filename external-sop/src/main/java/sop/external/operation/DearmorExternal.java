// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.Dearmor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of the {@link Dearmor} operation using an external SOP binary.
 */
public class DearmorExternal implements Dearmor {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    public DearmorExternal(String binary, Properties environment) {
        commandList.add(binary);
        commandList.add("dearmor");
        envList = ExternalSOP.propertiesToEnv(environment);
    }

    @Override
    public Ready data(InputStream data) throws SOPGPException.BadData {
        return ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, data);
    }
}
