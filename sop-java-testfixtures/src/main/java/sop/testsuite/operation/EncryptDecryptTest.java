// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.ByteArrayAndResult;
import sop.DecryptionResult;
import sop.EncryptionResult;
import sop.Profile;
import sop.SOP;
import sop.SessionKey;
import sop.Verification;
import sop.enums.EncryptAs;
import sop.enums.SignatureMode;
import sop.exception.SOPGPException;
import sop.operation.Decrypt;
import sop.operation.Encrypt;
import sop.testsuite.TestData;
import sop.testsuite.assertions.VerificationListAssert;
import sop.util.Optional;
import sop.util.UTCUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIf("sop.testsuite.operation.AbstractSOPTest#hasBackends")
public class EncryptDecryptTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void encryptDecryptRoundTripPasswordTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        ByteArrayAndResult<EncryptionResult> encResult = assumeSupported(sop::encrypt)
                .withPassword("sw0rdf1sh")
                .plaintext(message)
                .toByteArrayAndResult();

        byte[] ciphertext = encResult.getBytes();
        Optional<SessionKey> encSessionKey = encResult.getResult().getSessionKey();

        ByteArrayAndResult<DecryptionResult> decResult = assumeSupported(sop::decrypt)
                .withPassword("sw0rdf1sh")
                .ciphertext(ciphertext)
                .toByteArrayAndResult();

        byte[] plaintext = decResult.getBytes();
        Optional<SessionKey> decSessionKey = decResult.getResult().getSessionKey();

        assertArrayEquals(message, plaintext, "Decrypted plaintext does not match original plaintext.");
        if (encSessionKey.isPresent() && decSessionKey.isPresent()) {
            assertEquals(encSessionKey.get(), decSessionKey.get(),
                    "Extracted Session Key mismatch.");
        }
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void encryptDecryptRoundTripAliceTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = assumeSupported(sop::encrypt)
                .withCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .plaintext(message)
                .toByteArrayAndResult()
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = assumeSupported(sop::decrypt)
                .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult();

        byte[] plaintext = bytesAndResult.getBytes();
        assertArrayEquals(message, plaintext, "Decrypted plaintext does not match original plaintext.");

        DecryptionResult result = bytesAndResult.getResult();
        if (result.getSessionKey().isPresent()) {
            assertNotNull(result.getSessionKey().get(), "Session key MUST NOT be null.");
        }
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void encryptDecryptRoundTripBobTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = assumeSupported(sop::encrypt)
                .withCert(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .plaintext(message)
                .toByteArrayAndResult()
                .getBytes();

        byte[] plaintext = assumeSupported(sop::decrypt)
                .withKey(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayEquals(message, plaintext, "Decrypted plaintext does not match original plaintext.");
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void encryptDecryptRoundTripCarolTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = assumeSupported(sop::encrypt)
                .withCert(TestData.CAROL_CERT.getBytes(StandardCharsets.UTF_8))
                .plaintext(message)
                .toByteArrayAndResult()
                .getBytes();

        byte[] plaintext = assumeSupported(sop::decrypt)
                .withKey(TestData.CAROL_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayEquals(message, plaintext, "Decrypted plaintext does not match original plaintext.");
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void encryptNoArmorThenArmorThenDecryptRoundTrip(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = assumeSupported(sop::encrypt)
                .withCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .noArmor()
                .plaintext(message)
                .toByteArrayAndResult()
                .getBytes();

        byte[] armored = assumeSupported(sop::armor)
                .data(ciphertext)
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = assumeSupported(sop::decrypt)
                .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(armored)
                .toByteArrayAndResult();

        byte[] plaintext = bytesAndResult.getBytes();
        assertArrayEquals(message, plaintext, "Decrypted plaintext does not match original plaintext.");
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void encryptSignDecryptVerifyRoundTripAliceTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = assumeSupported(sop::encrypt)
                .withCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signWith(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(EncryptAs.binary)
                .plaintext(message)
                .toByteArrayAndResult()
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = assumeSupported(sop::decrypt)
                .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .verifyWithCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult();

        byte[] plaintext = bytesAndResult.getBytes();
        assertArrayEquals(message, plaintext, "Decrypted plaintext does not match original plaintext.");

        DecryptionResult result = bytesAndResult.getResult();
        if (result.getSessionKey().isPresent()) {
            assertNotNull(result.getSessionKey().get(), "Session key MUST NOT be null.");
        }

        List<Verification> verificationList = result.getVerifications();
        VerificationListAssert.assertThatVerificationList(verificationList)
                .isNotEmpty()
                .hasSingleItem()
                .issuedBy(TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT)
                .hasModeOrNull(SignatureMode.binary);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void encryptSignAsTextDecryptVerifyRoundTripAliceTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = assumeSupported(sop::encrypt)
                .withCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signWith(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(EncryptAs.text)
                .plaintext(message)
                .toByteArrayAndResult()
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = assumeSupported(sop::decrypt)
                .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .verifyWithCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult();

        byte[] plaintext = bytesAndResult.getBytes();
        assertArrayEquals(message, plaintext, "Decrypted plaintext does not match original plaintext.");

        DecryptionResult result = bytesAndResult.getResult();
        assertNotNull(result.getSessionKey().get());

        List<Verification> verificationList = result.getVerifications();
        VerificationListAssert.assertThatVerificationList(verificationList)
                .hasSingleItem()
                .issuedBy(TestData.ALICE_SIGNING_FINGERPRINT, TestData.ALICE_PRIMARY_FINGERPRINT)
                .hasModeOrNull(SignatureMode.text);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void encryptSignDecryptVerifyRoundTripWithFreshEncryptedKeyTest(SOP sop) throws IOException {
        byte[] keyPassword = "sw0rdf1sh".getBytes(StandardCharsets.UTF_8);
        byte[] key = assumeSupported(sop::generateKey)
                .withKeyPassword(keyPassword)
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getBytes();
        byte[] cert = assumeSupported(sop::extractCert)
                .key(key)
                .getBytes();

        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = assumeSupported(sop::encrypt)
                .withCert(cert)
                .signWith(key)
                .withKeyPassword(keyPassword)
                .plaintext(message)
                .toByteArrayAndResult()
                .getBytes();

        ByteArrayAndResult<DecryptionResult> bytesAndResult = assumeSupported(sop::decrypt)
                .withKey(key)
                .withKeyPassword(keyPassword)
                .verifyWithCert(cert)
                .ciphertext(ciphertext)
                .toByteArrayAndResult();

        List<Verification> verifications = bytesAndResult.getResult().getVerifications();
        VerificationListAssert.assertThatVerificationList(verifications)
                .isNotEmpty()
                .hasSingleItem();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void decryptVerifyNotAfterTest(SOP sop) throws ParseException {
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
            ByteArrayAndResult<DecryptionResult> bytesAndResult = assumeSupported(sop::decrypt)
                    .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                    .verifyWithCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                    .verifyNotAfter(beforeSignature)
                    .ciphertext(message)
                    .toByteArrayAndResult();

            // Some implementations do not throw NoSignature and instead return an empty list.
            if (bytesAndResult.getResult().getVerifications().isEmpty()) {
                throw new SOPGPException.NoSignature("No verifiable signature found.");
            }
        });
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void decryptVerifyNotBeforeTest(SOP sop) throws ParseException {
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
            ByteArrayAndResult<DecryptionResult> bytesAndResult = assumeSupported(sop::decrypt)
                    .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                    .verifyWithCert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                    .verifyNotBefore(afterSignature)
                    .ciphertext(message)
                    .toByteArrayAndResult();

            // Some implementations do not throw NoSignature and instead return an empty list.
            if (bytesAndResult.getResult().getVerifications().isEmpty()) {
                throw new SOPGPException.NoSignature("No verifiable signature found.");
            }
        });
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void missingArgsTest(SOP sop) {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        assertThrows(SOPGPException.MissingArg.class, () -> assumeSupported(sop::encrypt)
                .plaintext(message)
                .toByteArrayAndResult()
                .getBytes());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void passingSecretKeysForPublicKeysFails(SOP sop) {
        assertThrows(SOPGPException.BadData.class, () ->
                assumeSupported(sop::encrypt)
                        .withCert(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                        .plaintext(TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8))
                        .toByteArrayAndResult()
                        .getBytes());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void encryptDecryptWithAllSupportedKeyGenerationProfiles(SOP sop) throws IOException {
        List<Profile> profiles = assumeSupported(sop::listProfiles).generateKey();

        List<byte[]> keys = new ArrayList<>();
        List<byte[]> certs = new ArrayList<>();
        for (Profile p : profiles) {
            byte[] k = assumeSupported(sop::generateKey)
                    .profile(p)
                    .userId(p.getName())
                    .generate()
                    .getBytes();
            keys.add(k);

            byte[] c = assumeSupported(sop::extractCert)
                    .key(k)
                    .getBytes();
            certs.add(c);
        }

        byte[] plaintext = "Hello, World!\n".getBytes();

        Encrypt encrypt = assumeSupported(sop::encrypt);
        for (byte[] c : certs) {
            encrypt.withCert(c);
        }
        for (byte[] k : keys) {
            encrypt.signWith(k);
        }

        ByteArrayAndResult<EncryptionResult> encRes = encrypt.plaintext(plaintext)
                .toByteArrayAndResult();
        EncryptionResult eResult = encRes.getResult();
        byte[] ciphertext = encRes.getBytes();

        for (byte[] k : keys) {
            Decrypt decrypt = assumeSupported(sop::decrypt)
                    .withKey(k);
            for (byte[] c : certs) {
                decrypt.verifyWithCert(c);
            }
            ByteArrayAndResult<DecryptionResult> decRes = decrypt.ciphertext(ciphertext)
                    .toByteArrayAndResult();
            DecryptionResult dResult = decRes.getResult();
            byte[] decPlaintext = decRes.getBytes();
            assertArrayEquals(plaintext, decPlaintext, "Decrypted plaintext does not match original plaintext.");
            assertEquals(certs.size(), dResult.getVerifications().size());
        }
    }
}
