// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import com.ginsberg.junit.exit.FailOnSystemExit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sop.Ready;
import sop.SOP;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.Armor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ArmorCmdTest {

    private Armor armor;
    private SOP sop;

    @BeforeEach
    public void mockComponents() throws SOPGPException.BadData, IOException {
        armor = mock(Armor.class);
        sop = mock(SOP.class);
        when(sop.armor()).thenReturn(armor);
        when(armor.data((InputStream) any())).thenReturn(nopReady());

        SopCLI.setSopInstance(sop);
    }

    @Test
    public void assertDataIsAlwaysCalled() throws SOPGPException.BadData, IOException {
        SopCLI.main(new String[] {"armor"});
        verify(armor, times(1)).data((InputStream) any());
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.BadData.EXIT_CODE)
    public void ifBadDataExit41() throws SOPGPException.BadData, IOException {
        when(armor.data((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));

        SopCLI.main(new String[] {"armor"});
    }

    @Test
    @FailOnSystemExit
    public void ifNoErrorsNoExit() {
        when(sop.armor()).thenReturn(armor);

        SopCLI.main(new String[] {"armor"});
    }

    private static Ready nopReady() {
        return new Ready() {
            @Override
            public void writeTo(@Nonnull OutputStream outputStream) {
            }
        };
    }
}
