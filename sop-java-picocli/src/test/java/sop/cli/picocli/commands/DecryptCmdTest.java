// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import sop.DecryptionResult;
import sop.ReadyWithResult;
import sop.SOP;
import sop.SessionKey;
import sop.Verification;
import sop.cli.picocli.SopCLI;
import sop.cli.picocli.TestFileUtil;
import sop.exception.SOPGPException;
import sop.operation.Decrypt;
import sop.util.HexUtil;
import sop.util.UTCUtil;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sop.testsuite.assertions.SopExecutionAssertions.assertBadData;
import static sop.testsuite.assertions.SopExecutionAssertions.assertCannotDecrypt;
import static sop.testsuite.assertions.SopExecutionAssertions.assertGenericError;
import static sop.testsuite.assertions.SopExecutionAssertions.assertIncompleteVerification;
import static sop.testsuite.assertions.SopExecutionAssertions.assertKeyIsProtected;
import static sop.testsuite.assertions.SopExecutionAssertions.assertMissingArg;
import static sop.testsuite.assertions.SopExecutionAssertions.assertMissingInput;
import static sop.testsuite.assertions.SopExecutionAssertions.assertOutputExists;
import static sop.testsuite.assertions.SopExecutionAssertions.assertPasswordNotHumanReadable;
import static sop.testsuite.assertions.SopExecutionAssertions.assertSuccess;
import static sop.testsuite.assertions.SopExecutionAssertions.assertUnsupportedAsymmetricAlgo;
import static sop.testsuite.assertions.SopExecutionAssertions.assertUnsupportedOption;

public class DecryptCmdTest {

    private Decrypt decrypt;

    @BeforeEach
    public void mockComponents() throws SOPGPException.UnsupportedOption, SOPGPException.MissingArg, SOPGPException.BadData, SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.PasswordNotHumanReadable, SOPGPException.CannotDecrypt, IOException {
        SOP sop = mock(SOP.class);
        decrypt = mock(Decrypt.class);

        when(decrypt.verifyNotAfter(any())).thenReturn(decrypt);
        when(decrypt.verifyNotBefore(any())).thenReturn(decrypt);
        when(decrypt.withPassword(any())).thenReturn(decrypt);
        when(decrypt.withSessionKey(any())).thenReturn(decrypt);
        when(decrypt.withKey((InputStream) any())).thenReturn(decrypt);
        when(decrypt.ciphertext((InputStream) any())).thenReturn(nopReadyWithResult());

        when(sop.decrypt()).thenReturn(decrypt);

        SopCLI.setSopInstance(sop);
    }

    private static ReadyWithResult<DecryptionResult> nopReadyWithResult() {
        return new ReadyWithResult<DecryptionResult>() {
            @Override
            public DecryptionResult writeTo(OutputStream outputStream) {
                return new DecryptionResult(null, Collections.emptyList());
            }
        };
    }

    @Test
    public void missingArgumentsExceptionCausesExit19() throws SOPGPException.MissingArg, SOPGPException.BadData, SOPGPException.CannotDecrypt, IOException {
        when(decrypt.ciphertext((InputStream) any())).thenThrow(new SOPGPException.MissingArg("Missing arguments."));
        assertMissingArg(() -> SopCLI.execute("decrypt"));
    }

    @Test
    public void badDataExceptionCausesExit41() throws SOPGPException.MissingArg, SOPGPException.BadData, SOPGPException.CannotDecrypt, IOException {
        when(decrypt.ciphertext((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        assertBadData(() -> SopCLI.execute("decrypt"));
    }

    @Test
    public void assertNotHumanReadablePasswordCausesExit31() throws SOPGPException.PasswordNotHumanReadable,
            SOPGPException.UnsupportedOption, IOException {
        File passwordFile = TestFileUtil.writeTempStringFile("pretendThisIsNotReadable");
        when(decrypt.withPassword(any())).thenThrow(new SOPGPException.PasswordNotHumanReadable());
        assertPasswordNotHumanReadable(() ->
                SopCLI.execute("decrypt", "--with-password", passwordFile.getAbsolutePath())
        );
    }

    @Test
    public void assertWithPasswordPassesPasswordDown() throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption, IOException {
        File passwordFile = TestFileUtil.writeTempStringFile("orange");
        assertSuccess(() -> SopCLI.execute("decrypt", "--with-password", passwordFile.getAbsolutePath()));
        verify(decrypt, times(1)).withPassword("orange");
    }

    @Test
    public void assertUnsupportedWithPasswordCausesExit37() throws SOPGPException.PasswordNotHumanReadable, SOPGPException.UnsupportedOption, IOException {
        File passwordFile = TestFileUtil.writeTempStringFile("swordfish");
        when(decrypt.withPassword(any())).thenThrow(new SOPGPException.UnsupportedOption("Decrypting with password not supported."));
        assertUnsupportedOption(() ->
                SopCLI.execute("decrypt", "--with-password", passwordFile.getAbsolutePath())
        );
    }

    @Test
    public void assertDefaultTimeRangesAreUsedIfNotOverwritten() throws SOPGPException.UnsupportedOption {
        Date now = new Date();
        assertSuccess(() -> SopCLI.execute("decrypt"));
        verify(decrypt, times(1)).verifyNotBefore(AbstractSopCmd.BEGINNING_OF_TIME);
        verify(decrypt, times(1)).verifyNotAfter(
                ArgumentMatchers.argThat(argument -> {
                    // allow 1-second difference
                    return Math.abs(now.getTime() - argument.getTime()) <= 1000;
                }));
    }

    @Test
    public void assertVerifyNotAfterAndBeforeDashResultsInMaxTimeRange() throws SOPGPException.UnsupportedOption {
        assertSuccess(() ->
                SopCLI.execute("decrypt", "--verify-not-before", "-", "--verify-not-after", "-"));
        verify(decrypt, times(1)).verifyNotBefore(AbstractSopCmd.BEGINNING_OF_TIME);
        verify(decrypt, times(1)).verifyNotAfter(AbstractSopCmd.END_OF_TIME);
    }

    @Test
    public void assertVerifyNotAfterAndBeforeNowResultsInMinTimeRange() throws SOPGPException.UnsupportedOption {
        Date now = new Date();
        ArgumentMatcher<Date> isMaxOneSecOff = argument -> {
            // Allow less than 1-second difference
            return Math.abs(now.getTime() - argument.getTime()) <= 1000;
        };

        assertSuccess(() ->
                SopCLI.execute("decrypt", "--verify-not-before", "now", "--verify-not-after", "now"));
        verify(decrypt, times(1)).verifyNotAfter(ArgumentMatchers.argThat(isMaxOneSecOff));
        verify(decrypt, times(1)).verifyNotBefore(ArgumentMatchers.argThat(isMaxOneSecOff));
    }

    @Test
    public void assertMalformedDateInNotBeforeCausesExit1() {
        // ParserException causes exit(1)
        assertGenericError(() ->
                SopCLI.execute("decrypt", "--verify-not-before", "invalid"));
    }

    @Test
    public void assertMalformedDateInNotAfterCausesExit1() {
        // ParserException causes exit(1)
        assertGenericError(() ->
                SopCLI.execute("decrypt", "--verify-not-after", "invalid"));
    }

    @Test
    public void assertUnsupportedNotAfterCausesExit37() throws SOPGPException.UnsupportedOption {
        when(decrypt.verifyNotAfter(any())).thenThrow(
                new SOPGPException.UnsupportedOption("Setting upper signature date boundary not supported."));
        assertUnsupportedOption(() ->
                SopCLI.execute("decrypt", "--verify-not-after", "now"));
    }

    @Test
    public void assertUnsupportedNotBeforeCausesExit37() throws SOPGPException.UnsupportedOption {
        when(decrypt.verifyNotBefore(any())).thenThrow(
                new SOPGPException.UnsupportedOption("Setting lower signature date boundary not supported."));
        assertUnsupportedOption(() ->
                SopCLI.execute("decrypt", "--verify-not-before", "now"));
    }

    @Test
    public void assertExistingSessionKeyOutFileCausesExit59() throws IOException {
        File tempFile = File.createTempFile("existing-session-key-", ".tmp");
        tempFile.deleteOnExit();
        assertOutputExists(() ->
                SopCLI.execute("decrypt", "--session-key-out", tempFile.getAbsolutePath()));
    }

    @Test
    public void assertWhenSessionKeyCannotBeExtractedExit37() throws IOException {
        Path tempDir = Files.createTempDirectory("session-key-out-dir");
        File tempFile = new File(tempDir.toFile(), "session-key");
        tempFile.deleteOnExit();
        assertUnsupportedOption(() ->
                SopCLI.execute("decrypt", "--session-key-out", tempFile.getAbsolutePath()));
    }

    @Test
    public void assertSessionKeyAndVerificationsIsProperlyWrittenToSessionKeyFile() throws SOPGPException.CannotDecrypt, SOPGPException.MissingArg, SOPGPException.BadData, IOException, ParseException {
        Date signDate = UTCUtil.parseUTCDate("2022-11-07T15:01:24Z");
        String keyFP = "F9E6F53F7201C60A87064EAB0B27F2B0760A1209";
        String certFP = "4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B";
        Verification verification = new Verification(signDate, keyFP, certFP);
        SessionKey sessionKey = SessionKey.fromString("9:C7CBDAF42537776F12509B5168793C26B93294E5ABDFA73224FB0177123E9137");
        when(decrypt.ciphertext((InputStream) any())).thenReturn(new ReadyWithResult<DecryptionResult>() {
            @Override
            public DecryptionResult writeTo(OutputStream outputStream) {
                return new DecryptionResult(
                        sessionKey,
                        Collections.singletonList(verification)
                );
            }
        });
        Path tempDir = Files.createTempDirectory("session-key-out-dir");
        File sessionKeyFile = new File(tempDir.toFile(), "session-key");
        sessionKeyFile.deleteOnExit();
        File verificationsFile = new File(tempDir.toFile(), "verifications");
        File keyFile = new File(tempDir.toFile(), "key.asc");
        keyFile.createNewFile();
        assertSuccess(() ->
                SopCLI.execute("decrypt", "--session-key-out", sessionKeyFile.getAbsolutePath(),
                        "--verifications-out", verificationsFile.getAbsolutePath(), "--verify-with",
                        keyFile.getAbsolutePath()));

        ByteArrayOutputStream bytesInFile = new ByteArrayOutputStream();
        try (FileInputStream fileIn = new FileInputStream(sessionKeyFile)) {
            byte[] buf = new byte[32];
            int read = fileIn.read(buf);
            while (read != -1) {
                bytesInFile.write(buf, 0, read);
                read = fileIn.read(buf);
            }
        }

        SessionKey parsedSessionKey = SessionKey.fromString(bytesInFile.toString());
        assertEquals(sessionKey, parsedSessionKey);

        bytesInFile = new ByteArrayOutputStream();
        try (FileInputStream fileIn = new FileInputStream(verificationsFile)) {
            byte[] buf = new byte[32];
            int read = fileIn.read(buf);
            while (read != -1) {
                bytesInFile.write(buf, 0, read);
                read = fileIn.read(buf);
            }
        }

        Verification parsedVerification = Verification.fromString(bytesInFile.toString());
        assertEquals(verification, parsedVerification);
    }

    @Test
    public void assertUnableToDecryptExceptionResultsInExit29() throws SOPGPException.CannotDecrypt, SOPGPException.MissingArg, SOPGPException.BadData, IOException {
        when(decrypt.ciphertext((InputStream) any())).thenThrow(new SOPGPException.CannotDecrypt());
        assertCannotDecrypt(() ->
                SopCLI.execute("decrypt"));
    }

    @Test
    public void assertNoVerificationsIsOkay() throws SOPGPException.CannotDecrypt, SOPGPException.MissingArg, SOPGPException.BadData, IOException {
        File tempFile = File.createTempFile("verify-with-", ".tmp");
        File verifyOut = new File(tempFile.getParent(), "verifications.out");
        verifyOut.deleteOnExit();
        when(decrypt.ciphertext((InputStream) any())).thenReturn(new ReadyWithResult<DecryptionResult>() {
            @Override
            public DecryptionResult writeTo(@Nonnull OutputStream outputStream) throws SOPGPException.NoSignature {
                return new DecryptionResult(null, Collections.emptyList());
            }
        });
        assertSuccess(() ->
                SopCLI.execute("decrypt", "--verify-with", tempFile.getAbsolutePath(), "--verifications-out",
                        verifyOut.getAbsolutePath()));
    }

    @Test
    public void badDataInVerifyWithCausesExit41() throws IOException, SOPGPException.BadData {
        when(decrypt.verifyWithCert((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        File tempFile = File.createTempFile("verify-with-", ".tmp");
        assertBadData(() ->
                SopCLI.execute("decrypt", "--verify-with", tempFile.getAbsolutePath()));
    }

    @Test
    public void unexistentCertFileCausesExit61() {
        assertMissingInput(() ->
                SopCLI.execute("decrypt", "--verify-with", "invalid"));
    }

    @Test
    public void existingVerifyOutCausesExit59() throws IOException {
        File certFile = File.createTempFile("existing-verify-out-cert", ".asc");
        File existingVerifyOut = File.createTempFile("existing-verify-out", ".tmp");

        assertOutputExists(() -> SopCLI.execute("decrypt", "--verifications-out",
                existingVerifyOut.getAbsolutePath(), "--verify-with", certFile.getAbsolutePath()));
    }

    @Test
    public void verifyOutIsProperlyWritten() throws IOException, SOPGPException.CannotDecrypt, SOPGPException.MissingArg, SOPGPException.BadData, ParseException {
        File certFile = File.createTempFile("verify-out-cert", ".asc");
        File verifyOut = new File(certFile.getParent(), "verify-out.txt");
        if (verifyOut.exists()) {
            verifyOut.delete();
        }
        verifyOut.deleteOnExit();
        Date date = UTCUtil.parseUTCDate("2021-07-11T20:58:23Z");
        when(decrypt.ciphertext((InputStream) any())).thenReturn(new ReadyWithResult<DecryptionResult>() {
            @Override
            public DecryptionResult writeTo(OutputStream outputStream) {
                return new DecryptionResult(null, Collections.singletonList(
                        new Verification(
                                date,
                                "1B66A707819A920925BC6777C3E0AFC0B2DFF862",
                                "C8CD564EBF8D7BBA90611D8D071773658BF6BF86"))
                );
            }
        });

        assertSuccess(() ->
                SopCLI.execute("decrypt", "--verifications-out", verifyOut.getAbsolutePath(),
                        "--verify-with", certFile.getAbsolutePath()));
        try (BufferedReader reader = new BufferedReader(new FileReader(verifyOut))) {
            String line = reader.readLine();
            assertEquals("2021-07-11T20:58:23Z 1B66A707819A920925BC6777C3E0AFC0B2DFF862 C8CD564EBF8D7BBA90611D8D071773658BF6BF86", line);
        }
    }

    @Test
    public void assertWithSessionKeyIsPassedDown() throws SOPGPException.UnsupportedOption, IOException {
        SessionKey key1 = new SessionKey((byte) 9, HexUtil.hexToBytes("C7CBDAF42537776F12509B5168793C26B93294E5ABDFA73224FB0177123E9137"));
        SessionKey key2 = new SessionKey((byte) 9, HexUtil.hexToBytes("FCA4BEAF687F48059CACC14FB019125CD57392BAB7037C707835925CBF9F7BCD"));

        File sessionKeyFile1 = TestFileUtil.writeTempStringFile(key1.toString());
        File sessionKeyFile2 = TestFileUtil.writeTempStringFile(key2.toString());

        assertSuccess(() ->
                SopCLI.execute("decrypt",
                        "--with-session-key", sessionKeyFile1.getAbsolutePath(),
                        "--with-session-key", sessionKeyFile2.getAbsolutePath()));
        verify(decrypt).withSessionKey(key1);
        verify(decrypt).withSessionKey(key2);
    }

    @Test
    public void assertMalformedSessionKeysResultInExit1() throws IOException {
        File sessionKeyFile = TestFileUtil.writeTempStringFile("C7CBDAF42537776F12509B5168793C26B93294E5ABDFA73224FB0177123E9137");
        assertGenericError(() ->
                SopCLI.execute("decrypt",
                "--with-session-key", sessionKeyFile.getAbsolutePath()));
    }

    @Test
    public void assertBadDataInKeysResultsInExit41() throws SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.BadData, IOException {
        when(decrypt.withKey((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        File tempKeyFile = File.createTempFile("key-", ".tmp");
        assertBadData(() -> SopCLI.execute("decrypt", tempKeyFile.getAbsolutePath()));
    }

    @Test
    public void assertKeyFileNotFoundCausesExit61() {
        assertMissingInput(() -> SopCLI.execute("decrypt", "nonexistent-key"));
    }

    @Test
    public void assertProtectedKeyCausesExit67() throws IOException, SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.BadData {
        when(decrypt.withKey((InputStream) any())).thenThrow(new SOPGPException.KeyIsProtected());
        File tempKeyFile = File.createTempFile("key-", ".tmp");
        assertKeyIsProtected(() -> SopCLI.execute("decrypt", tempKeyFile.getAbsolutePath()));
    }

    @Test
    public void assertUnsupportedAlgorithmExceptionCausesExit13() throws SOPGPException.KeyIsProtected, SOPGPException.UnsupportedAsymmetricAlgo, SOPGPException.BadData, IOException {
        when(decrypt.withKey((InputStream) any())).thenThrow(new SOPGPException.UnsupportedAsymmetricAlgo("Unsupported asymmetric algorithm.", new IOException()));
        File tempKeyFile = File.createTempFile("key-", ".tmp");
        assertUnsupportedAsymmetricAlgo(() ->
                SopCLI.execute("decrypt", tempKeyFile.getAbsolutePath()));
    }

    @Test
    public void assertMissingPassphraseFileCausesExit61() {
        assertMissingInput(() ->
                SopCLI.execute("decrypt", "--with-password", "missing"));
    }

    @Test
    public void assertMissingSessionKeyFileCausesExit61() {
        assertMissingInput(() ->
                SopCLI.execute("decrypt", "--with-session-key", "missing"));
    }

    @Test
    public void verifyOutWithoutVerifyWithCausesExit23() {
        assertIncompleteVerification(() ->
                SopCLI.execute("decrypt", "--verifications-out", "out.file"));
    }
}
