// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sop.ByteArrayAndResult;
import sop.DecryptionResult;
import sop.SOP;
import sop.Verification;
import sop.enums.EncryptAs;
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

public class ExternalEncryptDecryptRoundTripTest extends AbstractExternalSOPTest {

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void encryptDecryptRoundTripPasswordTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = sop.encrypt()
                .withPassword("sw0rdf1sh")
                .plaintext(message)
                .getBytes();

        byte[] plaintext = sop.decrypt()
                .withPassword("sw0rdf1sh")
                .ciphertext(ciphertext)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayEquals(message, plaintext);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void encryptDecryptRoundTripAliceTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = sop.encrypt()
                .withCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .plaintext(message)
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = sop.decrypt()
                .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult();

        byte[] plaintext = bytesAndResult.getBytes();
        assertArrayEquals(message, plaintext);

        DecryptionResult result = bytesAndResult.getResult();
        assertNotNull(result.getSessionKey().get());
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void encryptDecryptRoundTripBobTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = sop.encrypt()
                .withCert(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .plaintext(message)
                .getBytes();

        byte[] plaintext = sop.decrypt()
                .withKey(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayEquals(message, plaintext);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void encryptDecryptRoundTripCarolTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = sop.encrypt()
                .withCert(TestData.CAROL_CERT.getBytes(StandardCharsets.UTF_8))
                .plaintext(message)
                .getBytes();

        byte[] plaintext = sop.decrypt()
                .withKey(TestData.CAROL_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayEquals(message, plaintext);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void encryptNoArmorThenArmorThenDecryptRoundTrip(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = sop.encrypt()
                .withCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .noArmor()
                .plaintext(message)
                .getBytes();

        byte[] armored = sop.armor()
                .data(ciphertext)
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = sop.decrypt()
                .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(armored)
                .toByteArrayAndResult();

        byte[] plaintext = bytesAndResult.getBytes();
        assertArrayEquals(message, plaintext);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void encryptSignDecryptVerifyRoundTripAliceTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = sop.encrypt()
                .withCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signWith(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .plaintext(message)
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = sop.decrypt()
                .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .verifyWithCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
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

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void encryptSignAsTextDecryptVerifyRoundTripAliceTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = sop.encrypt()
                .withCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signWith(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(EncryptAs.Text)
                .plaintext(message)
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = sop.decrypt()
                .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .verifyWithCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
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

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void encryptSignDecryptVerifyRoundTripWithFreshEncryptedKeyTest(SOP sop) throws IOException {
        byte[] keyPassword = "sw0rdf1sh".getBytes(StandardCharsets.UTF_8);
        byte[] key = sop.generateKey()
                .withKeyPassword(keyPassword)
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getBytes();
        byte[] cert = sop.extractCert()
                .key(key)
                .getBytes();

        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = sop.encrypt()
                .withCert(cert)
                .signWith(key)
                .withKeyPassword(keyPassword)
                .plaintext(message)
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = sop.decrypt()
                .withKey(key)
                .withKeyPassword(keyPassword)
                .verifyWithCert(cert)
                .ciphertext(ciphertext)
                .toByteArrayAndResult();

        assertFalse(bytesAndResult.getResult().getVerifications().isEmpty());
        assertArrayEquals(message, bytesAndResult.getBytes());
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void decryptVerifyNotAfterTest(SOP sop) {
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
            ByteArrayAndResult<DecryptionResult> bytesAndResult = sop.decrypt()
                    .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                    .verifyWithCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                    .verifyNotAfter(beforeSignature)
                    .ciphertext(message)
                    .toByteArrayAndResult();

            if (bytesAndResult.getResult().getVerifications().isEmpty()) {
                throw new SOPGPException.NoSignature("No verifiable signature found.");
            }
        });
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void decryptVerifyNotBeforeTest(SOP sop) {
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
            ByteArrayAndResult<DecryptionResult> bytesAndResult = sop.decrypt()
                    .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                    .verifyWithCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                    .verifyNotBefore(afterSignature)
                    .ciphertext(message)
                    .toByteArrayAndResult();

            if (bytesAndResult.getResult().getVerifications().isEmpty()) {
                throw new SOPGPException.NoSignature("No verifiable signature found.");
            }
        });
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void missingArgsTest(SOP sop) {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        assertThrows(SOPGPException.MissingArg.class, () -> sop.encrypt()
                .plaintext(message)
                .getBytes());
    }
}
