// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.Profile;
import sop.SOP;
import sop.exception.SOPGPException;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ListProfilesTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void listGenerateKeyProfiles(SOP sop) {
        List<Profile> profiles = assumeSupported(sop::listProfiles)
                .generateKey();

        assertFalse(profiles.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void listEncryptProfiles(SOP sop) {
        List<Profile> profiles = assumeSupported(sop::listProfiles)
                .encrypt();

        assertFalse(profiles.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void listUnsupportedProfiles(SOP sop) {
        assertThrows(SOPGPException.UnsupportedProfile.class, () -> assumeSupported(sop::listProfiles)
                .subcommand("invalid"));
    }
}
