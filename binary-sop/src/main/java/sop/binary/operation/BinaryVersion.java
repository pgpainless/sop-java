// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.binary.operation;

import sop.operation.Version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BinaryVersion implements Version {

    private final Runtime runtime = Runtime.getRuntime();
    private final String binary;

    public BinaryVersion(String binaryName) {
        this.binary = binaryName;
    }

    @Override
    public String getName() {
        String[] command = new String[] {binary, "version"};
        try {
            Process process = runtime.exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = stdInput.readLine().trim();
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
        try {
            Process process = runtime.exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = stdInput.readLine().trim();
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
        try {
            Process process = runtime.exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getExtendedVersion() {
        String[] command = new String[] {binary, "version", "--extended"};
        try {
            Process process = runtime.exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
