// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("sop.external.AbstractExternalSOPTest#hasBackends")
public class ExternalVersionTest extends AbstractExternalSOPTest {

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void versionNameTest(SOP sop) {
        String name = sop.version().getName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void versionVersionTest(SOP sop) {
        String version = sop.version().getVersion();
        assertTrue(version.matches("\\d+(\\.\\d+)*\\S*"));
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void backendVersionTest(SOP sop) {
        String backend = sop.version().getBackendVersion();
        assertFalse(backend.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void extendedVersionTest(SOP sop) {
        String extended = sop.version().getExtendedVersion();
        assertFalse(extended.isEmpty());
    }

}
