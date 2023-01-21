// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
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

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalDetachedSignVerifyRoundTripTest extends AbstractExternalSOPTest {

    private static final String BEGIN_PGP_SIGNATURE = "-----BEGIN PGP SIGNATURE-----\n";
    private static final byte[] BEGIN_PGP_SIGNATURE_BYTES = BEGIN_PGP_SIGNATURE.getBytes(StandardCharsets.UTF_8);

    @Test
    public void signVerifyWithAliceKey() throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = getSop().detachedSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = getSop().detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT);
    }

    @Test
    public void signVerifyTextModeWithAliceKey() throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = getSop().detachedSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(SignAs.Text)
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = getSop().detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT);
    }

    @Test
    public void verifyKnownMessageWithAliceCert() throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signature = TestData.ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);

        List<Verification> verificationList = getSop().detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT, TestData.ALICE_DETACHED_SIGNED_MESSAGE_DATE);
    }

    @Test
    public void signVerifyWithBobKey() throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = getSop().detachedSign()
                .key(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = getSop().detachedVerify()
                .cert(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, TestData.BOB_SIGNING_FINGERPRINT, TestData.BOB_PRIMARY_FINGERPRINT);
    }

    @Test
    public void signVerifyWithCarolKey() throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = getSop().detachedSign()
                .key(TestData.CAROL_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = getSop().detachedVerify()
                .cert(TestData.CAROL_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, TestData.CAROL_SIGNING_FINGERPRINT, TestData.CAROL_PRIMARY_FINGERPRINT);
    }

    @Test
    public void signVerifyWithEncryptedKey() throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = getSop().detachedSign()
                .key(TestData.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                .withKeyPassword(TestData.PASSWORD)
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayStartsWith(signature, BEGIN_PGP_SIGNATURE_BYTES);

        List<Verification> verificationList = getSop().detachedVerify()
                .cert(TestData.PASSWORD_PROTECTED_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
    }

    @Test
    public void signArmorVerifyWithBobKey() throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = getSop().detachedSign()
                .key(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .noArmor()
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        byte[] armored = getSop().armor()
                .data(signature)
                .getBytes();

        List<Verification> verificationList = getSop().detachedVerify()
                .cert(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(armored)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertSignedBy(verificationList, TestData.BOB_SIGNING_FINGERPRINT, TestData.BOB_PRIMARY_FINGERPRINT);
    }

    @Test
    public void verifyNotAfterThrowsNoSignature() {
        ignoreIf("sqop", Is.leq, "0.27.2"); // returns 1 instead of 3 (NO_SIGNATURE)

        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signature = TestData.ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date signatureDate = TestData.ALICE_DETACHED_SIGNED_MESSAGE_DATE;
        Date beforeSignature = new Date(signatureDate.getTime() - 1000); // 1 sec before sig

        assertThrows(SOPGPException.NoSignature.class, () -> getSop().detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .notAfter(beforeSignature)
                .signatures(signature)
                .data(message));
    }

    @Test
    public void verifyNotBeforeThrowsNoSignature() {
        ignoreIf("sqop", Is.leq, "0.27.2"); // returns 1 instead of 3 (NO_SIGNATURE)

        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] signature = TestData.ALICE_DETACHED_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);
        Date signatureDate = TestData.ALICE_DETACHED_SIGNED_MESSAGE_DATE;
        Date afterSignature = new Date(signatureDate.getTime() + 1000); // 1 sec after sig

        assertThrows(SOPGPException.NoSignature.class, () -> getSop().detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .notBefore(afterSignature)
                .signatures(signature)
                .data(message));
    }


    @Test
    public void signVerifyWithEncryptedKeyWithoutPassphraseFails() {
        ignoreIf("sqop", Is.leq, "0.27.2"); // does not return exit code 67 for encrypted keys without passphrase

        assertThrows(SOPGPException.KeyIsProtected.class, () ->
                getSop().detachedSign()
                        .key(TestData.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                        .data(TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8))
                        .toByteArrayAndResult()
                        .getBytes());
    }

    @Test
    public void signWithProtectedKeyAndMultiplePassphrasesTest()
            throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] signature = getSop().sign()
                .key(TestData.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                .withKeyPassword("wrong")
                .withKeyPassword(TestData.PASSWORD) // correct
                .withKeyPassword("wrong2")
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        assertFalse(getSop().verify()
                .cert(TestData.PASSWORD_PROTECTED_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message)
                .isEmpty());
    }

}
