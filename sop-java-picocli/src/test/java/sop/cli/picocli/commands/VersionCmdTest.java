// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sop.SOP;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.Version;

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
        when(sop.version()).thenReturn(version);

        SopCLI.setSopInstance(sop);
    }

    @Test
    public void assertVersionCommandWorks() {
        SopCLI.main(new String[] {"version"});
        verify(version, times(1)).getVersion();
        verify(version, times(1)).getName();
    }

    @Test
    public void assertExtendedVersionCommandWorks() {
        SopCLI.main(new String[] {"version", "--extended"});
        verify(version, times(1)).getExtendedVersion();
    }

    @Test
    public void assertBackendVersionCommandWorks() {
        SopCLI.main(new String[] {"version", "--backend"});
        verify(version, times(1)).getBackendVersion();
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.UnsupportedOption.EXIT_CODE)
    public void assertInvalidOptionResultsInExit37() {
        SopCLI.main(new String[] {"version", "--invalid"});
    }
}
