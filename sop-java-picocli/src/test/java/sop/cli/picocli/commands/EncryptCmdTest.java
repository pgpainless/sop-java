// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
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
    @ExpectSystemExitWithStatus(SOPGPException.MissingArg.EXIT_CODE)
    public void missingBothPasswordAndCertFileCauseExit19() {
        SopCLI.main(new String[] {"encrypt", "--no-armor"});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.UnsupportedOption.EXIT_CODE)
    public void as_unsupportedEncryptAsCausesExit37() throws SOPGPException.UnsupportedOption {
        when(encrypt.mode(any())).thenThrow(new SOPGPException.UnsupportedOption("Setting encryption mode not supported."));

        SopCLI.main(new String[] {"encrypt", "--as", "Binary"});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.UnsupportedOption.EXIT_CODE)
    public void as_invalidModeOptionCausesExit37() {
        SopCLI.main(new String[] {"encrypt", "--as", "invalid"});
    }

    @Test
    public void as_modeIsPassedDown() throws SOPGPException.UnsupportedOption, IOException {
        File passwordFile = TestFileUtil.writeTempStringFile("0rbit");
        for (EncryptAs mode : EncryptAs.values()) {
            SopCLI.main(new String[] {"encrypt", "--as", mode.name(), "--with-password", passwordFile.getAbsolutePath()});
            verify(encrypt, times(1)).mode(mode);
        }
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.PasswordNotHumanReadable.EXIT_CODE)
    public void withPassword_notHumanReadablePasswordCausesExit31() throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption, IOException {
        when(encrypt.withPassword("pretendThisIsNotReadable")).thenThrow(new SOPGPException.PasswordNotHumanReadable());
        File passwordFile = TestFileUtil.writeTempStringFile("pretendThisIsNotReadable");
        SopCLI.main(new String[] {"encrypt", "--with-password", passwordFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.UnsupportedOption.EXIT_CODE)
    public void withPassword_unsupportedWithPasswordCausesExit37() throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption, IOException {
        when(encrypt.withPassword(any())).thenThrow(new SOPGPException.UnsupportedOption("Encrypting with password not supported."));
        File passwordFile = TestFileUtil.writeTempStringFile("orange");
        SopCLI.main(new String[] {"encrypt", "--with-password", passwordFile.getAbsolutePath()});
    }

    @Test
    public void signWith_multipleTimesGetPassedDown() throws IOException, SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.KeyCannotSign, SOPGPException.BadData {
        File keyFile1 = File.createTempFile("sign-with-1-", ".asc");
        File keyFile2 = File.createTempFile("sign-with-2-", ".asc");
        File passwordFile = TestFileUtil.writeTempStringFile("password");
        SopCLI.main(new String[] {"encrypt", "--with-password", passwordFile.getAbsolutePath(), "--sign-with", keyFile1.getAbsolutePath(), "--sign-with", keyFile2.getAbsolutePath()});
        verify(encrypt, times(2)).signWith((InputStream) any());
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.MissingInput.EXIT_CODE)
    public void signWith_nonExistentKeyFileCausesExit61() {
        SopCLI.main(new String[] {"encrypt", "--with-password", "admin", "--sign-with", "nonExistent.asc"});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.KeyIsProtected.EXIT_CODE)
    public void signWith_keyIsProtectedCausesExit67() throws SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.KeyCannotSign, SOPGPException.BadData, IOException {
        when(encrypt.signWith((InputStream) any())).thenThrow(new SOPGPException.KeyIsProtected());
        File keyFile = File.createTempFile("sign-with", ".asc");
        File passwordFile = TestFileUtil.writeTempStringFile("starship");
        SopCLI.main(new String[] {"encrypt", "--sign-with", keyFile.getAbsolutePath(), "--with-password", passwordFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.UnsupportedAsymmetricAlgo.EXIT_CODE)
    public void signWith_unsupportedAsymmetricAlgoCausesExit13() throws SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.KeyCannotSign, SOPGPException.BadData, IOException {
        when(encrypt.signWith((InputStream) any())).thenThrow(new SOPGPException.UnsupportedAsymmetricAlgo("Unsupported asymmetric algorithm.", new Exception()));
        File keyFile = File.createTempFile("sign-with", ".asc");
        File passwordFile = TestFileUtil.writeTempStringFile("123456");
        SopCLI.main(new String[] {"encrypt", "--with-password", passwordFile.getAbsolutePath(), "--sign-with", keyFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.KeyCannotSign.EXIT_CODE)
    public void signWith_certCannotSignCausesExit79() throws IOException, SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.KeyCannotSign, SOPGPException.BadData {
        when(encrypt.signWith((InputStream) any())).thenThrow(new SOPGPException.KeyCannotSign());
        File keyFile = File.createTempFile("sign-with", ".asc");
        File passwordFile = TestFileUtil.writeTempStringFile("dragon");
        SopCLI.main(new String[] {"encrypt", "--with-password", passwordFile.getAbsolutePath(), "--sign-with", keyFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.BadData.EXIT_CODE)
    public void signWith_badDataCausesExit41() throws SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.KeyCannotSign, SOPGPException.BadData, IOException {
        when(encrypt.signWith((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        File keyFile = File.createTempFile("sign-with", ".asc");
        File passwordFile = TestFileUtil.writeTempStringFile("orange");
        SopCLI.main(new String[] {"encrypt", "--with-password", passwordFile.getAbsolutePath(), "--sign-with", keyFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.MissingInput.EXIT_CODE)
    public void cert_nonExistentCertFileCausesExit61() {
        SopCLI.main(new String[] {"encrypt", "invalid.asc"});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.UnsupportedAsymmetricAlgo.EXIT_CODE)
    public void cert_unsupportedAsymmetricAlgorithmCausesExit13() throws IOException, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.CertCannotEncrypt, SOPGPException.BadData {
        when(encrypt.withCert((InputStream) any())).thenThrow(new SOPGPException.UnsupportedAsymmetricAlgo("Unsupported asymmetric algorithm.", new Exception()));
        File certFile = File.createTempFile("cert", ".asc");
        SopCLI.main(new String[] {"encrypt", certFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.CertCannotEncrypt.EXIT_CODE)
    public void cert_certCannotEncryptCausesExit17() throws IOException, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.CertCannotEncrypt, SOPGPException.BadData {
        when(encrypt.withCert((InputStream) any())).thenThrow(new SOPGPException.CertCannotEncrypt("Certificate cannot encrypt.", new Exception()));
        File certFile = File.createTempFile("cert", ".asc");
        SopCLI.main(new String[] {"encrypt", certFile.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.BadData.EXIT_CODE)
    public void cert_badDataCausesExit41() throws IOException, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.CertCannotEncrypt, SOPGPException.BadData {
        when(encrypt.withCert((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        File certFile = File.createTempFile("cert", ".asc");
        SopCLI.main(new String[] {"encrypt", certFile.getAbsolutePath()});
    }

    @Test
    public void noArmor_notCalledByDefault() throws IOException {
        File passwordFile = TestFileUtil.writeTempStringFile("clownfish");
        SopCLI.main(new String[] {"encrypt", "--with-password", passwordFile.getAbsolutePath()});
        verify(encrypt, never()).noArmor();
    }

    @Test
    public void noArmor_callGetsPassedDown() throws IOException {
        File passwordFile = TestFileUtil.writeTempStringFile("monkey");
        SopCLI.main(new String[] {"encrypt", "--with-password", passwordFile.getAbsolutePath(), "--no-armor"});
        verify(encrypt, times(1)).noArmor();
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    public void writeTo_ioExceptionCausesExit1() throws IOException {
        when(encrypt.plaintext((InputStream) any())).thenReturn(new ReadyWithResult<EncryptionResult>() {
            @Override
            public EncryptionResult writeTo(@NotNull OutputStream outputStream) throws IOException, SOPGPException {
                throw new IOException();
            }
        });
        File passwordFile = TestFileUtil.writeTempStringFile("wildcat");
        SopCLI.main(new String[] {"encrypt", "--with-password", passwordFile.getAbsolutePath()});
    }
}
