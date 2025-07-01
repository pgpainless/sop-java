// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.TestAbortedException;
import sop.SOP;
import sop.exception.SOPGPException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("sop.testsuite.operation.AbstractSOPTest#hasBackends")
public class VersionTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void versionNameTest(SOP sop) {
        String name = assumeSupported(sop::version).getName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void versionVersionTest(SOP sop) {
        String version = assumeSupported(sop::version).getVersion();
        assertFalse(version.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void backendVersionTest(SOP sop) {
        String backend = assumeSupported(sop::version).getBackendVersion();
        assertFalse(backend.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void extendedVersionTest(SOP sop) {
        String extended = assumeSupported(sop::version).getExtendedVersion();
        assertFalse(extended.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void sopSpecVersionTest(SOP sop) {
        try {
            assumeSupported(sop::version).getSopSpecVersion();
        } catch (RuntimeException e) {
            throw new TestAbortedException("SOP backend does not support 'version --sop-spec' yet.");
        }

        String sopSpec = assumeSupported(sop::version).getSopSpecVersion();
        if (assumeSupported(sop::version).isSopSpecImplementationIncomplete()) {
            assertTrue(sopSpec.startsWith("~draft-dkg-openpgp-stateless-cli-"));
        } else {
            assertTrue(sopSpec.startsWith("draft-dkg-openpgp-stateless-cli-"));
        }

        int sopRevision = assumeSupported(sop::version).getSopSpecRevisionNumber();
        assertTrue(assumeSupported(sop::version).getSopSpecRevisionName().endsWith("" + sopRevision));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void sopVVersionTest(SOP sop) {
        try {
            assumeSupported(sop::version).getSopVVersion();
        } catch (SOPGPException.UnsupportedOption e) {
            throw new TestAbortedException(
                    "Implementation does (gracefully) not provide coverage for any sopv interface version.");
        } catch (RuntimeException e) {
            throw new TestAbortedException("Implementation does not provide coverage for any sopv interface version.");
        }
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void sopJavaVersionTest(SOP sop) {
        assertNotNull(assumeSupported(sop::version).getSopJavaVersion());
    }
}
