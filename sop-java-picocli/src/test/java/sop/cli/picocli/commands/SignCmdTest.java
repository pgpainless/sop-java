// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sop.ReadyWithResult;
import sop.SOP;
import sop.SigningResult;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.DetachedSign;

public class SignCmdTest {

    DetachedSign detachedSign;
    File keyFile;

    @BeforeEach
    public void mockComponents() throws IOException, SOPGPException.ExpectedText {
        detachedSign = mock(DetachedSign.class);
        when(detachedSign.data((InputStream) any())).thenReturn(new ReadyWithResult<SigningResult>() {
            @Override
            public SigningResult writeTo(OutputStream outputStream) {
                return SigningResult.builder().build();
            }
        });

        SOP sop = mock(SOP.class);
        when(sop.detachedSign()).thenReturn(detachedSign);

        SopCLI.setSopInstance(sop);

        keyFile = File.createTempFile("sign-", ".asc");
    }

    @Test
    public void as_optionsAreCaseInsensitive() {
        SopCLI.main(new String[] {"sign", "--as", "Binary", keyFile.getAbsolutePath()});
        SopCLI.main(new String[] {"sign", "--as", "binary", keyFile.getAbsolutePath()});
        SopCLI.main(new String[] {"sign", "--as", "BINARY", keyFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.UnsupportedOption.EXIT_CODE)
    public void as_invalidOptionCausesExit37() {
        SopCLI.main(new String[] {"sign", "--as", "Invalid", keyFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.UnsupportedOption.EXIT_CODE)
    public void as_unsupportedOptionCausesExit37() throws SOPGPException.UnsupportedOption {
        when(detachedSign.mode(any())).thenThrow(new SOPGPException.UnsupportedOption("Setting signing mode not supported."));
        SopCLI.main(new String[] {"sign", "--as", "binary", keyFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.MissingInput.EXIT_CODE)
    public void key_nonExistentKeyFileCausesExit61() {
        SopCLI.main(new String[] {"sign", "invalid.asc"});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.KeyIsProtected.EXIT_CODE)
    public void key_keyIsProtectedCausesExit67() throws SOPGPException.KeyIsProtected, IOException, SOPGPException.BadData {
        when(detachedSign.key((InputStream) any())).thenThrow(new SOPGPException.KeyIsProtected());
        SopCLI.main(new String[] {"sign", keyFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.BadData.EXIT_CODE)
    public void key_badDataCausesExit41() throws SOPGPException.KeyIsProtected, IOException, SOPGPException.BadData {
        when(detachedSign.key((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        SopCLI.main(new String[] {"sign", keyFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.MissingArg.EXIT_CODE)
    public void key_missingKeyFileCausesExit19() {
        SopCLI.main(new String[] {"sign"});
    }

    @Test
    public void noArmor_notCalledByDefault() {
        SopCLI.main(new String[] {"sign", keyFile.getAbsolutePath()});
        verify(detachedSign, never()).noArmor();
    }

    @Test
    public void noArmor_passedDown() {
        SopCLI.main(new String[] {"sign", "--no-armor", keyFile.getAbsolutePath()});
        verify(detachedSign, times(1)).noArmor();
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    public void data_ioExceptionCausesExit1() throws IOException, SOPGPException.ExpectedText {
        when(detachedSign.data((InputStream) any())).thenReturn(new ReadyWithResult<SigningResult>() {
            @Override
            public SigningResult writeTo(OutputStream outputStream) throws IOException {
                throw new IOException();
            }
        });
        SopCLI.main(new String[] {"sign", keyFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.ExpectedText.EXIT_CODE)
    public void data_expectedTextExceptionCausesExit53() throws IOException, SOPGPException.ExpectedText {
        when(detachedSign.data((InputStream) any())).thenThrow(new SOPGPException.ExpectedText());
        SopCLI.main(new String[] {"sign", keyFile.getAbsolutePath()});
    }
}
