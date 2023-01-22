// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sop.ByteArrayAndResult;
import sop.DecryptionResult;
import sop.SOP;
import sop.SessionKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExternalDecryptWithSessionKeyTest extends AbstractExternalSOPTest {

    private static final String CIPHERTEXT = "-----BEGIN PGP MESSAGE-----\n" +
            "\n" +
            "wV4DR2b2udXyHrYSAQdAy+Et2hCh4ubh8KsmM8ctRDN6Pee+UHVVcI6YXpY9S2cw\n" +
            "1QEROCgfm6xGb+hgxmoFrWhtZU03Arb27ZmpWA6e6Ha9jFdB4/DDbqbhlVuFOmti\n" +
            "0j8BqGjEvEYAon+8F9TwMaDbPjjy9SdgQBorlM88ChIW14KQtpG9FZN+r+xVKPG1\n" +
            "8EIOxI4qOZaH3Wejraca31M=\n" +
            "=1imC\n" +
            "-----END PGP MESSAGE-----\n";
    private static final String SESSION_KEY = "9:ED682800F5FEA829A82E8B7DDF8CE9CF4BF9BB45024B017764462EE53101C36A";

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void testDecryptAndExtractSessionKey(SOP sop) throws IOException {
        ByteArrayAndResult<DecryptionResult> bytesAndResult = sop.decrypt()
                .withKey(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .ciphertext(CIPHERTEXT.getBytes(StandardCharsets.UTF_8))
                .toByteArrayAndResult();

        assertEquals(SESSION_KEY, bytesAndResult.getResult().getSessionKey().get().toString());

        assertArrayEquals("Hello, World!\n".getBytes(StandardCharsets.UTF_8), bytesAndResult.getBytes());
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void testDecryptWithSessionKey(SOP sop) throws IOException {
        byte[] decrypted = sop.decrypt()
                .withSessionKey(SessionKey.fromString(SESSION_KEY))
                .ciphertext(CIPHERTEXT.getBytes(StandardCharsets.UTF_8))
                .toByteArrayAndResult()
                .getBytes();

        assertArrayEquals("Hello, World!\n".getBytes(StandardCharsets.UTF_8), decrypted);
    }
}
