// SPDX-FileCopyrightText: 2025 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import kotlin.collections.ArraysKt;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class MergeCertsTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void testMergeWithItself(SOP sop) throws IOException {
        byte[] key = assumeSupported(sop::generateKey)
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] cert = assumeSupported(sop::extractCert)
                .key(key)
                .getBytes();

        byte[] merged = assumeSupported(sop::mergeCerts)
                .updates(cert)
                .baseCertificates(cert)
                .getBytes();

        assertArrayEquals(cert, merged);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void testMergeWithItselfArmored(SOP sop) throws IOException {
        byte[] key = assumeSupported(sop::generateKey)
                .noArmor()
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] cert = assumeSupported(sop::extractCert)
                .key(key)
                .getBytes();

        byte[] merged = assumeSupported(sop::mergeCerts)
                .updates(cert)
                .baseCertificates(cert)
                .getBytes();

        assertArrayEquals(cert, merged);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void testMergeWithItselfViaBase(SOP sop) throws IOException {
        byte[] key = assumeSupported(sop::generateKey)
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] cert = assumeSupported(sop::extractCert)
                .key(key)
                .getBytes();

        byte[] certs = ArraysKt.plus(cert, cert);

        byte[] merged = assumeSupported(sop::mergeCerts)
                .updates(cert)
                .baseCertificates(certs)
                .getBytes();

        assertArrayEquals(cert, merged);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void testApplyBaseToUpdate(SOP sop) throws IOException {
        byte[] key = assumeSupported(sop::generateKey)
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] cert = assumeSupported(sop::extractCert)
                .key(key)
                .getBytes();

        byte[] update = assumeSupported(sop::revokeKey)
                .keys(key)
                .getBytes();

        byte[] merged = assumeSupported(sop::mergeCerts)
                .updates(cert)
                .baseCertificates(update)
                .getBytes();

        assertArrayEquals(update, merged);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void testApplyUpdateToBase(SOP sop) throws IOException {
        byte[] key = assumeSupported(sop::generateKey)
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] cert = assumeSupported(sop::extractCert)
                .key(key)
                .getBytes();

        byte[] update = assumeSupported(sop::revokeKey)
                .keys(key)
                .getBytes();

        byte[] merged = assumeSupported(sop::mergeCerts)
                .updates(update)
                .baseCertificates(cert)
                .getBytes();

        assertArrayEquals(update, merged);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void testApplyUpdateToMissingBaseDoesNothing(SOP sop) throws IOException {
        byte[] aliceKey = assumeSupported(sop::generateKey)
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] aliceCert = assumeSupported(sop::extractCert)
                .key(aliceKey)
                .getBytes();

        byte[] bobKey = assumeSupported(sop::generateKey)
                .userId("Bob <bob@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] bobCert = assumeSupported(sop::extractCert)
                .key(bobKey)
                .getBytes();

        byte[] merged = assumeSupported(sop::mergeCerts)
                .updates(bobCert)
                .baseCertificates(aliceCert)
                .getBytes();

        assertArrayEquals(aliceCert, merged);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void testApplyUpdateToMissingBaseDoesNothingNoArmor(SOP sop) throws IOException {
        byte[] aliceKey = assumeSupported(sop::generateKey)
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] aliceCert = assumeSupported(sop::extractCert)
                .noArmor()
                .key(aliceKey)
                .getBytes();

        byte[] bobKey = assumeSupported(sop::generateKey)
                .userId("Bob <bob@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] bobCert = assumeSupported(sop::extractCert)
                .key(bobKey)
                .getBytes();

        byte[] merged = assumeSupported(sop::mergeCerts)
                .noArmor()
                .updates(bobCert)
                .baseCertificates(aliceCert)
                .getBytes();

        assertArrayEquals(aliceCert, merged);
    }

}
