// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import sop.exception.SOPGPException;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class UnsupportedSubcommandTest extends AbstractExternalSOPTest {

    private final UnsupportedSubcommandExternal unsupportedSubcommand;

    public UnsupportedSubcommandTest() {
        String backend = readSopBackendFromProperties();
        assumeTrue(backend != null);
        Properties environment = readBackendEnvironment();
        unsupportedSubcommand = new UnsupportedSubcommandExternal(backend, environment);
    }

    @Test
    public void testUnsupportedSubcommand() {
        // "sop unsupported" returns error code UNSUPPORTED_SUBCOMMAND
        assertThrows(SOPGPException.UnsupportedSubcommand.class,
                unsupportedSubcommand::executeUnsupportedSubcommand);
    }

    private static class UnsupportedSubcommandExternal {

        private final Runtime runtime = Runtime.getRuntime();
        private final String binary;
        private final Properties environment;

        UnsupportedSubcommandExternal(String binaryName, Properties environment) {
            this.binary = binaryName;
            this.environment = environment;
        }

        public void executeUnsupportedSubcommand() {
            String[] command = new String[] {binary, "unsupported"}; // ~$ sop unsupported
            String[] env = ExternalSOP.propertiesToEnv(environment).toArray(new String[0]);
            try {
                Process process = runtime.exec(command, env);
                ExternalSOP.finish(process);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
