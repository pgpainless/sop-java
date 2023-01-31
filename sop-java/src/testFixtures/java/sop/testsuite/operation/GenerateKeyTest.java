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
import java.util.stream.Stream;

@EnabledIf("sop.testsuite.operation.AbstractSOPTest#hasBackends")
public class GenerateKeyTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void generateKeyTest(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .generate()
                .getBytes();

        JUtils.assertArrayStartsWith(key, TestData.BEGIN_PGP_PRIVATE_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(key, TestData.END_PGP_PRIVATE_KEY_BLOCK);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void generateKeyNoArmor(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .noArmor()
                .generate()
                .getBytes();

        Assertions.assertFalse(JUtils.arrayStartsWith(key, TestData.BEGIN_PGP_PRIVATE_KEY_BLOCK));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void generateKeyWithMultipleUserIdsTest(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .userId("Bob <bob@openpgp.org>")
                .generate()
                .getBytes();

        JUtils.assertArrayStartsWith(key, TestData.BEGIN_PGP_PRIVATE_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(key, TestData.END_PGP_PRIVATE_KEY_BLOCK);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void generateKeyWithoutUserIdTest(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .generate()
                .getBytes();

        JUtils.assertArrayStartsWith(key, TestData.BEGIN_PGP_PRIVATE_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(key, TestData.END_PGP_PRIVATE_KEY_BLOCK);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void generateKeyWithPasswordTest(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .withKeyPassword("sw0rdf1sh")
                .generate()
                .getBytes();

        JUtils.assertArrayStartsWith(key, TestData.BEGIN_PGP_PRIVATE_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(key, TestData.END_PGP_PRIVATE_KEY_BLOCK);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void generateKeyWithMultipleUserIdsAndPassword(SOP sop) throws IOException {
        byte[] key = sop.generateKey()
                .userId("Alice <alice@openpgp.org>")
                .userId("Bob <bob@openpgp.org>")
                .withKeyPassword("sw0rdf1sh")
                .generate()
                .getBytes();

        JUtils.assertArrayStartsWith(key, TestData.BEGIN_PGP_PRIVATE_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(key, TestData.END_PGP_PRIVATE_KEY_BLOCK);
    }
}
