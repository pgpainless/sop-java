// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.ByteArrayAndResult;
import sop.SOP;
import sop.Signatures;
import sop.Verification;
import sop.testsuite.JUtils;
import sop.testsuite.TestData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnabledIf("sop.operation.AbstractSOPTest#hasBackends")
public class InlineSignInlineDetachDetachedVerifyTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void inlineSignThenDetachThenDetachedVerifyTest(SOP sop) throws IOException {
        byte[] message = TestData.PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = sop.inlineSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
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
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signatures)
                .data(plaintext);

        assertFalse(verifications.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void inlineSignThenDetachNoArmorThenArmorThenDetachedVerifyTest(SOP sop) throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = sop.inlineSign()
                .key(TestData.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
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
        Assertions.assertFalse(JUtils.arrayStartsWith(signatures, TestData.BEGIN_PGP_SIGNATURE));

        byte[] armored = sop.armor()
                .data(signatures)
                .getBytes();
        JUtils.assertArrayStartsWith(armored, TestData.BEGIN_PGP_SIGNATURE);

        List<Verification> verifications = sop.detachedVerify()
                .cert(TestData.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(armored)
                .data(plaintext);

        assertFalse(verifications.isEmpty());
    }
}
