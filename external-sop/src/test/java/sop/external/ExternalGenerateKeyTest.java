// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static sop.external.JUtils.assertArrayStartsWith;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalGenerateKeyTest extends AbstractExternalSOPTest {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final String BEGIN_PGP_PRIVATE_KEY_BLOCK = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n";
    byte[] BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES = BEGIN_PGP_PRIVATE_KEY_BLOCK.getBytes(UTF8);

    @Test
    public void generateKeyTest() throws IOException {
        byte[] key = getSop().generateKey()
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getBytes();

        assertArrayStartsWith(key, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
    }

    @Test
    public void generateKeyNoArmor() throws IOException {
        byte[] key = getSop().generateKey()
                .userId("Alice <alice@openpgp.org>")
                .noArmor()
                .generate()
                .getBytes();

        assertFalse(JUtils.arrayStartsWith(key, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES));
    }

    @Test
    public void generateKeyWithMultipleUserIdsTest() throws IOException {
        byte[] key = getSop().generateKey()
                .userId("Alice <alice@openpgp.org>")
                .userId("Bob <bob@openpgp.org>")
                .generate()
                .getBytes();

        assertArrayStartsWith(key, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
    }

    @Test
    public void generateKeyWithoutUserIdTest() throws IOException {
        ignoreIf("pgpainless-cli", Is.le, "1.3.15");

        byte[] key = getSop().generateKey()
                .generate()
                .getBytes();

        assertArrayStartsWith(key, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
    }

    @Test
    public void generateKeyWithPasswordTest() throws IOException {
        ignoreIf("sqop", Is.le, "0.27.0");
        ignoreIf("pgpainless-cli", Is.le, "1.3.0");

        byte[] key = getSop().generateKey()
                .userId("Alice <alice@openpgp.org>")
                .withKeyPassword("sw0rdf1sh")
                .generate()
                .getBytes();

        assertArrayStartsWith(key, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
    }

    @Test
    public void generateKeyWithMultipleUserIdsAndPassword() throws IOException {
        ignoreIf("sqop", Is.le, "0.27.0");
        ignoreIf("PGPainless-SOP", Is.le, "1.3.15");
        ignoreIf("PGPainless-SOP", Is.eq, "1.4.0");
        ignoreIf("PGPainless-SOP", Is.eq, "1.4.1");

        byte[] key = getSop().generateKey()
                .userId("Alice <alice@openpgp.org>")
                .userId("Bob <bob@openpgp.org>")
                .withKeyPassword("sw0rdf1sh")
                .generate()
                .getBytes();

        assertArrayStartsWith(key, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
    }
}
