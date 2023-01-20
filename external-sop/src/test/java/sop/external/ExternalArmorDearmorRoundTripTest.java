// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static sop.external.JUtils.arrayStartsWith;
import static sop.external.JUtils.assertArrayStartsWith;
import static sop.external.JUtils.assertAsciiArmorEquals;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalArmorDearmorRoundTripTest extends AbstractExternalSOPTest {

    private static final String BEGIN_PGP_PRIVATE_KEY_BLOCK = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n";
    private static final byte[] BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES = BEGIN_PGP_PRIVATE_KEY_BLOCK.getBytes(StandardCharsets.UTF_8);
    private static final String BEGIN_PGP_PUBLIC_KEY_BLOCK = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n";
    private static final byte[] BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES = BEGIN_PGP_PUBLIC_KEY_BLOCK.getBytes(StandardCharsets.UTF_8);
    private static final String BEGIN_PGP_MESSAGE = "-----BEGIN PGP MESSAGE-----\n";
    private static final byte[] BEGIN_PGP_MESSAGE_BYTES = BEGIN_PGP_MESSAGE.getBytes(StandardCharsets.UTF_8);
    private static final String BEGIN_PGP_SIGNATURE = "-----BEGIN PGP SIGNATURE-----\n";
    private static final byte[] BEGIN_PGP_SIGNATURE_BYTES = BEGIN_PGP_SIGNATURE.getBytes(StandardCharsets.UTF_8);

    @Test
    public void dearmorArmorAliceKey() throws IOException {
        byte[] aliceKey = TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = getSop().dearmor()
                .data(aliceKey)
                .getBytes();

        assertFalse(arrayStartsWith(dearmored, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES));

        byte[] armored = getSop().armor()
                .data(dearmored)
                .getBytes();

        assertArrayStartsWith(armored, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
        assertAsciiArmorEquals(aliceKey, armored);
    }

    @Test
    public void dearmorArmorAliceCert() throws IOException {
        byte[] aliceCert = TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = getSop().dearmor()
                .data(aliceCert)
                .getBytes();

        assertFalse(arrayStartsWith(dearmored, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES));

        byte[] armored = getSop().armor()
                .data(dearmored)
                .getBytes();

        assertArrayStartsWith(armored, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES);
        assertAsciiArmorEquals(aliceCert, armored);
    }

    @Test
    public void dearmorArmorBobKey() throws IOException {
        byte[] bobKey = TestKeys.BOB_KEY.getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = getSop().dearmor()
                .data(bobKey)
                .getBytes();

        assertFalse(arrayStartsWith(dearmored, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES));

        byte[] armored = getSop().armor()
                .data(dearmored)
                .getBytes();

        assertArrayStartsWith(armored, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
        assertAsciiArmorEquals(bobKey, armored);
    }

    @Test
    public void dearmorArmorBobCert() throws IOException {
        byte[] bobCert = TestKeys.BOB_CERT.getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = getSop().dearmor()
                .data(bobCert)
                .getBytes();

        assertFalse(arrayStartsWith(dearmored, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES));

        byte[] armored = getSop().armor()
                .data(dearmored)
                .getBytes();

        assertArrayStartsWith(armored, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES);
        assertAsciiArmorEquals(bobCert, armored);
    }

    @Test
    public void dearmorArmorCarolKey() throws IOException {
        byte[] carolKey = TestKeys.CAROL_KEY.getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = getSop().dearmor()
                .data(carolKey)
                .getBytes();

        assertFalse(arrayStartsWith(dearmored, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES));

        byte[] armored = getSop().armor()
                .data(dearmored)
                .getBytes();

        assertArrayStartsWith(armored, BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES);
        assertAsciiArmorEquals(carolKey, armored);
    }

    @Test
    public void dearmorArmorCarolCert() throws IOException {
        byte[] carolCert = TestKeys.CAROL_CERT.getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = getSop().dearmor()
                .data(carolCert)
                .getBytes();

        assertFalse(arrayStartsWith(dearmored, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES));

        byte[] armored = getSop().armor()
                .data(dearmored)
                .getBytes();

        assertArrayStartsWith(armored, BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES);
        assertAsciiArmorEquals(carolCert, armored);
    }

    @Test
    public void dearmorArmorMessage() throws IOException {
        ignoreIf("sqop", Is.leq, "0.26.1"); // falsely reports Invalid Data Type
        byte[] message = ("-----BEGIN PGP MESSAGE-----\n" +
                "\n" +
                "wV4DR2b2udXyHrYSAQdAMZy9Iqb1IxszjI3v+TsfK//0lnJ9PKHDqVAB5ohp+RMw\n" +
                "8fmuL3phS9uISFT/DrizC8ALJhMqw5R+lLB/RvTTA/qS6tN5dRyL+YLFU3/N0CRF\n" +
                "0j8BtQEsMmRo60LzUq/OBI0dFjwFq1efpfOGkpRYkuIzndCjBEgnLUkrHzUc1uD9\n" +
                "CePQFpprprnGEzpE3flQLUc=\n" +
                "=ZiFR\n" +
                "-----END PGP MESSAGE-----\n").getBytes(StandardCharsets.UTF_8);
        byte[] dearmored = getSop().dearmor()
                .data(message)
                .getBytes();

        assertFalse(arrayStartsWith(dearmored, BEGIN_PGP_MESSAGE_BYTES));

        byte[] armored = getSop().armor()
                .data(dearmored)
                .getBytes();

        assertArrayStartsWith(armored, BEGIN_PGP_MESSAGE_BYTES);
        assertAsciiArmorEquals(message, armored);
    }

    @Test
    public void dearmorArmorSignature() throws IOException {
        byte[] signature = ("-----BEGIN PGP SIGNATURE-----\n" +
                "\n" +
                "wr0EABYKAG8FgmPBdRAJEPIxVQxPR+OORxQAAAAAAB4AIHNhbHRAbm90YXRpb25z\n" +
                "LnNlcXVvaWEtcGdwLm9yZ2un17fF3C46Adgzp0mU4RG8Txy/T/zOBcBw/NYaLGrQ\n" +
                "FiEE64W7X6M6deFelE5j8jFVDE9H444AAMiEAP9LBQWLo4oP5IrFZPuSUQSPsUxB\n" +
                "c+Qu1raXDKzS/8Q9IAD+LnHIjRHcqNPobNHXF/saXIYXeZR+LJKszTJozzwqdQE=\n" +
                "=GHvQ\n" +
                "-----END PGP SIGNATURE-----\n").getBytes(StandardCharsets.UTF_8);

        byte[] dearmored = getSop().dearmor()
                .data(signature)
                .getBytes();

        assertFalse(arrayStartsWith(dearmored, BEGIN_PGP_SIGNATURE_BYTES));

        byte[] armored = getSop().armor()
                .data(dearmored)
                .getBytes();

        assertArrayStartsWith(armored, BEGIN_PGP_SIGNATURE_BYTES);
        assertAsciiArmorEquals(signature, armored);
    }

    @Test
    public void testDearmoringTwiceIsIdempotent() throws IOException {
        ignoreIf("sqop", Is.eq, "0.27.2"); // IO error because: EOF

        byte[] dearmored = getSop().dearmor()
                .data(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .getBytes();

        byte[] dearmoredAgain = getSop().dearmor()
                .data(dearmored)
                .getBytes();

        assertArrayEquals(dearmored, dearmoredAgain);
    }

    @Test
    public void testArmoringTwiceIsIdempotent() throws IOException {
        byte[] armored = ("-----BEGIN PGP SIGNATURE-----\n" +
                "\n" +
                "wr0EABYKAG8FgmPBdRAJEPIxVQxPR+OORxQAAAAAAB4AIHNhbHRAbm90YXRpb25z\n" +
                "LnNlcXVvaWEtcGdwLm9yZ2un17fF3C46Adgzp0mU4RG8Txy/T/zOBcBw/NYaLGrQ\n" +
                "FiEE64W7X6M6deFelE5j8jFVDE9H444AAMiEAP9LBQWLo4oP5IrFZPuSUQSPsUxB\n" +
                "c+Qu1raXDKzS/8Q9IAD+LnHIjRHcqNPobNHXF/saXIYXeZR+LJKszTJozzwqdQE=\n" +
                "=GHvQ\n" +
                "-----END PGP SIGNATURE-----\n").getBytes(StandardCharsets.UTF_8);

        byte[] armoredAgain = getSop().armor()
                .data(armored)
                .getBytes();

        assertAsciiArmorEquals(armored, armoredAgain);
    }

}