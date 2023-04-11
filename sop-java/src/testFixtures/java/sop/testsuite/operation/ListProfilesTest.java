// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ListProfilesTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void listGlobalProfiles(SOP sop) throws IOException {
        List<String> profiles = sop.listProfiles()
                .global();
        assertFalse(profiles.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void listGenerateKeyProfiles(SOP sop) throws IOException {
        List<String> profiles = sop.listProfiles()
                .ofCommand("generate-key");
        assertFalse(profiles.isEmpty());
    }

}
