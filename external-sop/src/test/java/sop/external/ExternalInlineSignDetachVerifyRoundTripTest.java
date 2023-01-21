// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import sop.ByteArrayAndResult;
import sop.Signatures;
import sop.Verification;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static sop.external.JUtils.arrayStartsWith;
import static sop.external.JUtils.assertArrayStartsWith;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalInlineSignDetachVerifyRoundTripTest extends AbstractExternalSOPTest {

    private static final byte[] BEGIN_PGP_SIGNATURE = "-----BEGIN PGP SIGNATURE-----\n".getBytes(StandardCharsets.UTF_8);

    @Test
    public void inlineSignThenDetachThenDetachedVerifyTest() throws IOException {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported

        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = getSop().inlineSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .getBytes();

        ByteArrayAndResult<Signatures> bytesAndResult = getSop().inlineDetach()
                .message(inlineSigned)
                .toByteArrayAndResult();

        byte[] plaintext = bytesAndResult.getBytes();
        assertArrayEquals(message, plaintext);

        byte[] signatures = bytesAndResult.getResult()
                .getBytes();

        List<Verification> verifications = getSop().detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signatures)
                .data(plaintext);

        assertFalse(verifications.isEmpty());
    }

    @Test
    public void inlineSignThenDetachNoArmorThenArmorThenDetachedVerifyTest() throws IOException {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported

        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = getSop().inlineSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .getBytes();

        ByteArrayAndResult<Signatures> bytesAndResult = getSop().inlineDetach()
                .noArmor()
                .message(inlineSigned)
                .toByteArrayAndResult();

        byte[] plaintext = bytesAndResult.getBytes();
        assertArrayEquals(message, plaintext);

        byte[] signatures = bytesAndResult.getResult()
                .getBytes();
        assertFalse(arrayStartsWith(signatures, BEGIN_PGP_SIGNATURE));

        byte[] armored = getSop().armor()
                .data(signatures)
                .getBytes();
        assertArrayStartsWith(armored, BEGIN_PGP_SIGNATURE);

        List<Verification> verifications = getSop().detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(armored)
                .data(plaintext);

        assertFalse(verifications.isEmpty());
    }
}
