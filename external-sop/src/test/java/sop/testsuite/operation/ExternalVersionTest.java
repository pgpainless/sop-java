// SPDX-FileCopyrightText: 2026 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;
import sop.external.ExternalSOP;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ExternalVersionTest extends VersionTest {

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void testExtendedVersionHasAppendedExternalSOPVersion(SOP sop) {
        assumeTrue(sop instanceof ExternalSOP);
        String sopJavaVersion = sop.version().getSopJavaVersion();

        String extendedVersion = sop.version().getExtendedVersion();
        assertTrue(extendedVersion.endsWith("via external-sop " + sopJavaVersion));
    }
}
