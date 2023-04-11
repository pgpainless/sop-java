// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.external.ExternalSOP;
import sop.operation.ListProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ListProfilesExternal implements ListProfiles {

    private final List<String> commandList = new ArrayList<>();
    private final List<String> envList;

    public ListProfilesExternal(String binary, Properties properties) {
        this.commandList.add(binary);
        this.commandList.add("list-profiles");
        this.envList = ExternalSOP.propertiesToEnv(properties);
    }

    @Override
    public List<String> ofCommand(String command) {
        commandList.add(command);
        try {
            String output = new String(ExternalSOP.executeProducingOperation(Runtime.getRuntime(), commandList, envList).getBytes());
            return Arrays.asList(output.split("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> global() {
        try {
            String output = new String(ExternalSOP.executeProducingOperation(Runtime.getRuntime(), commandList, envList).getBytes());
            return Arrays.asList(output.split("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
