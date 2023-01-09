// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class EncryptDecryptRoundTripTest extends AbstractExternalSOPTest {

    @Test
    public void encryptDecryptRoundTripAliceTest() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = getSop().encrypt()
                .withCert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .plaintext(message)
                .getBytes();

        byte[] plaintext = getSop().decrypt()
                .withKey(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(ciphertext)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayEquals(message, plaintext);
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
}
