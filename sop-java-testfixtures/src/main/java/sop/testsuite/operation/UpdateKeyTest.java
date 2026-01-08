// SPDX-FileCopyrightText: 2026 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;
import sop.exception.SOPGPException;
import sop.testsuite.TestData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UpdateKeyTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void updateFreshKeyDoesNothing(SOP sop) throws IOException {
        byte[] freshKey = sop.generateKey()
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] updateKey = sop.updateKey()
                .key(freshKey)
                .getBytes();

        assertArrayEquals(freshKey, updateKey, "Armored representation of fresh and updated fresh key does not match.");
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void updateFreshSignOnlyKeyWithoutSignOnlyFlagAddsEncryptionSubkey(SOP sop) throws IOException {
        byte[] signOnlyKey = sop.generateKey()
                .userId("Alice <alice@pgpainless.org>")
                .signingOnly()
                .generate()
                .getBytes();
        byte[] signOnlyCert = sop.extractCert().key(signOnlyKey).getBytes();

        // Verify that we cannot use the sign-only cert for encryption
        assertThrows(SOPGPException.CertCannotEncrypt.class, () -> sop.encrypt()
                .withCert(signOnlyCert)
                .plaintext(TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8))
                .toByteArrayAndResult()
                .getBytes());

        // Update the key without passing in the --signing-only flag, expecting an encryption subkey to be added
        byte[] updatedKey = sop.updateKey()
                .key(signOnlyKey)
                .getBytes();

        byte[] updatedCert = sop.extractCert()
                .key(updatedKey)
                .getBytes();

        byte[] ciphertext = sop.encrypt()
                .withCert(updatedCert)
                .plaintext(TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8))
                .toByteArrayAndResult()
                .getBytes();

        assertArrayEquals(
                TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8),
                sop.decrypt()
                        .withKey(updatedKey)
                        .ciphertext(ciphertext)
                        .toByteArrayAndResult().getBytes());
    }
}
