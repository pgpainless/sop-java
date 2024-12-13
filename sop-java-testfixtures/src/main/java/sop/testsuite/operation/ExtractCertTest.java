// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;
import sop.testsuite.JUtils;
import sop.testsuite.TestData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@EnabledIf("sop.testsuite.operation.AbstractSOPTest#hasBackends")
public class ExtractCertTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void extractArmoredCertFromArmoredKeyTest(SOP sop) throws IOException {
        InputStream keyIn = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getInputStream();

        byte[] cert = sop.extractCert().key(keyIn).getBytes();
        JUtils.assertArrayStartsWith(cert, TestData.BEGIN_PGP_PUBLIC_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(cert, TestData.END_PGP_PUBLIC_KEY_BLOCK);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void extractAliceCertFromAliceKeyTest(SOP sop) throws IOException {
        byte[] armoredCert = sop.extractCert()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .getBytes();
        JUtils.assertAsciiArmorEquals(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8), armoredCert);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void extractBobsCertFromBobsKeyTest(SOP sop) throws IOException {
        byte[] armoredCert = sop.extractCert()
                .key(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .getBytes();
        JUtils.assertAsciiArmorEquals(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8), armoredCert);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void extractCarolsCertFromCarolsKeyTest(SOP sop) throws IOException {
        byte[] armoredCert = sop.extractCert()
                .key(TestData.CAROL_KEY.getBytes(StandardCharsets.UTF_8))
                .getBytes();
        JUtils.assertAsciiArmorEquals(TestData.CAROL_CERT.getBytes(StandardCharsets.UTF_8), armoredCert);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void extractUnarmoredCertFromArmoredKeyTest(SOP sop) throws IOException {
        InputStream keyIn = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getInputStream();

        byte[] cert = sop.extractCert()
                .noArmor()
                .key(keyIn)
                .getBytes();

        Assertions.assertFalse(JUtils.arrayStartsWith(cert, TestData.BEGIN_PGP_PUBLIC_KEY_BLOCK));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void extractArmoredCertFromUnarmoredKeyTest(SOP sop) throws IOException {
        InputStream keyIn = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .noArmor()
                .generate()
                .getInputStream();

        byte[] cert = sop.extractCert()
                .key(keyIn)
                .getBytes();

        JUtils.assertArrayStartsWith(cert, TestData.BEGIN_PGP_PUBLIC_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(cert, TestData.END_PGP_PUBLIC_KEY_BLOCK);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void extractUnarmoredCertFromUnarmoredKeyTest(SOP sop) throws IOException {
        InputStream keyIn = sop.generateKey()
                .noArmor()
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getInputStream();

        byte[] cert = sop.extractCert()
                .noArmor()
                .key(keyIn)
                .getBytes();

        Assertions.assertFalse(JUtils.arrayStartsWith(cert, TestData.BEGIN_PGP_PUBLIC_KEY_BLOCK));
    }
}
