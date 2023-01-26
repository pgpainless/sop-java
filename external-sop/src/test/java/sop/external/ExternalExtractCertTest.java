// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;
import sop.testing.TestData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static sop.testing.JUtils.arrayStartsWith;
import static sop.testing.JUtils.assertArrayEndsWithIgnoreNewlines;
import static sop.testing.JUtils.assertArrayStartsWith;
import static sop.testing.JUtils.assertAsciiArmorEquals;
import static sop.testing.TestData.BEGIN_PGP_PUBLIC_KEY_BLOCK;
import static sop.testing.TestData.END_PGP_PUBLIC_KEY_BLOCK;

@EnabledIf("sop.external.AbstractExternalSOPTest#hasBackends")
public class ExternalExtractCertTest extends AbstractExternalSOPTest {

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void extractArmoredCertFromArmoredKeyTest(SOP sop) throws IOException {
        InputStream keyIn = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getInputStream();

        byte[] cert = sop.extractCert().key(keyIn).getBytes();
        assertArrayStartsWith(cert, BEGIN_PGP_PUBLIC_KEY_BLOCK);
        assertArrayEndsWithIgnoreNewlines(cert, END_PGP_PUBLIC_KEY_BLOCK);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void extractAliceCertFromAliceKeyTest(SOP sop) throws IOException {
        byte[] armoredCert = sop.extractCert()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .getBytes();
        assertAsciiArmorEquals(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8), armoredCert);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void extractBobsCertFromBobsKeyTest(SOP sop) throws IOException {
        byte[] armoredCert = sop.extractCert()
                .key(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .getBytes();
        assertAsciiArmorEquals(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8), armoredCert);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void extractCarolsCertFromCarolsKeyTest(SOP sop) throws IOException {
        byte[] armoredCert = sop.extractCert()
                .key(TestData.CAROL_KEY.getBytes(StandardCharsets.UTF_8))
                .getBytes();
        assertAsciiArmorEquals(TestData.CAROL_CERT.getBytes(StandardCharsets.UTF_8), armoredCert);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void extractUnarmoredCertFromArmoredKeyTest(SOP sop) throws IOException {
        InputStream keyIn = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getInputStream();

        byte[] cert = sop.extractCert()
                .noArmor()
                .key(keyIn)
                .getBytes();

        assertFalse(arrayStartsWith(cert, BEGIN_PGP_PUBLIC_KEY_BLOCK));
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void extractArmoredCertFromUnarmoredKeyTest(SOP sop) throws IOException {
        InputStream keyIn = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .noArmor()
                .generate()
                .getInputStream();

        byte[] cert = sop.extractCert()
                .key(keyIn)
                .getBytes();

        assertArrayStartsWith(cert, BEGIN_PGP_PUBLIC_KEY_BLOCK);
        assertArrayEndsWithIgnoreNewlines(cert, END_PGP_PUBLIC_KEY_BLOCK);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
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

        assertFalse(arrayStartsWith(cert, BEGIN_PGP_PUBLIC_KEY_BLOCK));
    }
}
