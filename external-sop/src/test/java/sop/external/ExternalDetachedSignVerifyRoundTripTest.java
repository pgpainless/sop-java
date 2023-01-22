// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;
import sop.Verification;
import sop.enums.SignAs;
import sop.exception.SOPGPException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sop.external.JUtils.assertArrayStartsWith;
import static sop.external.JUtils.assertSignedBy;

public class ExternalDetachedSignVerifyRoundTripTest extends AbstractExternalSOPTest {

    private static final String BEGIN_PGP_SIGNATURE = "-----BEGIN PGP SIGNATURE-----\n";
    private static final byte[] BEGIN_PGP_SIGNATURE_BYTES = BEGIN_PGP_SIGNATURE.getBytes(StandardCharsets.UTF_8);

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signVerifyWithAliceKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.detachedSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = sop.detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signVerifyTextModeWithAliceKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.detachedSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(SignAs.Text)
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = sop.detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void verifyKnownMessageWithAliceCert(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signature = TestData.ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);

        List<Verification> verificationList = sop.detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT, TestData.ALICE_DETACHED_SIGNED_MESSAGE_DATE);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signVerifyWithBobKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.detachedSign()
                .key(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = sop.detachedVerify()
                .cert(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, TestData.BOB_SIGNING_FINGERPRINT, TestData.BOB_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signVerifyWithCarolKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.detachedSign()
                .key(TestData.CAROL_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = sop.detachedVerify()
                .cert(TestData.CAROL_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, TestData.CAROL_SIGNING_FINGERPRINT, TestData.CAROL_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signVerifyWithEncryptedKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.detachedSign()
                .key(TestData.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                .withKeyPassword(TestData.PASSWORD)
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayStartsWith(signature, BEGIN_PGP_SIGNATURE_BYTES);

        List<Verification> verificationList = sop.detachedVerify()
                .cert(TestData.PASSWORD_PROTECTED_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signArmorVerifyWithBobKey(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.detachedSign()
                .key(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .noArmor()
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        byte[] armored = sop.armor()
                .data(signature)
                .getBytes();

        List<Verification> verificationList = sop.detachedVerify()
                .cert(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(armored)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, TestData.BOB_SIGNING_FINGERPRINT, TestData.BOB_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void verifyNotAfterThrowsNoSignature(SOP sop) {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signature = TestData.ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date signatureDate = TestData.ALICE_DETACHED_SIGNED_MESSAGE_DATE;
        Date beforeSignature = new Date(signatureDate.getTime() - 1000); // 1 sec before sig

        assertThrows(SOPGPException.NoSignature.class, () -> sop.detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .notAfter(beforeSignature)
                .signatures(signature)
                .data(message));
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void verifyNotBeforeThrowsNoSignature(SOP sop) {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signature = TestData.ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date signatureDate = TestData.ALICE_DETACHED_SIGNED_MESSAGE_DATE;
        Date afterSignature = new Date(signatureDate.getTime() + 1000); // 1 sec after sig

        assertThrows(SOPGPException.NoSignature.class, () -> sop.detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .notBefore(afterSignature)
                .signatures(signature)
                .data(message));
    }


    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signVerifyWithEncryptedKeyWithoutPassphraseFails(SOP sop) {
        assertThrows(SOPGPException.KeyIsProtected.class, () ->
                sop.detachedSign()
                        .key(TestData.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                        .data(TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8))
                        .toByteArrayAndResult()
                        .getBytes());
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signWithProtectedKeyAndMultiplePassphrasesTest(SOP sop)
            throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.sign()
                .key(TestData.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                .withKeyPassword("wrong")
                .withKeyPassword(TestData.PASSWORD) // correct
                .withKeyPassword("wrong2")
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        assertFalse(sop.verify()
                .cert(TestData.PASSWORD_PROTECTED_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message)
                .isEmpty());
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void verifyMissingCertCausesMissingArg(SOP sop) {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        assertThrows(SOPGPException.MissingArg.class, () ->
                sop.verify()
                        .signatures(TestData.ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8))
                        .data(message));
    }

}
