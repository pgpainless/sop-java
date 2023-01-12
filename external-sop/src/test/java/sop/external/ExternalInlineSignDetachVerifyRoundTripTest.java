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

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalInlineSignDetachVerifyRoundTripTest extends AbstractExternalSOPTest {

    @Test
    public void inlineSignThenDetachThenDetachedVerifyTest() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = getSop().inlineSign()
                .key(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
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
                .cert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signatures)
                .data(plaintext);

        assertFalse(verifications.isEmpty());
    }
}
