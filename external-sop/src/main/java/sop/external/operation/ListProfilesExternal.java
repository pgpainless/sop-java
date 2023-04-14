// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Profile;
import sop.external.ExternalSOP;
import sop.operation.ListProfiles;

import java.io.IOException;
import java.util.ArrayList;
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
    public List<Profile> subcommand(String command) {
        commandList.add(command);
        try {
            String output = new String(ExternalSOP.executeProducingOperation(Runtime.getRuntime(), commandList, envList).getBytes());
            return toProfiles(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Profile> toProfiles(String output) {
        List<Profile> profiles = new ArrayList<>();
        for (String line : output.split("\n")) {
            String[] split = line.split(": ");
            profiles.add(new Profile(split[0], split[1]));
        }
        return profiles;
    }
}
