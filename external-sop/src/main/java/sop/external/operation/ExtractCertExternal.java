// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.external.ExternalSOP;
import sop.operation.ExtractCert;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of the {@link ExtractCert} operation using an external SOP binary.
 */
public class ExtractCertExternal implements ExtractCert {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    public ExtractCertExternal(String binary, Properties properties) {
        this.commandList.add(binary);
        this.commandList.add("extract-cert");
        this.envList = ExternalSOP.propertiesToEnv(properties);
    }

    @Override
    @Nonnull
    public ExtractCert noArmor() {
        this.commandList.add("--no-armor");
        return this;
    }

    @Override
    @Nonnull
    public Ready key(@Nonnull InputStream keyInputStream) throws SOPGPException.BadData {
        return ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, keyInputStream);
    }
}
