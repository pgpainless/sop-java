// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import sop.ByteArrayAndResult;
import sop.DecryptionResult;
import sop.Verification;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
