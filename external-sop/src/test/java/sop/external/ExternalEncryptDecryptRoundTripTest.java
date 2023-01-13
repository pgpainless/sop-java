// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import sop.ByteArrayAndResult;
import sop.DecryptionResult;
import sop.Verification;
import sop.exception.SOPGPException;
import sop.util.UTCUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalEncryptDecryptRoundTripTest extends AbstractExternalSOPTest {

    @Test
    public void encryptDecryptRoundTripPasswordTest() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = getSop().encrypt()
                .withPassword("sw0rdf1sh")
                .plaintext(message)
                .getBytes();

        byte[] plaintext = getSop().decrypt()
                .withPassword("sw0rdf1sh")
                .ciphertext(ciphertext)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayEquals(message, plaintext);
    }

    @Test
    public void encryptDecryptRoundTripAliceTest() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = getSop().encrypt()
                .withCert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .plaintext(message)
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = getSop().decrypt()
                .withKey(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult();

        byte[] plaintext = bytesAndResult.getBytes();
        assertArrayEquals(message, plaintext);

        DecryptionResult result = bytesAndResult.getResult();
        assertNotNull(result.getSessionKey().get());
    }

    @Test
    public void encryptDecryptRoundTripBobTest() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = getSop().encrypt()
                .withCert(TestKeys.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .plaintext(message)
                .getBytes();

        byte[] plaintext = getSop().decrypt()
                .withKey(TestKeys.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayEquals(message, plaintext);
    }

    @Test
    public void encryptDecryptRoundTripCarolTest() throws IOException {
        ignoreIf("sqop", Is.geq, "0.0.0"); // sqop reports cert not encryption capable

        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = getSop().encrypt()
                .withCert(TestKeys.CAROL_CERT.getBytes(StandardCharsets.UTF_8))
                .plaintext(message)
                .getBytes();

        byte[] plaintext = getSop().decrypt()
                .withKey(TestKeys.CAROL_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayEquals(message, plaintext);
    }

    @Test
    public void encryptSignDecryptVerifyRoundTripAliceTest() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = getSop().encrypt()
                .withCert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signWith(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .plaintext(message)
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = getSop().decrypt()
                .withKey(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .verifyWithCert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult();

        byte[] plaintext = bytesAndResult.getBytes();
        assertArrayEquals(message, plaintext);

        DecryptionResult result = bytesAndResult.getResult();
        assertNotNull(result.getSessionKey().get());
        List<Verification> verificationList = result.getVerifications();
        assertEquals(1, verificationList.size());
        assertTrue(verificationList.get(0).toString().contains("EB85BB5FA33A75E15E944E63F231550C4F47E38E EB85BB5FA33A75E15E944E63F231550C4F47E38E"));
    }

    @Test
    public void encryptSignDecryptVerifyRoundTripWithFreshEncryptedKeyTest() throws IOException {
        byte[] keyPassword = "sw0rdf1sh".getBytes(StandardCharsets.UTF_8);
        byte[] key = getSop().generateKey()
                .withKeyPassword(keyPassword)
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getBytes();
        byte[] cert = getSop().extractCert()
                .key(key)
                .getBytes();

        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = getSop().encrypt()
                .withCert(cert)
                .signWith(key)
                .withKeyPassword(keyPassword)
                .plaintext(message)
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = getSop().decrypt()
                .withKey(key)
                .withKeyPassword(keyPassword)
                .verifyWithCert(cert)
                .ciphertext(ciphertext)
                .toByteArrayAndResult();

        assertFalse(bytesAndResult.getResult().getVerifications().isEmpty());
        assertArrayEquals(message, bytesAndResult.getBytes());
    }

    @Test
    public void decryptVerifyNotAfterTest() {
        ignoreIf("PGPainless-SOP", Is.le, "1.4.2"); // does not recognize --verify-not-after
        ignoreIf("sqop", Is.leq, "0.27.2"); // does not throw NoSignature

        byte[] message = ("-----BEGIN PGP MESSAGE-----\n" +
                "\n" +
                "wV4DR2b2udXyHrYSAQdAwlOwwyxFDJta5+H9abgSj8jum9v7etUc9usdrElESmow\n" +
                "2Hka48AFVfOezYh0OFn9R8+DMcpuE+e4nw3XnnX5nKs/j3AC2IW6zRHUkRcF3ZCq\n" +
                "0sBNAfjnTYCMjuBmqdcCLzaZT4Hadnpg6neP1UecT/jP14maGfv8nwt0IDGR0Bik\n" +
                "0WC/UJLpWyJ/6TgRrA5hNfANVnfiFBzIiThiVBRWPT2StHr2cOAvFxQK4Uk07rK9\n" +
                "9aTUak8FpML+QA83U8I3qOk4QbzGVBP+IDJ+AKmvDz+0V+9kUhKp+8vyXsBmo9c3\n" +
                "SAXjhFSiPQkU7ORsc6gQHL9+KPOU+W2poPK87H3cmaGiusnXMeLXLIUbkBUJTswd\n" +
                "JNrA2yAkTTFP9QabsdcdTGoeYamq1c29kHF3GOTTcEqXw4WWXngcF7Kbcf435kkL\n" +
                "4iSJnCaxTPftKUxmiGqMqLef7ICVnq/lz3HrH1VD54s=\n" +
                "=Ebi3\n" +
                "-----END PGP MESSAGE-----").getBytes(StandardCharsets.UTF_8);
        Date signatureDate = UTCUtil.parseUTCDate("2023-01-13T16:09:32Z");

        Date beforeSignature = new Date(signatureDate.getTime() - 1000); // 1 sec before signing date

        assertThrows(SOPGPException.NoSignature.class, () -> {
            ByteArrayAndResult<DecryptionResult> bytesAndResult = getSop().decrypt()
                    .withKey(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                    .verifyWithCert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                    .verifyNotAfter(beforeSignature)
                    .ciphertext(message)
                    .toByteArrayAndResult();

            if (bytesAndResult.getResult().getVerifications().isEmpty()) {
                throw new SOPGPException.NoSignature("No verifiable signature found.");
            }
        });
    }

    @Test
    public void decryptVerifyNotBeforeTest() {
        ignoreIf("PGPainless-SOP", Is.le, "1.4.2"); // does not recognize --verify-not-after
        ignoreIf("sqop", Is.leq, "0.27.2"); // does not throw NoSignature

        byte[] message = ("-----BEGIN PGP MESSAGE-----\n" +
                "\n" +
                "wV4DR2b2udXyHrYSAQdAwlOwwyxFDJta5+H9abgSj8jum9v7etUc9usdrElESmow\n" +
                "2Hka48AFVfOezYh0OFn9R8+DMcpuE+e4nw3XnnX5nKs/j3AC2IW6zRHUkRcF3ZCq\n" +
                "0sBNAfjnTYCMjuBmqdcCLzaZT4Hadnpg6neP1UecT/jP14maGfv8nwt0IDGR0Bik\n" +
                "0WC/UJLpWyJ/6TgRrA5hNfANVnfiFBzIiThiVBRWPT2StHr2cOAvFxQK4Uk07rK9\n" +
                "9aTUak8FpML+QA83U8I3qOk4QbzGVBP+IDJ+AKmvDz+0V+9kUhKp+8vyXsBmo9c3\n" +
                "SAXjhFSiPQkU7ORsc6gQHL9+KPOU+W2poPK87H3cmaGiusnXMeLXLIUbkBUJTswd\n" +
                "JNrA2yAkTTFP9QabsdcdTGoeYamq1c29kHF3GOTTcEqXw4WWXngcF7Kbcf435kkL\n" +
                "4iSJnCaxTPftKUxmiGqMqLef7ICVnq/lz3HrH1VD54s=\n" +
                "=Ebi3\n" +
                "-----END PGP MESSAGE-----").getBytes(StandardCharsets.UTF_8);
        Date signatureDate = UTCUtil.parseUTCDate("2023-01-13T16:09:32Z");

        Date afterSignature = new Date(signatureDate.getTime() + 1000); // 1 sec after signing date

        assertThrows(SOPGPException.NoSignature.class, () -> {
            ByteArrayAndResult<DecryptionResult> bytesAndResult = getSop().decrypt()
                    .withKey(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                    .verifyWithCert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                    .verifyNotBefore(afterSignature)
                    .ciphertext(message)
                    .toByteArrayAndResult();

            if (bytesAndResult.getResult().getVerifications().isEmpty()) {
                throw new SOPGPException.NoSignature("No verifiable signature found.");
            }
        });
    }
}
