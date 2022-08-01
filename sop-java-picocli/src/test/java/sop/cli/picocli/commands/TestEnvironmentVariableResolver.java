// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import java.util.HashMap;
import java.util.Map;

public class TestEnvironmentVariableResolver implements AbstractSopCmd.EnvironmentVariableResolver {

    private final Map<String, String> environment = new HashMap<>();

    public void addEnvironmentVariable(String name, String value) {
        this.environment.put(name, value);
    }

    @Override
    public String resolveEnvironmentVariable(String name) {
        return environment.get(name);
    }
}
