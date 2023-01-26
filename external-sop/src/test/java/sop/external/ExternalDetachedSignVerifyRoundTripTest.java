// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.condition.EnabledIf;
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
import static sop.testing.JUtils.assertArrayStartsWith;
import static sop.testing.JUtils.assertSignedBy;
import static sop.testing.TestData.ALICE_CERT;
import static sop.testing.TestData.ALICE_DETACHED_SIGNED_MESSAGE;
import static sop.testing.TestData.ALICE_DETACHED_SIGNED_MESSAGE_DATE;
import static sop.testing.TestData.ALICE_KEY;
import static sop.testing.TestData.ALICE_PRIMARY_FINGERPRINT;
import static sop.testing.TestData.ALICE_SIGNING_FINGERPRINT;
import static sop.testing.TestData.BEGIN_PGP_SIGNATURE;
import static sop.testing.TestData.BOB_CERT;
import static sop.testing.TestData.BOB_KEY;
import static sop.testing.TestData.BOB_PRIMARY_FINGERPRINT;
import static sop.testing.TestData.BOB_SIGNING_FINGERPRINT;
import static sop.testing.TestData.CAROL_CERT;
import static sop.testing.TestData.CAROL_KEY;
import static sop.testing.TestData.CAROL_PRIMARY_FINGERPRINT;
import static sop.testing.TestData.CAROL_SIGNING_FINGERPRINT;
import static sop.testing.TestData.PASSWORD;
import static sop.testing.TestData.PASSWORD_PROTECTED_CERT;
import static sop.testing.TestData.PASSWORD_PROTECTED_KEY;
import static sop.testing.TestData.PLAINTEXT;

@EnabledIf("sop.external.AbstractExternalSOPTest#hasBackends")
public class ExternalDetachedSignVerifyRoundTripTest extends AbstractExternalSOPTest {

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signVerifyWithAliceKey(SOP sop) throws IOException {
        byte[] message = PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.detachedSign()
                .key(ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = sop.detachedVerify()
                .cert(ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, ALICE_SIGNING_FINGERPRINT, ALICE_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signVerifyTextModeWithAliceKey(SOP sop) throws IOException {
        byte[] message = PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.detachedSign()
                .key(ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(SignAs.Text)
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = sop.detachedVerify()
                .cert(ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, ALICE_SIGNING_FINGERPRINT, ALICE_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void verifyKnownMessageWithAliceCert(SOP sop) throws IOException {
        byte[] message = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signature = ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);

        List<Verification> verificationList = sop.detachedVerify()
                .cert(ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, ALICE_SIGNING_FINGERPRINT, ALICE_PRIMARY_FINGERPRINT, ALICE_DETACHED_SIGNED_MESSAGE_DATE);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signVerifyWithBobKey(SOP sop) throws IOException {
        byte[] message = PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.detachedSign()
                .key(BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = sop.detachedVerify()
                .cert(BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, BOB_SIGNING_FINGERPRINT, BOB_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signVerifyWithCarolKey(SOP sop) throws IOException {
        byte[] message = PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.detachedSign()
                .key(CAROL_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = sop.detachedVerify()
                .cert(CAROL_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, CAROL_SIGNING_FINGERPRINT, CAROL_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signVerifyWithEncryptedKey(SOP sop) throws IOException {
        byte[] message = PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.detachedSign()
                .key(PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                .withKeyPassword(PASSWORD)
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayStartsWith(signature, BEGIN_PGP_SIGNATURE);

        List<Verification> verificationList = sop.detachedVerify()
                .cert(PASSWORD_PROTECTED_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signArmorVerifyWithBobKey(SOP sop) throws IOException {
        byte[] message = PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.detachedSign()
                .key(BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .noArmor()
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        byte[] armored = sop.armor()
                .data(signature)
                .getBytes();

        List<Verification> verificationList = sop.detachedVerify()
                .cert(BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(armored)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, BOB_SIGNING_FINGERPRINT, BOB_PRIMARY_FINGERPRINT);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void verifyNotAfterThrowsNoSignature(SOP sop) {
        byte[] message = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signature = ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date beforeSignature = new Date(ALICE_DETACHED_SIGNED_MESSAGE_DATE.getTime() - 1000); // 1 sec before sig

        assertThrows(SOPGPException.NoSignature.class, () -> sop.detachedVerify()
                .cert(ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .notAfter(beforeSignature)
                .signatures(signature)
                .data(message));
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void verifyNotBeforeThrowsNoSignature(SOP sop) {
        byte[] message = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signature = ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date afterSignature = new Date(ALICE_DETACHED_SIGNED_MESSAGE_DATE.getTime() + 1000); // 1 sec after sig

        assertThrows(SOPGPException.NoSignature.class, () -> sop.detachedVerify()
                .cert(ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .notBefore(afterSignature)
                .signatures(signature)
                .data(message));
    }


    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signVerifyWithEncryptedKeyWithoutPassphraseFails(SOP sop) {
        assertThrows(SOPGPException.KeyIsProtected.class, () ->
                sop.detachedSign()
                        .key(PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                        .data(PLAINTEXT.getBytes(StandardCharsets.UTF_8))
                        .toByteArrayAndResult()
                        .getBytes());
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void signWithProtectedKeyAndMultiplePassphrasesTest(SOP sop)
            throws IOException {
        byte[] message = PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = sop.sign()
                .key(PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                .withKeyPassword("wrong")
                .withKeyPassword(PASSWORD) // correct
                .withKeyPassword("wrong2")
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        assertFalse(sop.verify()
                .cert(PASSWORD_PROTECTED_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message)
                .isEmpty());
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void verifyMissingCertCausesMissingArg(SOP sop) {
        byte[] message = PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        assertThrows(SOPGPException.MissingArg.class, () ->
                sop.verify()
                        .signatures(ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8))
                        .data(message));
    }

}
