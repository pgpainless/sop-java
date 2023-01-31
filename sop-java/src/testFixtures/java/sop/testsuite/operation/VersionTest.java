// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIf("sop.operation.AbstractSOPTest#hasBackends")
public class VersionTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void versionNameTest(SOP sop) {
        String name = sop.version().getName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void versionVersionTest(SOP sop) {
        String version = sop.version().getVersion();
        assertFalse(version.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void backendVersionTest(SOP sop) {
        String backend = sop.version().getBackendVersion();
        assertFalse(backend.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void extendedVersionTest(SOP sop) {
        String extended = sop.version().getExtendedVersion();
        assertFalse(extended.isEmpty());
    }

}
