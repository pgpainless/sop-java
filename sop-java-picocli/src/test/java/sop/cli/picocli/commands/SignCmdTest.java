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
import static sop.testsuite.assertions.SopExecutionAssertions.assertBadData;
import static sop.testsuite.assertions.SopExecutionAssertions.assertExpectedText;
import static sop.testsuite.assertions.SopExecutionAssertions.assertGenericError;
import static sop.testsuite.assertions.SopExecutionAssertions.assertKeyIsProtected;
import static sop.testsuite.assertions.SopExecutionAssertions.assertMissingArg;
import static sop.testsuite.assertions.SopExecutionAssertions.assertMissingInput;
import static sop.testsuite.assertions.SopExecutionAssertions.assertSuccess;
import static sop.testsuite.assertions.SopExecutionAssertions.assertUnsupportedOption;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sop.ReadyWithResult;
import sop.SOP;
import sop.SigningResult;
import sop.cli.picocli.SopCLI;
import sop.cli.picocli.TestFileUtil;
import sop.exception.SOPGPException;
import sop.operation.DetachedSign;

public class SignCmdTest {

    DetachedSign detachedSign;
    File keyFile;
    File passFile;

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
        passFile = TestFileUtil.writeTempStringFile("sw0rdf1sh");
    }

    @Test
    public void as_optionsAreCaseInsensitive() {
        assertSuccess(() ->
                SopCLI.execute("sign", "--as", "Binary", keyFile.getAbsolutePath()));
        assertSuccess(() ->
                SopCLI.execute("sign", "--as", "binary", keyFile.getAbsolutePath()));
        assertSuccess(() ->
                SopCLI.execute("sign", "--as", "BINARY", keyFile.getAbsolutePath()));
    }

    @Test
    public void as_invalidOptionCausesExit37() {
        assertUnsupportedOption(() ->
                SopCLI.execute("sign", "--as", "Invalid", keyFile.getAbsolutePath()));
    }

    @Test
    public void as_unsupportedOptionCausesExit37() throws SOPGPException.UnsupportedOption {
        when(detachedSign.mode(any())).thenThrow(new SOPGPException.UnsupportedOption("Setting signing mode not supported."));
        assertUnsupportedOption(() ->
                SopCLI.execute("sign", "--as", "binary", keyFile.getAbsolutePath()));
    }

    @Test
    public void key_nonExistentKeyFileCausesExit61() {
        assertMissingInput(() ->
                SopCLI.execute("sign", "invalid.asc"));
    }

    @Test
    public void key_keyIsProtectedCausesExit67() throws SOPGPException.KeyIsProtected, IOException, SOPGPException.BadData {
        when(detachedSign.key((InputStream) any())).thenThrow(new SOPGPException.KeyIsProtected());
        assertKeyIsProtected(() ->
                SopCLI.execute("sign", keyFile.getAbsolutePath()));
    }

    @Test
    public void key_badDataCausesExit41() throws SOPGPException.KeyIsProtected, IOException, SOPGPException.BadData {
        when(detachedSign.key((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        assertBadData(() ->
                SopCLI.execute("sign", keyFile.getAbsolutePath()));
    }

    @Test
    public void key_missingKeyFileCausesExit19() {
        assertMissingArg(() ->
                SopCLI.execute("sign"));
    }

    @Test
    public void noArmor_notCalledByDefault() {
        assertSuccess(() ->
                SopCLI.execute("sign", keyFile.getAbsolutePath()));
        verify(detachedSign, never()).noArmor();
    }

    @Test
    public void noArmor_passedDown() {
        assertSuccess(() ->
                SopCLI.execute("sign", "--no-armor", keyFile.getAbsolutePath()));
        verify(detachedSign, times(1)).noArmor();
    }

    @Test
    public void withKeyPassword_passedDown() {
        assertSuccess(() ->
                SopCLI.execute("sign",
                        "--with-key-password", passFile.getAbsolutePath(),
                        keyFile.getAbsolutePath()));
        verify(detachedSign, times(1)).withKeyPassword("sw0rdf1sh");
    }

    @Test
    public void data_ioExceptionCausesExit1() throws IOException, SOPGPException.ExpectedText {
        when(detachedSign.data((InputStream) any())).thenReturn(new ReadyWithResult<SigningResult>() {
            @Override
            public SigningResult writeTo(OutputStream outputStream) throws IOException {
                throw new IOException();
            }
        });
        assertGenericError(() ->
                SopCLI.execute("sign", keyFile.getAbsolutePath()));
    }

    @Test
    public void data_expectedTextExceptionCausesExit53() throws IOException, SOPGPException.ExpectedText {
        when(detachedSign.data((InputStream) any())).thenThrow(new SOPGPException.ExpectedText());
        assertExpectedText(() ->
                SopCLI.execute("sign", keyFile.getAbsolutePath()));
    }
}
