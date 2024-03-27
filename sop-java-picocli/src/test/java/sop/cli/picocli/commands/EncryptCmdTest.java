// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sop.EncryptionResult;
import sop.ReadyWithResult;
import sop.SOP;
import sop.cli.picocli.SopCLI;
import sop.cli.picocli.TestFileUtil;
import sop.enums.EncryptAs;
import sop.exception.SOPGPException;
import sop.operation.Encrypt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sop.testsuite.assertions.SopExecutionAssertions.assertBadData;
import static sop.testsuite.assertions.SopExecutionAssertions.assertCertCannotEncrypt;
import static sop.testsuite.assertions.SopExecutionAssertions.assertGenericError;
import static sop.testsuite.assertions.SopExecutionAssertions.assertKeyCannotSign;
import static sop.testsuite.assertions.SopExecutionAssertions.assertKeyIsProtected;
import static sop.testsuite.assertions.SopExecutionAssertions.assertMissingArg;
import static sop.testsuite.assertions.SopExecutionAssertions.assertMissingInput;
import static sop.testsuite.assertions.SopExecutionAssertions.assertPasswordNotHumanReadable;
import static sop.testsuite.assertions.SopExecutionAssertions.assertSuccess;
import static sop.testsuite.assertions.SopExecutionAssertions.assertUnsupportedAsymmetricAlgo;
import static sop.testsuite.assertions.SopExecutionAssertions.assertUnsupportedOption;

public class EncryptCmdTest {

    Encrypt encrypt;

    @BeforeEach
    public void mockComponents() throws IOException {
        encrypt = mock(Encrypt.class);
        when(encrypt.plaintext((InputStream) any())).thenReturn(new ReadyWithResult<EncryptionResult>() {
            @Override
            public EncryptionResult writeTo(@NotNull OutputStream outputStream) throws IOException, SOPGPException {
                return new EncryptionResult(null);
            }
        });

        SOP sop = mock(SOP.class);
        when(sop.encrypt()).thenReturn(encrypt);

        SopCLI.setSopInstance(sop);
    }

    @Test
    public void missingBothPasswordAndCertFileCausesMissingArg() {
        assertMissingArg(() ->
                SopCLI.execute("encrypt", "--no-armor"));
    }

    @Test
    public void as_unsupportedEncryptAsCausesUnsupportedOption() throws SOPGPException.UnsupportedOption {
        when(encrypt.mode(any())).thenThrow(new SOPGPException.UnsupportedOption("Setting encryption mode not supported."));

        assertUnsupportedOption(() ->
                SopCLI.execute("encrypt", "--as", "Binary"));
    }

    @Test
    public void as_invalidModeOptionCausesUnsupportedOption() {
        assertUnsupportedOption(() ->
                SopCLI.execute("encrypt", "--as", "invalid"));
    }

    @Test
    public void as_modeIsPassedDown() throws SOPGPException.UnsupportedOption, IOException {
        File passwordFile = TestFileUtil.writeTempStringFile("0rbit");
        for (EncryptAs mode : EncryptAs.values()) {
            assertSuccess(() ->
                    SopCLI.execute("encrypt", "--as", mode.name(),
                            "--with-password", passwordFile.getAbsolutePath()));
            verify(encrypt, times(1)).mode(mode);
        }
    }

    @Test
    public void withPassword_notHumanReadablePasswordCausesPWNotHumanReadable() throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption, IOException {
        when(encrypt.withPassword("pretendThisIsNotReadable")).thenThrow(new SOPGPException.PasswordNotHumanReadable());
        File passwordFile = TestFileUtil.writeTempStringFile("pretendThisIsNotReadable");
        assertPasswordNotHumanReadable(() ->
                SopCLI.execute("encrypt", "--with-password", passwordFile.getAbsolutePath()));
    }

    @Test
    public void withPassword_unsupportedWithPasswordCausesUnsupportedOption() throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption, IOException {
        when(encrypt.withPassword(any())).thenThrow(new SOPGPException.UnsupportedOption("Encrypting with password not supported."));
        File passwordFile = TestFileUtil.writeTempStringFile("orange");
        assertUnsupportedOption(() ->
                SopCLI.execute("encrypt", "--with-password", passwordFile.getAbsolutePath()));
    }

    @Test
    public void signWith_multipleTimesGetPassedDown() throws IOException, SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.KeyCannotSign, SOPGPException.BadData {
        File keyFile1 = File.createTempFile("sign-with-1-", ".asc");
        File keyFile2 = File.createTempFile("sign-with-2-", ".asc");
        File passwordFile = TestFileUtil.writeTempStringFile("password");
        assertSuccess(() ->
                SopCLI.execute("encrypt", "--with-password", passwordFile.getAbsolutePath(),
                        "--sign-with", keyFile1.getAbsolutePath(),
                        "--sign-with", keyFile2.getAbsolutePath()));
        verify(encrypt, times(2)).signWith((InputStream) any());
    }

    @Test
    public void signWith_nonExistentKeyFileCausesMissingInput() {
        assertMissingInput(() ->
                SopCLI.execute("encrypt", "--with-password", "admin", "--sign-with", "nonExistent.asc"));
    }

    @Test
    public void signWith_keyIsProtectedCausesKeyIsProtected() throws SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.KeyCannotSign, SOPGPException.BadData, IOException {
        when(encrypt.signWith((InputStream) any())).thenThrow(new SOPGPException.KeyIsProtected());
        File keyFile = File.createTempFile("sign-with", ".asc");
        File passwordFile = TestFileUtil.writeTempStringFile("starship");
        assertKeyIsProtected(() ->
                SopCLI.execute("encrypt", "--sign-with", keyFile.getAbsolutePath(),
                        "--with-password", passwordFile.getAbsolutePath()));
    }

    @Test
    public void signWith_unsupportedAsymmetricAlgoCausesUnsupportedAsymAlgo() throws SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.KeyCannotSign, SOPGPException.BadData, IOException {
        when(encrypt.signWith((InputStream) any())).thenThrow(new SOPGPException.UnsupportedAsymmetricAlgo("Unsupported asymmetric algorithm.", new Exception()));
        File keyFile = File.createTempFile("sign-with", ".asc");
        File passwordFile = TestFileUtil.writeTempStringFile("123456");
        assertUnsupportedAsymmetricAlgo(() ->
                SopCLI.execute("encrypt", "--with-password", passwordFile.getAbsolutePath(),
                        "--sign-with", keyFile.getAbsolutePath()));
    }

    @Test
    public void signWith_certCannotSignCausesKeyCannotSign() throws IOException, SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.KeyCannotSign, SOPGPException.BadData {
        when(encrypt.signWith((InputStream) any())).thenThrow(new SOPGPException.KeyCannotSign());
        File keyFile = File.createTempFile("sign-with", ".asc");
        File passwordFile = TestFileUtil.writeTempStringFile("dragon");
        assertKeyCannotSign(() ->
                SopCLI.execute("encrypt", "--with-password", passwordFile.getAbsolutePath(),
                        "--sign-with", keyFile.getAbsolutePath()));
    }

    @Test
    public void signWith_badDataCausesBadData() throws SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.KeyCannotSign, SOPGPException.BadData, IOException {
        when(encrypt.signWith((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        File keyFile = File.createTempFile("sign-with", ".asc");
        File passwordFile = TestFileUtil.writeTempStringFile("orange");
        assertBadData(() ->
                SopCLI.execute("encrypt", "--with-password", passwordFile.getAbsolutePath(),
                        "--sign-with", keyFile.getAbsolutePath()));
    }

    @Test
    public void cert_nonExistentCertFileCausesMissingInput() {
        assertMissingInput(() ->
                SopCLI.execute("encrypt", "invalid.asc"));
    }

    @Test
    public void cert_unsupportedAsymmetricAlgorithmCausesUnsupportedAsymAlg() throws IOException, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.CertCannotEncrypt, SOPGPException.BadData {
        when(encrypt.withCert((InputStream) any())).thenThrow(new SOPGPException.UnsupportedAsymmetricAlgo("Unsupported asymmetric algorithm.", new Exception()));
        File certFile = File.createTempFile("cert", ".asc");
        assertUnsupportedAsymmetricAlgo(() ->
                SopCLI.execute("encrypt", certFile.getAbsolutePath()));
    }

    @Test
    public void cert_certCannotEncryptCausesCertCannotEncrypt() throws IOException, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.CertCannotEncrypt, SOPGPException.BadData {
        when(encrypt.withCert((InputStream) any())).thenThrow(new SOPGPException.CertCannotEncrypt("Certificate cannot encrypt.", new Exception()));
        File certFile = File.createTempFile("cert", ".asc");
        assertCertCannotEncrypt(() ->
                SopCLI.execute("encrypt", certFile.getAbsolutePath()));
    }

    @Test
    public void cert_badDataCausesBadData() throws IOException, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.CertCannotEncrypt, SOPGPException.BadData {
        when(encrypt.withCert((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        File certFile = File.createTempFile("cert", ".asc");
        assertBadData(() ->
                SopCLI.execute("encrypt", certFile.getAbsolutePath()));
    }

    @Test
    public void noArmor_notCalledByDefault() throws IOException {
        File passwordFile = TestFileUtil.writeTempStringFile("clownfish");
        assertSuccess(() ->
                SopCLI.execute("encrypt", "--with-password", passwordFile.getAbsolutePath()));
        verify(encrypt, never()).noArmor();
    }

    @Test
    public void noArmor_callGetsPassedDown() throws IOException {
        File passwordFile = TestFileUtil.writeTempStringFile("monkey");
        assertSuccess(() ->
                SopCLI.execute("encrypt", "--with-password", passwordFile.getAbsolutePath(), "--no-armor"));
        verify(encrypt, times(1)).noArmor();
    }

    @Test
    public void writeTo_ioExceptionCausesGenericError() throws IOException {
        when(encrypt.plaintext((InputStream) any())).thenReturn(new ReadyWithResult<EncryptionResult>() {
            @Override
            public EncryptionResult writeTo(@NotNull OutputStream outputStream) throws IOException, SOPGPException {
                throw new IOException();
            }
        });
        File passwordFile = TestFileUtil.writeTempStringFile("wildcat");
        assertGenericError(() ->
                SopCLI.execute("encrypt", "--with-password", passwordFile.getAbsolutePath()));
    }
}
