// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.ByteArrayAndResult;
import sop.SOP;
import sop.Verification;
import sop.enums.InlineSignAs;
import sop.exception.SOPGPException;
import sop.testsuite.JUtils;
import sop.testsuite.TestData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIf("sop.operation.AbstractSOPTest#hasBackends")
public class InlineSignInlineVerifyTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void inlineSignVerifyAlice(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = sop.inlineSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .getBytes();

        JUtils.assertArrayStartsWith(inlineSigned, TestData.BEGIN_PGP_MESSAGE);

        ByteArrayAndResult<List<Verification>> bytesAndResult = sop.inlineVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(inlineSigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        List<Verification> verificationList = bytesAndResult.getResult();
        JUtils.assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void inlineSignVerifyAliceNoArmor(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = sop.inlineSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .noArmor()
                .data(message)
                .getBytes();

        Assertions.assertFalse(JUtils.arrayStartsWith(inlineSigned, TestData.BEGIN_PGP_MESSAGE));

        ByteArrayAndResult<List<Verification>> bytesAndResult = sop.inlineVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(inlineSigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        List<Verification> verificationList = bytesAndResult.getResult();
        JUtils.assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void clearsignVerifyAlice(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] clearsigned = sop.inlineSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(InlineSignAs.clearsigned)
                .data(message)
                .getBytes();

        JUtils.assertArrayStartsWith(clearsigned, TestData.BEGIN_PGP_SIGNED_MESSAGE);

        ByteArrayAndResult<List<Verification>> bytesAndResult = sop.inlineVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(clearsigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        List<Verification> verificationList = bytesAndResult.getResult();
        JUtils.assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void inlineVerifyCompareSignatureDate(SOP sop) throws IOException {
        byte[] message = TestData.ALICE_INLINE_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date signatureDate = TestData.ALICE_INLINE_SIGNED_MESSAGE_DATE;

        ByteArrayAndResult<List<Verification>> bytesAndResult = sop.inlineVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult();
        List<Verification> verificationList = bytesAndResult.getResult();
        JUtils.assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT, signatureDate);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void assertNotBeforeThrowsNoSignature(SOP sop) {
        byte[] message = TestData.ALICE_INLINE_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date signatureDate = TestData.ALICE_INLINE_SIGNED_MESSAGE_DATE;
        Date afterSignature = new Date(signatureDate.getTime() + 1000); // 1 sec before sig

        assertThrows(SOPGPException.NoSignature.class, () -> sop.inlineVerify()
                .notBefore(afterSignature)
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void assertNotAfterThrowsNoSignature(SOP sop) {
        byte[] message = TestData.ALICE_INLINE_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date signatureDate = TestData.ALICE_INLINE_SIGNED_MESSAGE_DATE;
        Date beforeSignature = new Date(signatureDate.getTime() - 1000); // 1 sec before sig

        assertThrows(SOPGPException.NoSignature.class, () -> sop.inlineVerify()
                .notAfter(beforeSignature)
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void inlineSignVerifyBob(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = sop.inlineSign()
                .key(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .getBytes();

        JUtils.assertArrayStartsWith(inlineSigned, TestData.BEGIN_PGP_MESSAGE);

        ByteArrayAndResult<List<Verification>> bytesAndResult = sop.inlineVerify()
                .cert(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .data(inlineSigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        List<Verification> verificationList = bytesAndResult.getResult();
        JUtils.assertSignedBy(verificationList, TestData.BOB_SIGNING_FINGERPRINT, TestData.BOB_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void inlineSignVerifyCarol(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = sop.inlineSign()
                .key(TestData.CAROL_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .getBytes();

        JUtils.assertArrayStartsWith(inlineSigned, TestData.BEGIN_PGP_MESSAGE);

        ByteArrayAndResult<List<Verification>> bytesAndResult = sop.inlineVerify()
                .cert(TestData.CAROL_CERT.getBytes(StandardCharsets.UTF_8))
                .data(inlineSigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        List<Verification> verificationList = bytesAndResult.getResult();
        JUtils.assertSignedBy(verificationList, TestData.CAROL_SIGNING_FINGERPRINT, TestData.CAROL_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void inlineSignVerifyProtectedKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = sop.inlineSign()
                .withKeyPassword(TestData.PASSWORD)
                .key(TestData.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(InlineSignAs.binary)
                .data(message)
                .getBytes();

        ByteArrayAndResult<List<Verification>> bytesAndResult = sop.inlineVerify()
                .cert(TestData.PASSWORD_PROTECTED_CERT.getBytes(StandardCharsets.UTF_8))
                .data(inlineSigned)
                .toByteArrayAndResult();

        List<Verification> verificationList = bytesAndResult.getResult();
        JUtils.assertSignedBy(verificationList, TestData.PASSWORD_PROTECTED_SIGNING_FINGERPRINT, TestData.PASSWORD_PROTECTED_PRIMARY_FINGERPRINT);
    }

}
