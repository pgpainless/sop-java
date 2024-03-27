// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sop.SOP;
import sop.cli.picocli.SopCLI;
import sop.operation.Version;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sop.testsuite.assertions.SopExecutionAssertions.assertSuccess;
import static sop.testsuite.assertions.SopExecutionAssertions.assertUnsupportedOption;

public class VersionCmdTest {

    private Version version;

    @BeforeEach
    public void mockComponents() {
        SOP sop = mock(SOP.class);
        version = mock(Version.class);
        when(version.getName()).thenReturn("MockSop");
        when(version.getVersion()).thenReturn("1.0");
        when(version.getExtendedVersion()).thenReturn("MockSop Extended Version Information");
        when(version.getBackendVersion()).thenReturn("Foo");
        when(version.getSopSpecVersion()).thenReturn("draft-dkg-openpgp-stateless-cli-XX");
        when(version.getSopVVersion()).thenReturn("1.0");
        when(sop.version()).thenReturn(version);

        SopCLI.setSopInstance(sop);
    }

    @Test
    public void assertVersionCommandWorks() {
        assertSuccess(() ->
                SopCLI.execute("version"));
        verify(version, times(1)).getVersion();
        verify(version, times(1)).getName();
    }

    @Test
    public void assertExtendedVersionCommandWorks() {
        assertSuccess(() ->
                SopCLI.execute("version", "--extended"));
        verify(version, times(1)).getExtendedVersion();
    }

    @Test
    public void assertBackendVersionCommandWorks() {
        assertSuccess(() ->
                SopCLI.execute("version", "--backend"));
        verify(version, times(1)).getBackendVersion();
    }

    @Test
    public void assertSpecVersionCommandWorks() {
        assertSuccess(() ->
                SopCLI.execute("version", "--sop-spec"));
    }

    @Test
    public void assertSOPVVersionCommandWorks() {
        assertSuccess(() ->
                SopCLI.execute("version", "--sopv"));
    }

    @Test
    public void assertInvalidOptionResultsInExit37() {
        assertUnsupportedOption(() ->
                SopCLI.execute("version", "--invalid"));
    }
}
