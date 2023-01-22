// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static sop.external.JUtils.arrayStartsWith;
import static sop.external.JUtils.assertArrayStartsWith;
import static sop.external.JUtils.assertAsciiArmorEquals;

@EnabledIf("sop.external.AbstractExternalSOPTest#hasBackends")
public class ExternalExtractCertTest extends AbstractExternalSOPTest {

    private static final String BEGIN_PGP_PUBLIC_KEY_BLOCK = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n";
    private static final byte[] BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES = BEGIN_PGP_PUBLIC_KEY_BLOCK.getBytes(StandardCharsets.UTF_8);

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void extractArmoredCertFromArmoredKeyTest(SOP sop) throws IOException {
        InputStream keyIn = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getInputStream();

        byte[] cert = sop.extractCert().key(keyIn).getBytes();
        assertArrayStartsWith(cert, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES);
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

        assertFalse(arrayStartsWith(cert, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES));
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

        assertArrayStartsWith(cert, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES);
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

        assertFalse(arrayStartsWith(cert, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES));
    }
}
