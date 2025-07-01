// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;
import sop.Verification;
import sop.enums.SignAs;
import sop.enums.SignatureMode;
import sop.exception.SOPGPException;
import sop.testsuite.JUtils;
import sop.testsuite.TestData;
import sop.testsuite.assertions.VerificationListAssert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIf("sop.testsuite.operation.AbstractSOPTest#hasBackends")
public class DetachedSignDetachedVerifyTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void signVerifyWithAliceKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = assumeSupported(sop::detachedSign)
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = assumeSupported(sop::detachedVerify)
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        VerificationListAssert.assertThatVerificationList(verificationList)
                .isNotEmpty()
                .hasSingleItem()
                .issuedBy(TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT)
                .hasModeOrNull(SignatureMode.binary);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void signVerifyTextModeWithAliceKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = assumeSupported(sop::detachedSign)
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(SignAs.text)
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = assumeSupported(sop::detachedVerify)
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        VerificationListAssert.assertThatVerificationList(verificationList)
                .isNotEmpty()
                .hasSingleItem()
                .issuedBy(TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT)
                .hasModeOrNull(SignatureMode.text);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void verifyKnownMessageWithAliceCert(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signature = TestData.ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);

        List<Verification> verificationList = assumeSupported(sop::detachedVerify)
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        VerificationListAssert.assertThatVerificationList(verificationList)
                .isNotEmpty()
                .hasSingleItem()
                .issuedBy(TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void signVerifyWithBobKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = assumeSupported(sop::detachedSign)
                .key(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = assumeSupported(sop::detachedVerify)
                .cert(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        VerificationListAssert.assertThatVerificationList(verificationList)
                .isNotEmpty()
                .hasSingleItem()
                .issuedBy(TestData.BOB_SIGNING_FINGERPRINT, TestData.BOB_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void signVerifyWithCarolKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = assumeSupported(sop::detachedSign)
                .key(TestData.CAROL_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = assumeSupported(sop::detachedVerify)
                .cert(TestData.CAROL_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        VerificationListAssert.assertThatVerificationList(verificationList)
                .isNotEmpty()
                .hasSingleItem()
                .issuedBy(TestData.CAROL_SIGNING_FINGERPRINT, TestData.CAROL_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void signVerifyWithEncryptedKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = assumeSupported(sop::detachedSign)
                .key(TestData.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                .withKeyPassword(TestData.PASSWORD)
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        JUtils.assertArrayStartsWith(signature, TestData.BEGIN_PGP_SIGNATURE);

        List<Verification> verificationList = assumeSupported(sop::detachedVerify)
                .cert(TestData.PASSWORD_PROTECTED_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        VerificationListAssert.assertThatVerificationList(verificationList)
                .isNotEmpty()
                .hasSingleItem()
                .issuedBy(TestData.PASSWORD_PROTECTED_SIGNING_FINGERPRINT, TestData.PASSWORD_PROTECTED_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void signArmorVerifyWithBobKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = assumeSupported(sop::detachedSign)
                .key(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .noArmor()
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        byte[] armored = assumeSupported(sop::armor)
                .data(signature)
                .getBytes();

        List<Verification> verificationList = assumeSupported(sop::detachedVerify)
                .cert(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(armored)
                .data(message);

        VerificationListAssert.assertThatVerificationList(verificationList)
                .isNotEmpty()
                .hasSingleItem()
                .issuedBy(TestData.BOB_SIGNING_FINGERPRINT, TestData.BOB_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void verifyNotAfterThrowsNoSignature(SOP sop) {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signature = TestData.ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date beforeSignature = new Date(TestData.ALICE_DETACHED_SIGNED_MESSAGE_DATE.getTime() - 1000); // 1 sec before sig

        assertThrows(SOPGPException.NoSignature.class, () -> assumeSupported(sop::detachedVerify)
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .notAfter(beforeSignature)
                .signatures(signature)
                .data(message));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void verifyNotBeforeThrowsNoSignature(SOP sop) {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signature = TestData.ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date afterSignature = new Date(TestData.ALICE_DETACHED_SIGNED_MESSAGE_DATE.getTime() + 1000); // 1 sec after sig

        assertThrows(SOPGPException.NoSignature.class, () -> assumeSupported(sop::detachedVerify)
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .notBefore(afterSignature)
                .signatures(signature)
                .data(message));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void signWithAliceVerifyWithBobThrowsNoSignature(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signatures = assumeSupported(sop::detachedSign)
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        assertThrows(SOPGPException.NoSignature.class, () -> assumeSupported(sop::detachedVerify)
                .cert(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signatures)
                .data(message));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void signVerifyWithEncryptedKeyWithoutPassphraseFails(SOP sop) {
        assertThrows(SOPGPException.KeyIsProtected.class, () ->
                assumeSupported(sop::detachedSign)
                        .key(TestData.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                        .data(TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8))
                        .toByteArrayAndResult()
                        .getBytes());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void signWithProtectedKeyAndMultiplePassphrasesTest(SOP sop)
            throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = assumeSupported(sop::sign)
                .key(TestData.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                .withKeyPassword("wrong")
                .withKeyPassword(TestData.PASSWORD) // correct
                .withKeyPassword("wrong2")
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = assumeSupported(sop::verify)
                .cert(TestData.PASSWORD_PROTECTED_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        VerificationListAssert.assertThatVerificationList(verificationList)
                .isNotEmpty()
                .hasSingleItem()
                .issuedBy(TestData.PASSWORD_PROTECTED_SIGNING_FINGERPRINT, TestData.PASSWORD_PROTECTED_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void verifyMissingCertCausesMissingArg(SOP sop) {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        assertThrows(SOPGPException.MissingArg.class, () ->
                assumeSupported(sop::verify)
                        .signatures(TestData.ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8))
                        .data(message));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void signVerifyWithMultipleKeys(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signatures = assumeSupported(sop::detachedSign)
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .key(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = assumeSupported(sop::detachedVerify)
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .cert(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signatures)
                .data(message);

        VerificationListAssert.assertThatVerificationList(verificationList)
                .isNotEmpty()
                .sizeEquals(2)
                .containsVerificationBy(TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT)
                .containsVerificationBy(TestData.BOB_SIGNING_FINGERPRINT, TestData.BOB_PRIMARY_FINGERPRINT);
    }


}
