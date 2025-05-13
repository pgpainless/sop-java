// SPDX-FileCopyrightText: 2025 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;
import sop.util.HexUtil;

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
        byte[] key = sop.generateKey()
                .noArmor()
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] cert = sop.extractCert()
                .noArmor()
                .key(key)
                .getBytes();

        byte[] merged = sop.mergeCerts()
                .noArmor()
                .updates(cert)
                .baseCertificates(cert)
                .getBytes();

        System.out.println("cert");
        System.out.println(new String(sop.armor().data(cert).getBytes()));
        System.out.println("merged");
        System.out.println(new String(sop.armor().data(merged).getBytes()));
        assertArrayEquals(cert, merged);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void testApplyBaseToUpdate(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .noArmor()
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] cert = sop.extractCert()
                .noArmor()
                .key(key)
                .getBytes();

        byte[] update = sop.revokeKey()
                .noArmor()
                .keys(key)
                .getBytes();

        byte[] merged = sop.mergeCerts()
                .noArmor()
                .updates(cert)
                .baseCertificates(update)
                .getBytes();

        assertArrayEquals(update, merged);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void testApplyUpdateToBase(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .noArmor()
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();

        byte[] cert = sop.extractCert()
                .noArmor()
                .key(key)
                .getBytes();

        byte[] update = sop.revokeKey()
                .noArmor()
                .keys(key)
                .getBytes();

        byte[] merged = sop.mergeCerts()
                .noArmor()
                .updates(update)
                .baseCertificates(cert)
                .getBytes();

        assertArrayEquals(update, merged);
    }
}
