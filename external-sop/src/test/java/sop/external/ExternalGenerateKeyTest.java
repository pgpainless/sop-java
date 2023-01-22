// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static sop.external.JUtils.assertArrayStartsWith;

@EnabledIf("sop.external.AbstractExternalSOPTest#hasBackends")
public class ExternalGenerateKeyTest extends AbstractExternalSOPTest {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final String BEGIN_PGP_PRIVATE_KEY_BLOCK = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n";
    byte[] BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES = BEGIN_PGP_PRIVATE_KEY_BLOCK.getBytes(UTF8);

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void generateKeyTest(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getBytes();

        assertArrayStartsWith(key, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void generateKeyNoArmor(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .noArmor()
                .generate()
                .getBytes();

        assertFalse(JUtils.arrayStartsWith(key, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES));
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void generateKeyWithMultipleUserIdsTest(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .userId("Bob <bob@openpgp.org>")
                .generate()
                .getBytes();

        assertArrayStartsWith(key, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void generateKeyWithoutUserIdTest(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .generate()
                .getBytes();

        assertArrayStartsWith(key, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void generateKeyWithPasswordTest(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .withKeyPassword("sw0rdf1sh")
                .generate()
                .getBytes();

        assertArrayStartsWith(key, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void generateKeyWithMultipleUserIdsAndPassword(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .userId("Bob <bob@openpgp.org>")
                .withKeyPassword("sw0rdf1sh")
                .generate()
                .getBytes();

        assertArrayStartsWith(key, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
    }
}
