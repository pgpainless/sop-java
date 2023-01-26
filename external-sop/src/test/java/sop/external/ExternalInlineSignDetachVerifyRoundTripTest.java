// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sop.ByteArrayAndResult;
import sop.SOP;
import sop.Signatures;
import sop.Verification;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static sop.testing.JUtils.arrayStartsWith;
import static sop.testing.JUtils.assertArrayStartsWith;
import static sop.testing.TestData.ALICE_CERT;
import static sop.testing.TestData.ALICE_KEY;
import static sop.testing.TestData.BEGIN_PGP_SIGNATURE;
import static sop.testing.TestData.PLAINTEXT;

@EnabledIf("sop.external.AbstractExternalSOPTest#hasBackends")
public class ExternalInlineSignDetachVerifyRoundTripTest extends AbstractExternalSOPTest {

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void inlineSignThenDetachThenDetachedVerifyTest(SOP sop) throws IOException {
        byte[] message = PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = sop.inlineSign()
                .key(ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .getBytes();

        ByteArrayAndResult<Signatures> bytesAndResult = sop.inlineDetach()
                .message(inlineSigned)
                .toByteArrayAndResult();

        byte[] plaintext = bytesAndResult.getBytes();
        assertArrayEquals(message, plaintext);

        byte[] signatures = bytesAndResult.getResult()
                .getBytes();

        List<Verification> verifications = sop.detachedVerify()
                .cert(ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signatures)
                .data(plaintext);

        assertFalse(verifications.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("sop.external.AbstractExternalSOPTest#provideBackends")
    public void inlineSignThenDetachNoArmorThenArmorThenDetachedVerifyTest(SOP sop) throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = sop.inlineSign()
                .key(ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .getBytes();

        ByteArrayAndResult<Signatures> bytesAndResult = sop.inlineDetach()
                .noArmor()
                .message(inlineSigned)
                .toByteArrayAndResult();

        byte[] plaintext = bytesAndResult.getBytes();
        assertArrayEquals(message, plaintext);

        byte[] signatures = bytesAndResult.getResult()
                .getBytes();
        assertFalse(arrayStartsWith(signatures, BEGIN_PGP_SIGNATURE));

        byte[] armored = sop.armor()
                .data(signatures)
                .getBytes();
        assertArrayStartsWith(armored, BEGIN_PGP_SIGNATURE);

        List<Verification> verifications = sop.detachedVerify()
                .cert(ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(armored)
                .data(plaintext);

        assertFalse(verifications.isEmpty());
    }
}
