// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation;

import sop.external.ExternalSOP;
import sop.operation.Version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class VersionExternal implements Version {

    private final Runtime runtime = Runtime.getRuntime();
    private final String binary;
    private final Properties environment;

    public VersionExternal(String binaryName, Properties environment) {
        this.binary = binaryName;
        this.environment = environment;
    }

    @Override
    public String getName() {
        String[] command = new String[] {binary, "version"};
        String[] env = ExternalSOP.propertiesToEnv(environment).toArray(new String[0]);
        try {
            Process process = runtime.exec(command, env);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = stdInput.readLine().trim();
            if (line.contains(" ")) {
                return line.substring(0, line.lastIndexOf(" "));
            }
            ExternalSOP.finish(process);
            return line;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getVersion() {
        String[] command = new String[] {binary, "version"};
        String[] env = ExternalSOP.propertiesToEnv(environment).toArray(new String[0]);
        try {
            Process process = runtime.exec(command, env);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = stdInput.readLine().trim();
            if (line.contains(" ")) {
                return line.substring(line.lastIndexOf(" ") + 1);
            }
            ExternalSOP.finish(process);
            return line;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBackendVersion() {
        String[] command = new String[] {binary, "version", "--backend"};
        String[] env = ExternalSOP.propertiesToEnv(environment).toArray(new String[0]);
        try {
            Process process = runtime.exec(command, env);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                sb.append(line).append('\n');
            }
            ExternalSOP.finish(process);
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getExtendedVersion() {
        String[] command = new String[] {binary, "version", "--extended"};
        String[] env = ExternalSOP.propertiesToEnv(environment).toArray(new String[0]);
        try {
            Process process = runtime.exec(command, env);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                sb.append(line).append('\n');
            }
            ExternalSOP.finish(process);
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
