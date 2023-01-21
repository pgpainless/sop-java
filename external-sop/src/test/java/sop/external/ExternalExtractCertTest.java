// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static sop.external.JUtils.arrayStartsWith;
import static sop.external.JUtils.assertArrayStartsWith;
import static sop.external.JUtils.assertAsciiArmorEquals;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalExtractCertTest extends AbstractExternalSOPTest {

    private static final String BEGIN_PGP_PUBLIC_KEY_BLOCK = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n";
    private static final byte[] BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES = BEGIN_PGP_PUBLIC_KEY_BLOCK.getBytes(StandardCharsets.UTF_8);

    @Test
    public void extractArmoredCertFromArmoredKeyTest() throws IOException {
        InputStream keyIn = getSop().generateKey()
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getInputStream();

        byte[] cert = getSop().extractCert().key(keyIn).getBytes();
        assertArrayStartsWith(cert, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES);
    }

    @Test
    public void extractAliceCertFromAliceKeyTest() throws IOException {
        ignoreIf("PGPainless-SOP", Is.geq, "0.0.0"); // PGPainless uses old CTB
        byte[] armoredCert = getSop().extractCert()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .getBytes();
        assertAsciiArmorEquals(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8), armoredCert);
    }

    @Test
    public void extractBobsCertFromBobsKeyTest() throws IOException {
        ignoreIf("PGPainless-SOP", Is.geq, "0.0.0"); // PGPainless uses old CTB
        byte[] armoredCert = getSop().extractCert()
                .key(TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .getBytes();
        assertAsciiArmorEquals(TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8), armoredCert);
    }

    @Test
    public void extractCarolsCertFromCarolsKeyTest() throws IOException {
        ignoreIf("PGPainless-SOP", Is.geq, "0.0.0"); // PGPainless uses old CTB
        byte[] armoredCert = getSop().extractCert()
                .key(TestData.CAROL_KEY.getBytes(StandardCharsets.UTF_8))
                .getBytes();
        assertAsciiArmorEquals(TestData.CAROL_CERT.getBytes(StandardCharsets.UTF_8), armoredCert);
    }

    @Test
    public void extractUnarmoredCertFromArmoredKeyTest() throws IOException {
        InputStream keyIn = getSop().generateKey()
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getInputStream();

        byte[] cert = getSop().extractCert()
                .noArmor()
                .key(keyIn)
                .getBytes();

        assertFalse(arrayStartsWith(cert, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES));
    }

    @Test
    public void extractArmoredCertFromUnarmoredKeyTest() throws IOException {
        InputStream keyIn = getSop().generateKey()
                .userId("Alice <alice@openpgp.org>")
                .noArmor()
                .generate()
                .getInputStream();

        byte[] cert = getSop().extractCert()
                .key(keyIn)
                .getBytes();

        assertArrayStartsWith(cert, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES);
    }

    @Test
    public void extractUnarmoredCertFromUnarmoredKeyTest() throws IOException {
        InputStream keyIn = getSop().generateKey()
                .noArmor()
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getInputStream();

        byte[] cert = getSop().extractCert()
                .noArmor()
                .key(keyIn)
                .getBytes();

        assertFalse(arrayStartsWith(cert, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES));
    }
}
