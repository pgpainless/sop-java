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
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@EnabledIf("sop.operation.AbstractSOPTest#hasBackends")
public class ArmorDearmorTest {

    static Stream<Arguments> provideInstances() {
        return AbstractSOPTest.provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void dearmorArmorAliceKey(SOP sop) throws IOException {
        byte[] aliceKey = TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = sop.dearmor()
                .data(aliceKey)
                .getBytes();

        Assertions.assertFalse(JUtils.arrayStartsWith(dearmored, TestData.BEGIN_PGP_PRIVATE_KEY_BLOCK));

        byte[] armored = sop.armor()
                .data(dearmored)
                .getBytes();

        JUtils.assertArrayStartsWith(armored, TestData.BEGIN_PGP_PRIVATE_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(armored, TestData.END_PGP_PRIVATE_KEY_BLOCK);

        // assertAsciiArmorEquals(aliceKey, armored);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void dearmorArmorAliceCert(SOP sop) throws IOException {
        byte[] aliceCert = TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = sop.dearmor()
                .data(aliceCert)
                .getBytes();

        Assertions.assertFalse(JUtils.arrayStartsWith(dearmored, TestData.BEGIN_PGP_PUBLIC_KEY_BLOCK));

        byte[] armored = sop.armor()
                .data(dearmored)
                .getBytes();

        JUtils.assertArrayStartsWith(armored, TestData.BEGIN_PGP_PUBLIC_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(armored, TestData.END_PGP_PUBLIC_KEY_BLOCK);

        // assertAsciiArmorEquals(aliceCert, armored);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void dearmorArmorBobKey(SOP sop) throws IOException {
        byte[] bobKey = TestData.BOB_KEY.getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = sop.dearmor()
                .data(bobKey)
                .getBytes();

        Assertions.assertFalse(JUtils.arrayStartsWith(dearmored, TestData.BEGIN_PGP_PRIVATE_KEY_BLOCK));

        byte[] armored = sop.armor()
                .data(dearmored)
                .getBytes();

        JUtils.assertArrayStartsWith(armored, TestData.BEGIN_PGP_PRIVATE_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(armored, TestData.END_PGP_PRIVATE_KEY_BLOCK);

        // assertAsciiArmorEquals(bobKey, armored);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void dearmorArmorBobCert(SOP sop) throws IOException {
        byte[] bobCert = TestData.BOB_CERT.getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = sop.dearmor()
                .data(bobCert)
                .getBytes();

        Assertions.assertFalse(JUtils.arrayStartsWith(dearmored, TestData.BEGIN_PGP_PUBLIC_KEY_BLOCK));

        byte[] armored = sop.armor()
                .data(dearmored)
                .getBytes();

        JUtils.assertArrayStartsWith(armored, TestData.BEGIN_PGP_PUBLIC_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(armored, TestData.END_PGP_PUBLIC_KEY_BLOCK);

        // assertAsciiArmorEquals(bobCert, armored);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void dearmorArmorCarolKey(SOP sop) throws IOException {
        byte[] carolKey = TestData.CAROL_KEY.getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = sop.dearmor()
                .data(carolKey)
                .getBytes();

        Assertions.assertFalse(JUtils.arrayStartsWith(dearmored, TestData.BEGIN_PGP_PRIVATE_KEY_BLOCK));

        byte[] armored = sop.armor()
                .data(dearmored)
                .getBytes();

        JUtils.assertArrayStartsWith(armored, TestData.BEGIN_PGP_PRIVATE_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(armored, TestData.END_PGP_PRIVATE_KEY_BLOCK);

        // assertAsciiArmorEquals(carolKey, armored);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void dearmorArmorCarolCert(SOP sop) throws IOException {
        byte[] carolCert = TestData.CAROL_CERT.getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = sop.dearmor()
                .data(carolCert)
                .getBytes();

        Assertions.assertFalse(JUtils.arrayStartsWith(dearmored, TestData.BEGIN_PGP_PUBLIC_KEY_BLOCK));

        byte[] armored = sop.armor()
                .data(dearmored)
                .getBytes();

        JUtils.assertArrayStartsWith(armored, TestData.BEGIN_PGP_PUBLIC_KEY_BLOCK);
        JUtils.assertArrayEndsWithIgnoreNewlines(armored, TestData.END_PGP_PUBLIC_KEY_BLOCK);

        // assertAsciiArmorEquals(carolCert, armored);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void dearmorArmorMessage(SOP sop) throws IOException {
        byte[] message = ("-----BEGIN PGP MESSAGE-----\n" +
                "\n" +
                "wV4DR2b2udXyHrYSAQdAMZy9Iqb1IxszjI3v+TsfK//0lnJ9PKHDqVAB5ohp+RMw\n" +
                "8fmuL3phS9uISFT/DrizC8ALJhMqw5R+lLB/RvTTA/qS6tN5dRyL+YLFU3/N0CRF\n" +
                "0j8BtQEsMmRo60LzUq/OBI0dFjwFq1efpfOGkpRYkuIzndCjBEgnLUkrHzUc1uD9\n" +
                "CePQFpprprnGEzpE3flQLUc=\n" +
                "=ZiFR\n" +
                "-----END PGP MESSAGE-----\n").getBytes(StandardCharsets.UTF_8);
        byte[] dearmored = sop.dearmor()
                .data(message)
                .getBytes();

        Assertions.assertFalse(JUtils.arrayStartsWith(dearmored, TestData.BEGIN_PGP_MESSAGE));

        byte[] armored = sop.armor()
                .data(dearmored)
                .getBytes();

        JUtils.assertArrayStartsWith(armored, TestData.BEGIN_PGP_MESSAGE);
        JUtils.assertArrayEndsWithIgnoreNewlines(armored, TestData.END_PGP_MESSAGE);

        // assertAsciiArmorEquals(message, armored);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void dearmorArmorSignature(SOP sop) throws IOException {
        byte[] signature = ("-----BEGIN PGP SIGNATURE-----\n" +
                "\n" +
                "wr0EABYKAG8FgmPBdRAJEPIxVQxPR+OORxQAAAAAAB4AIHNhbHRAbm90YXRpb25z\n" +
                "LnNlcXVvaWEtcGdwLm9yZ2un17fF3C46Adgzp0mU4RG8Txy/T/zOBcBw/NYaLGrQ\n" +
                "FiEE64W7X6M6deFelE5j8jFVDE9H444AAMiEAP9LBQWLo4oP5IrFZPuSUQSPsUxB\n" +
                "c+Qu1raXDKzS/8Q9IAD+LnHIjRHcqNPobNHXF/saXIYXeZR+LJKszTJozzwqdQE=\n" +
                "=GHvQ\n" +
                "-----END PGP SIGNATURE-----\n").getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = sop.dearmor()
                .data(signature)
                .getBytes();

        Assertions.assertFalse(JUtils.arrayStartsWith(dearmored, TestData.BEGIN_PGP_SIGNATURE));

        byte[] armored = sop.armor()
                .data(dearmored)
                .getBytes();

        JUtils.assertArrayStartsWith(armored, TestData.BEGIN_PGP_SIGNATURE);
        JUtils.assertArrayEndsWithIgnoreNewlines(armored, TestData.END_PGP_SIGNATURE);

        JUtils.assertAsciiArmorEquals(signature, armored);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void testDearmoringTwiceIsIdempotent(SOP sop) throws IOException {
        byte[] dearmored = sop.dearmor()
                .data(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .getBytes();

        byte[] dearmoredAgain = sop.dearmor()
                .data(dearmored)
                .getBytes();

        assertArrayEquals(dearmored, dearmoredAgain);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void testArmoringTwiceIsIdempotent(SOP sop) throws IOException {
        byte[] armored = ("-----BEGIN PGP SIGNATURE-----\n" +
                "\n" +
                "wr0EABYKAG8FgmPBdRAJEPIxVQxPR+OORxQAAAAAAB4AIHNhbHRAbm90YXRpb25z\n" +
                "LnNlcXVvaWEtcGdwLm9yZ2un17fF3C46Adgzp0mU4RG8Txy/T/zOBcBw/NYaLGrQ\n" +
                "FiEE64W7X6M6deFelE5j8jFVDE9H444AAMiEAP9LBQWLo4oP5IrFZPuSUQSPsUxB\n" +
                "c+Qu1raXDKzS/8Q9IAD+LnHIjRHcqNPobNHXF/saXIYXeZR+LJKszTJozzwqdQE=\n" +
                "=GHvQ\n" +
                "-----END PGP SIGNATURE-----\n").getBytes(StandardCharsets.UTF_8);

        byte[] armoredAgain = sop.armor()
                .data(armored)
                .getBytes();

        JUtils.assertAsciiArmorEquals(armored, armoredAgain);
    }

}
