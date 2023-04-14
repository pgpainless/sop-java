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

/**
 * Implementation of the {@link Version} operation using an external SOP binary.
 */
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
            ExternalSOP.finish(process);
            if (line.contains(" ")) {
                return line.substring(0, line.lastIndexOf(" "));
            }
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
            ExternalSOP.finish(process);
            if (line.contains(" ")) {
                return line.substring(line.lastIndexOf(" ") + 1);
            }
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

    @Override
    public int getSopSpecVersionNumber() {
        String revision = getSopSpecVersion();
        String firstLine;
        if (revision.contains("\n")) {
            firstLine = revision.substring(0, revision.indexOf("\n"));
        } else {
            firstLine = revision;
        }

        if (!firstLine.contains("-")) {
            return -1;
        }

        return Integer.parseInt(firstLine.substring(firstLine.lastIndexOf("-") + 1));
    }

    @Override
    public boolean isSopSpecImplementationIncomplete() {
        String revision = getSopSpecVersion();
        return revision.startsWith("~");
    }

    @Override
    public String getSopSpecImplementationIncompletenessRemarks() {
        String revision = getSopSpecVersion();
        if (revision.contains("\n")) {
            String tail = revision.substring(revision.indexOf("\n") + 1).trim();

            if (!tail.isEmpty()) {
                return tail;
            }
        }
        return null;
    }

    @Override
    public String getSopSpecVersion() {
        String[] command = new String[] {binary, "version", "--sop-spec"};
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
