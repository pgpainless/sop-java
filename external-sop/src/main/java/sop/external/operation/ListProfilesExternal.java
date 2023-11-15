// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.Profile;
import sop.external.ExternalSOP;
import sop.operation.ListProfiles;

import javax.annotation.Nonnull;
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
    @Nonnull
    public List<Profile> subcommand(@Nonnull String command) {
        commandList.add(command);
        try {
            String output = new String(ExternalSOP.executeProducingOperation(Runtime.getRuntime(), commandList, envList).getBytes());
            return toProfiles(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Profile> toProfiles(String output) {
        List<Profile> profiles = new ArrayList<>();
        for (String line : output.split("\n")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            profiles.add(Profile.parse(line));
        }
        return profiles;
    }
}
