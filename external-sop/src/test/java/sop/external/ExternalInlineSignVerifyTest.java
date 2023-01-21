// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import sop.ByteArrayAndResult;
import sop.Verification;
import sop.enums.InlineSignAs;
import sop.exception.SOPGPException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sop.external.JUtils.assertSignedBy;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalInlineSignVerifyTest extends AbstractExternalSOPTest {

    private static final String BEGIN_PGP_MESSAGE = "-----BEGIN PGP MESSAGE-----\n";
    private static final byte[] BEGIN_PGP_MESSAGE_BYTES = BEGIN_PGP_MESSAGE.getBytes(StandardCharsets.UTF_8);
    private static final String BEGIN_PGP_SIGNED_MESSAGE = "-----BEGIN PGP SIGNED MESSAGE-----\n";
    private static final byte[] BEGIN_PGP_SIGNED_MESSAGE_BYTES = BEGIN_PGP_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);

    @Test
    public void inlineSignVerifyAlice() throws IOException {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported

        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = getSop().inlineSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .getBytes();

        JUtils.assertArrayStartsWith(inlineSigned, BEGIN_PGP_MESSAGE_BYTES);

        ByteArrayAndResult<List<Verification>> bytesAndResult = getSop().inlineVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(inlineSigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        List<Verification> verificationList = bytesAndResult.getResult();
        assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT);
    }

    @Test
    public void inlineSignVerifyAliceNoArmor() throws IOException {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported

        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = getSop().inlineSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .noArmor()
                .data(message)
                .getBytes();

        assertFalse(JUtils.arrayStartsWith(inlineSigned, BEGIN_PGP_MESSAGE_BYTES));

        ByteArrayAndResult<List<Verification>> bytesAndResult = getSop().inlineVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(inlineSigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        List<Verification> verificationList = bytesAndResult.getResult();
        assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT);
    }

    @Test
    public void clearsignVerifyAlice() throws IOException {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported

        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] clearsigned = getSop().inlineSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(InlineSignAs.clearsigned)
                .data(message)
                .getBytes();

        JUtils.assertArrayStartsWith(clearsigned, BEGIN_PGP_SIGNED_MESSAGE_BYTES);

        ByteArrayAndResult<List<Verification>> bytesAndResult = getSop().inlineVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(clearsigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        List<Verification> verificationList = bytesAndResult.getResult();
        assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT);
    }

    @Test
    public void inlineVerifyCompareSignatureDate() throws IOException {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported
        ignoreIf("sqop", Is.leq, "0.27.2"); // returns 1 instead of 3 (NO_SIGNATURE)

        byte[] message = TestData.ALICE_INLINE_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date signatureDate = TestData.ALICE_INLINE_SIGNED_MESSAGE_DATE;

        ByteArrayAndResult<List<Verification>> bytesAndResult = getSop().inlineVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult();
        List<Verification> verificationList = bytesAndResult.getResult();
        assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT, signatureDate);
    }

    @Test
    public void assertNotBeforeThrowsNoSignature() {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported
        ignoreIf("sqop", Is.leq, "0.27.2"); // returns 1 instead of 3 (NO_SIGNATURE)

        byte[] message = TestData.ALICE_INLINE_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date signatureDate = TestData.ALICE_INLINE_SIGNED_MESSAGE_DATE;
        Date afterSignature = new Date(signatureDate.getTime() + 1000); // 1 sec before sig

        assertThrows(SOPGPException.NoSignature.class, () -> getSop().inlineVerify()
                .notBefore(afterSignature)
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult());
    }

    @Test
    public void assertNotAfterThrowsNoSignature() {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported
        ignoreIf("sqop", Is.leq, "0.27.2"); // returns 1 instead of 3 (NO_SIGNATURE)

        byte[] message = TestData.ALICE_INLINE_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date signatureDate = TestData.ALICE_INLINE_SIGNED_MESSAGE_DATE;
        Date beforeSignature = new Date(signatureDate.getTime() - 1000); // 1 sec before sig

        assertThrows(SOPGPException.NoSignature.class, () -> getSop().inlineVerify()
                .notAfter(beforeSignature)
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult());
    }

    @Test
    public void inlineSignVerifyBob() throws IOException {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported

        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = getSop().inlineSign()
                .key(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .getBytes();

        JUtils.assertArrayStartsWith(inlineSigned, BEGIN_PGP_MESSAGE_BYTES);

        ByteArrayAndResult<List<Verification>> bytesAndResult = getSop().inlineVerify()
                .cert(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .data(inlineSigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        List<Verification> verificationList = bytesAndResult.getResult();
        assertSignedBy(verificationList, TestData.BOB_SIGNING_FINGERPRINT, TestData.BOB_PRIMARY_FINGERPRINT);
    }

    @Test
    public void inlineSignVerifyCarol() throws IOException {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported

        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = getSop().inlineSign()
                .key(TestData.CAROL_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .getBytes();

        JUtils.assertArrayStartsWith(inlineSigned, BEGIN_PGP_MESSAGE_BYTES);

        ByteArrayAndResult<List<Verification>> bytesAndResult = getSop().inlineVerify()
                .cert(TestData.CAROL_CERT.getBytes(StandardCharsets.UTF_8))
                .data(inlineSigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        List<Verification> verificationList = bytesAndResult.getResult();
        assertSignedBy(verificationList, TestData.CAROL_SIGNING_FINGERPRINT, TestData.CAROL_PRIMARY_FINGERPRINT);
    }

    @Test
    public void inlineSignVerifyProtectedKey() throws IOException {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported

        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = getSop().inlineSign()
                .withKeyPassword(TestData.PASSWORD)
                .key(TestData.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(InlineSignAs.binary)
                .data(message)
                .getBytes();

        ByteArrayAndResult<List<Verification>> bytesAndResult = getSop().inlineVerify()
                .cert(TestData.PASSWORD_PROTECTED_CERT.getBytes(StandardCharsets.UTF_8))
                .data(inlineSigned)
                .toByteArrayAndResult();

        List<Verification> verificationList = bytesAndResult.getResult();
        assertSignedBy(verificationList, TestData.PASSWORD_PROTECTED_SIGNING_FINGERPRINT, TestData.PASSWORD_PROTECTED_PRIMARY_FINGERPRINT);
    }

}
