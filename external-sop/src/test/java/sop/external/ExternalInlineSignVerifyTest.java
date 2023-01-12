// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import sop.ByteArrayAndResult;
import sop.Verification;
import sop.enums.InlineSignAs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalInlineSignVerifyTest extends AbstractExternalSOPTest {

    private static final String BEGIN_PGP_MESSAGE = "-----BEGIN PGP MESSAGE-----\n";
    private static final byte[] BEGIN_PGP_MESSAGE_BYTES = BEGIN_PGP_MESSAGE.getBytes(StandardCharsets.UTF_8);
    private static final String BEGIN_PGP_SIGNED_MESSAGE = "-----BEGIN PGP SIGNED MESSAGE-----\n";
    private static final byte[] BEGIN_PGP_SIGNED_MESSAGE_BYTES = BEGIN_PGP_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);

    @Test
    public void inlineSignVerifyAlice() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = getSop().inlineSign()
                .key(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .getBytes();

        JUtils.assertArrayStartsWith(inlineSigned, BEGIN_PGP_MESSAGE_BYTES);

        ByteArrayAndResult<List<Verification>> bytesAndResult = getSop().inlineVerify()
                .cert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(inlineSigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        assertFalse(bytesAndResult.getResult().isEmpty());
    }

    @Test
    public void inlineSignVerifyAliceNoArmor() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] inlineSigned = getSop().inlineSign()
                .key(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .noArmor()
                .data(message)
                .getBytes();

        assertFalse(JUtils.arrayStartsWith(inlineSigned, BEGIN_PGP_MESSAGE_BYTES));

        ByteArrayAndResult<List<Verification>> bytesAndResult = getSop().inlineVerify()
                .cert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(inlineSigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        assertFalse(bytesAndResult.getResult().isEmpty());
    }

    @Test
    public void clearsignVerifyAlice() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] clearsigned = getSop().inlineSign()
                .key(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(InlineSignAs.clearsigned)
                .data(message)
                .getBytes();

        JUtils.assertArrayStartsWith(clearsigned, BEGIN_PGP_SIGNED_MESSAGE_BYTES);

        ByteArrayAndResult<List<Verification>> bytesAndResult = getSop().inlineVerify()
                .cert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(clearsigned)
                .toByteArrayAndResult();

        assertArrayEquals(message, bytesAndResult.getBytes());
        assertFalse(bytesAndResult.getResult().isEmpty());
    }
}
