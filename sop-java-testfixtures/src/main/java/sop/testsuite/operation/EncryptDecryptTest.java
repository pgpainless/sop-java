// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.Disabled;
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
import sop.enums.EncryptFor;
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
    @Disabled("Carol is a deprecated ElGamal key")
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

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void encryptForPurpose(SOP sop) throws IOException {
        String CERT = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "Comment: 88BF 5516 C226 5B7D 1817  03E6 1FF0 DE1E AF8B 379F\n" +
                "\n" +
                "mCYEaVxG2BvmBuO3v5cDQQCuGnAIuaeP0frpw7mutcMQwPkGuuAKUMKSBB8bCgA+\n" +
                "FqEEiL9VFsImW30YFwPmH/DeHq+LN58FgmlcRtgCngkFlQoJCAsFlgIDAQAEiwkI\n" +
                "BwknCQEJAgkDCAECmwEACgkQH/DeHq+LN5/NVHbqH098dr34p9KVQQNLXr8CITqP\n" +
                "vLTkijVXyfZg6Lz1krs3EgEvc8nz3evyYj5xJI+Hg1kHb+ctB5myyTyEtge4JgRp\n" +
                "XEbYG52SLEi5Biq9vn1pFgrozM2QuCqkwXtOr/0ASs0b3t20wsAnBBgbCgCTFqEE\n" +
                "iL9VFsImW30YFwPmH/DeHq+LN58FgmlcRtgCmwJyoAQZGwoAHRahBGp6EAtdr26T\n" +
                "x4sGLa+TQ+g71BlpBYJpXEbYAAoJEK+TQ+g71BlpkJ4VPAQeTXN88wXzLloW2WYP\n" +
                "5w3w7Js4csGE5OynUupCNwUBcIfC+FBMuUdgqjczw4xKRLbZMgp5YLr8Ve3pG48L\n" +
                "AAoJEB/w3h6vizefXrbECKbGBPh+c3+fFG3Au0gzkRMCsZsMaQaRWlQ1E2P/VWlo\n" +
                "xy4JF5nCA6bSC+sFl+DTbwpgvdQlIILR9O386EcHuCYEaVxG2Blrm96fHzaN1JmO\n" +
                "uhU0OMbiDMBYKOL3Iup+TQWzx897CMJ0BBgbCgAgFqEEiL9VFsImW30YFwPmH/De\n" +
                "Hq+LN58FgmlcRtgCmwQACgkQH/DeHq+LN5/wOkjl+MJktOsh+COv4tAhSu2kR0iw\n" +
                "rdY4IAEp7jlnZfx0BVMnVURSrZSge3Zw2vbQQe864GA3Y4le4CWFKm2QAwG4JgRp\n" +
                "XEbYGUzlbIju0H0KDcLmLXsXp7CCLmkcnSjNAj9WTRW7GCJownQEGBsKACAWoQSI\n" +
                "v1UWwiZbfRgXA+Yf8N4er4s3nwWCaVxG2AKbCAAKCRAf8N4er4s3n4+EpHlXYNzD\n" +
                "I2OT9NpobaalDbmDMuvIu/81Uoxv+pJLkrMV+WW5be27HrH6w7YTH1TngILr4V2e\n" +
                "jSB2HhjClk4YBw==\n" +
                "=3S3M\n" +
                "-----END PGP PUBLIC KEY BLOCK-----";
        String KEY_ONLY_STORAGE = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
                "Comment: 88BF 5516 C226 5B7D 1817  03E6 1FF0 DE1E AF8B 379F\n" +
                "\n" +
                "lEkEaVxG2BvmBuO3v5cDQQCuGnAIuaeP0frpw7mutcMQwPkGuuAKUAA0eJO2aUrG\n" +
                "NwfD+W5mn/EHzossHrQPa0jPzERJ1m7kkA/MwpIEHxsKAD4WoQSIv1UWwiZbfRgX\n" +
                "A+Yf8N4er4s3nwWCaVxG2AKeCQWVCgkICwWWAgMBAASLCQgHCScJAQkCCQMIAQKb\n" +
                "AQAKCRAf8N4er4s3n81UduofT3x2vfin0pVBA0tevwIhOo+8tOSKNVfJ9mDovPWS\n" +
                "uzcSAS9zyfPd6/JiPnEkj4eDWQdv5y0HmbLJPIS2B5xJBGlcRtgbnZIsSLkGKr2+\n" +
                "fWkWCujMzZC4KqTBe06v/QBKzRve3bQA80/esWlwPguauRAay+kli/gw/SRhTAK7\n" +
                "n1k63W1vkxkPHsLAJwQYGwoAkxahBIi/VRbCJlt9GBcD5h/w3h6vizefBYJpXEbY\n" +
                "ApsCcqAEGRsKAB0WoQRqehALXa9uk8eLBi2vk0PoO9QZaQWCaVxG2AAKCRCvk0Po\n" +
                "O9QZaZCeFTwEHk1zfPMF8y5aFtlmD+cN8OybOHLBhOTsp1LqQjcFAXCHwvhQTLlH\n" +
                "YKo3M8OMSkS22TIKeWC6/FXt6RuPCwAKCRAf8N4er4s3n162xAimxgT4fnN/nxRt\n" +
                "wLtIM5ETArGbDGkGkVpUNRNj/1VpaMcuCReZwgOm0gvrBZfg028KYL3UJSCC0fTt\n" +
                "/OhHB5xJBGlcRtgZTOVsiO7QfQoNwuYtexensIIuaRydKM0CP1ZNFbsYImgAUDFh\n" +
                "CkwFwBaiasW9oHJQd1TALWOtj0TjQo9tEp1zxmQOC8J0BBgbCgAgFqEEiL9VFsIm\n" +
                "W30YFwPmH/DeHq+LN58FgmlcRtgCmwgACgkQH/DeHq+LN5+PhKR5V2DcwyNjk/Ta\n" +
                "aG2mpQ25gzLryLv/NVKMb/qSS5KzFflluW3tux6x+sO2Ex9U54CC6+Fdno0gdh4Y\n" +
                "wpZOGAc=\n" +
                "=oK8k\n" +
                "-----END PGP PRIVATE KEY BLOCK-----";
        String KEY_ONLY_COMMS = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
                "Comment: 88BF 5516 C226 5B7D 1817  03E6 1FF0 DE1E AF8B 379F\n" +
                "\n" +
                "lEkEaVxG2BvmBuO3v5cDQQCuGnAIuaeP0frpw7mutcMQwPkGuuAKUAA0eJO2aUrG\n" +
                "NwfD+W5mn/EHzossHrQPa0jPzERJ1m7kkA/MwpIEHxsKAD4WoQSIv1UWwiZbfRgX\n" +
                "A+Yf8N4er4s3nwWCaVxG2AKeCQWVCgkICwWWAgMBAASLCQgHCScJAQkCCQMIAQKb\n" +
                "AQAKCRAf8N4er4s3n81UduofT3x2vfin0pVBA0tevwIhOo+8tOSKNVfJ9mDovPWS\n" +
                "uzcSAS9zyfPd6/JiPnEkj4eDWQdv5y0HmbLJPIS2B5xJBGlcRtgbnZIsSLkGKr2+\n" +
                "fWkWCujMzZC4KqTBe06v/QBKzRve3bQA80/esWlwPguauRAay+kli/gw/SRhTAK7\n" +
                "n1k63W1vkxkPHsLAJwQYGwoAkxahBIi/VRbCJlt9GBcD5h/w3h6vizefBYJpXEbY\n" +
                "ApsCcqAEGRsKAB0WoQRqehALXa9uk8eLBi2vk0PoO9QZaQWCaVxG2AAKCRCvk0Po\n" +
                "O9QZaZCeFTwEHk1zfPMF8y5aFtlmD+cN8OybOHLBhOTsp1LqQjcFAXCHwvhQTLlH\n" +
                "YKo3M8OMSkS22TIKeWC6/FXt6RuPCwAKCRAf8N4er4s3n162xAimxgT4fnN/nxRt\n" +
                "wLtIM5ETArGbDGkGkVpUNRNj/1VpaMcuCReZwgOm0gvrBZfg028KYL3UJSCC0fTt\n" +
                "/OhHB5xJBGlcRtgZa5venx82jdSZjroVNDjG4gzAWCji9yLqfk0Fs8fPewgAILUp\n" +
                "R5Ihy+ooSi/6ZnicHsld84qTXLpmeKZMVYhqDm4Ov8J0BBgbCgAgFqEEiL9VFsIm\n" +
                "W30YFwPmH/DeHq+LN58FgmlcRtgCmwQACgkQH/DeHq+LN5/wOkjl+MJktOsh+COv\n" +
                "4tAhSu2kR0iwrdY4IAEp7jlnZfx0BVMnVURSrZSge3Zw2vbQQe864GA3Y4le4CWF\n" +
                "Km2QAwE=\n" +
                "=6hyI\n" +
                "-----END PGP PRIVATE KEY BLOCK-----";

        // Encrypt message only for the storage encryption subkey
        byte[] forStorage = sop.encrypt()
                .encryptFor(EncryptFor.storage)
                .withCert(CERT.getBytes(StandardCharsets.UTF_8))
                .plaintext(TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8))
                .toByteArrayAndResult()
                .getBytes();

        // Storage enc key can decrypt
        assertArrayEquals(
                TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8),
                sop.decrypt()
                        .withKey(KEY_ONLY_STORAGE.getBytes(StandardCharsets.UTF_8))
                        .ciphertext(forStorage)
                        .toByteArrayAndResult()
                        .getBytes());
        // Comms only subkey cannot decrypt
        assertThrows(SOPGPException.CannotDecrypt.class, () -> sop.decrypt()
                .withKey(KEY_ONLY_COMMS.getBytes(StandardCharsets.UTF_8))
                .ciphertext(forStorage)
                .toByteArrayAndResult()
                .getBytes());

        // Encrypt message only for the comms encryption subkey
        byte[] forComms = sop.encrypt()
                .encryptFor(EncryptFor.communications)
                .withCert(CERT.getBytes(StandardCharsets.UTF_8))
                .plaintext(TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8))
                .toByteArrayAndResult()
                .getBytes();

        // Comms enc key can decrypt
        assertArrayEquals(
                TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8),
                sop.decrypt()
                        .withKey(KEY_ONLY_COMMS.getBytes(StandardCharsets.UTF_8))
                        .ciphertext(forComms)
                        .toByteArrayAndResult()
                        .getBytes());
        // Storage only subkey cannot decrypt
        assertThrows(SOPGPException.CannotDecrypt.class, () -> sop.decrypt()
                .withKey(KEY_ONLY_STORAGE.getBytes(StandardCharsets.UTF_8))
                .ciphertext(forComms)
                .toByteArrayAndResult()
                .getBytes());
    }
}
