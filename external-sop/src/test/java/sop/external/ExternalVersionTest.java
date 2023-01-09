// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalVersionTest extends AbstractExternalSOPTest {

    @Test
    public void versionNameTest() {
        String name = getSop().version().getName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test
    public void versionVersionTest() {
        String version = getSop().version().getVersion();
        assertTrue(version.matches("\\d+(\\.\\d+)*\\S*"));
    }

    @Test
    public void backendVersionTest() {
        String backend = getSop().version().getBackendVersion();
        assertFalse(backend.isEmpty());
    }

    @Test
    public void extendedVersionTest() {
        String extended = getSop().version().getExtendedVersion();
        assertFalse(extended.isEmpty());
    }

}
