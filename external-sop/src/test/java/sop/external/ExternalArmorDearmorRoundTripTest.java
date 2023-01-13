// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static sop.external.JUtils.arrayStartsWith;
import static sop.external.JUtils.assertArrayStartsWith;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalArmorDearmorRoundTripTest extends AbstractExternalSOPTest {

    private static final String BEGIN_PGP_PRIVATE_KEY_BLOCK = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n";
    private static final byte[] BEGIN_PGP_PRIVATE_KEY_BLOCK_BYTES = BEGIN_PGP_PRIVATE_KEY_BLOCK.getBytes(StandardCharsets.UTF_8);
    private static final String BEGIN_PGP_PUBLIC_KEY_BLOCK = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n";
    private static final byte[] BEGIN_PGP_PUBLIC_KEY_BLOCK_BYTES = BEGIN_PGP_PUBLIC_KEY_BLOCK.getBytes(StandardCharsets.UTF_8);

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
    }
}
