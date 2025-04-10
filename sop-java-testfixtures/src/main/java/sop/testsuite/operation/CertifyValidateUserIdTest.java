// SPDX-FileCopyrightText: 2025 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;
import sop.exception.SOPGPException;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("sop.testsuite.operation.AbstractSOPTest#hasBackends")
public class CertifyValidateUserIdTest {

    static Stream<Arguments> provideInstances() {
        return AbstractSOPTest.provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void certifyUserId(SOP sop) throws IOException {
        byte[] aliceKey = sop.generateKey()
                .withKeyPassword("sw0rdf1sh")
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] aliceCert = sop.extractCert()
                .key(aliceKey)
                .getBytes();

        byte[] bobKey = sop.generateKey()
                .userId("Bob <bob@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] bobCert = sop.extractCert()
                .key(bobKey)
                .getBytes();

        // Alice has her own user-id self-certified
        assertTrue(sop.validateUserId()
                .authorities(aliceCert)
                .userId("Alice <alice@pgpainless.org>")
                .subjects(aliceCert));

        // Alice has not yet certified Bobs user-id
        assertFalse(sop.validateUserId()
                .authorities(aliceCert)
                .userId("Bob <bob@pgpainless.org>")
                .subjects(bobCert));

        byte[] bobCertifiedByAlice = sop.certifyUserId()
                .userId("Bob <bob@pgpainless.org>")
                .withKeyPassword("sw0rdf1sh")
                .keys(aliceKey)
                .certs(bobCert)
                .getBytes();

        assertTrue(sop.validateUserId()
                .userId("Bob <bob@pgpainless.org>")
                .authorities(aliceCert)
                .subjects(bobCertifiedByAlice));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void addPetName(SOP sop) throws IOException {
        byte[] aliceKey = sop.generateKey()
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] aliceCert = sop.extractCert()
                .key(aliceKey)
                .getBytes();

        byte[] bobKey = sop.generateKey()
                .userId("Bob <bob@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] bobCert = sop.extractCert()
                .key(bobKey)
                .getBytes();

        assertThrows(SOPGPException.CertUserIdNoMatch.class, () ->
                sop.certifyUserId()
                        .userId("Bobby")
                        .keys(aliceKey)
                        .certs(bobCert)
                        .getBytes());

        byte[] bobWithPetName = sop.certifyUserId()
                .userId("Bobby")
                .noRequireSelfSig()
                .keys(aliceKey)
                .certs(bobCert)
                .getBytes();

        assertTrue(sop.validateUserId()
                .userId("Bobby")
                .authorities(aliceCert)
                .subjects(bobWithPetName));
    }
}
