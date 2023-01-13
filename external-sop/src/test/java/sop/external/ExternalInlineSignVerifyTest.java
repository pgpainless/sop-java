// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import sop.ByteArrayAndResult;
import sop.Verification;
import sop.enums.InlineSignAs;
import sop.exception.SOPGPException;
import sop.util.UTCUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalInlineSignVerifyTest extends AbstractExternalSOPTest {

    private static final String BEGIN_PGP_MESSAGE = "-----BEGIN PGP MESSAGE-----\n";
    private static final byte[] BEGIN_PGP_MESSAGE_BYTES = BEGIN_PGP_MESSAGE.getBytes(StandardCharsets.UTF_8);
    private static final String BEGIN_PGP_SIGNED_MESSAGE = "-----BEGIN PGP SIGNED MESSAGE-----\n";
    private static final byte[] BEGIN_PGP_SIGNED_MESSAGE_BYTES = BEGIN_PGP_SIGNED_MESSAGE.getBytes(StandardCharsets.UTF_8);

    @Test
    public void inlineSignVerifyAlice() throws IOException {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported

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
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported

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
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported

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

    @Test
    public void assertNotBeforeThrowsNoSignature() {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported
        ignoreIf("sqop", Is.leq, "0.27.2"); // returns 1 instead of 3 (NO_SIGNATURE)

        byte[] message = ("-----BEGIN PGP MESSAGE-----\n" +
                "\n" +
                "owGbwMvMwCX2yTCUx9/9cR/jaZEkBhDwSM3JyddRCM8vyklR5OooZWEQ42JQZ2VK\n" +
                "PjjpPacATLmYIsvr1t3xi61KH8ZN8UuGCTMwpPcw/E9jS+vcvPu2gmp4jcRbcSNP\n" +
                "FYmW8hmLJdUVrdt1V8w6GM/IMEvN0tP339sNGX4swq8T5p62q3jUfLjpstmcI6Ie\n" +
                "sfcfswMA\n" +
                "=RDAo\n" +
                "-----END PGP MESSAGE-----").getBytes(StandardCharsets.UTF_8);
        Date signatureDate = UTCUtil.parseUTCDate("2023-01-13T17:20:47Z");
        Date afterSignature = new Date(signatureDate.getTime() + 1000); // 1 sec before sig

        assertThrows(SOPGPException.NoSignature.class, () -> getSop().inlineVerify()
                .notBefore(afterSignature)
                .cert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult());
    }

    @Test
    public void assertNotAfterThrowsNoSignature() {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported
        ignoreIf("sqop", Is.leq, "0.27.2"); // returns 1 instead of 3 (NO_SIGNATURE)

        byte[] message = ("-----BEGIN PGP MESSAGE-----\n" +
                "\n" +
                "owGbwMvMwCX2yTCUx9/9cR/jaZEkBhDwSM3JyddRCM8vyklR5OooZWEQ42JQZ2VK\n" +
                "PjjpPacATLmYIsvr1t3xi61KH8ZN8UuGCTMwpPcw/E9jS+vcvPu2gmp4jcRbcSNP\n" +
                "FYmW8hmLJdUVrdt1V8w6GM/IMEvN0tP339sNGX4swq8T5p62q3jUfLjpstmcI6Ie\n" +
                "sfcfswMA\n" +
                "=RDAo\n" +
                "-----END PGP MESSAGE-----").getBytes(StandardCharsets.UTF_8);
        Date signatureDate = UTCUtil.parseUTCDate("2023-01-13T17:20:47Z");
        Date beforeSignature = new Date(signatureDate.getTime() - 1000); // 1 sec before sig

        assertThrows(SOPGPException.NoSignature.class, () -> getSop().inlineVerify()
                .notAfter(beforeSignature)
                .cert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult());
    }

    @Test
    public void signVerifyWithPasswordProtectedKey() throws IOException {
        ignoreIf("sqop", Is.leq, "0.26.1"); // inline-sign not supported

        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        byte[] keyPassword = "sw0rdf1sh".getBytes(StandardCharsets.UTF_8);
        byte[] key = getSop().generateKey()
                .userId("Alice <alice@openpgp.org>")
                .withKeyPassword(keyPassword)
                .generate()
                .getBytes();
        byte[] cert = getSop().extractCert()
                .key(key)
                .getBytes();

        byte[] inlineSigned = getSop().inlineSign()
                .withKeyPassword(keyPassword)
                .key(key)
                .mode(InlineSignAs.binary)
                .data(message)
                .getBytes();

        assertFalse(getSop().inlineVerify()
                .cert(cert)
                .data(inlineSigned)
                .toByteArrayAndResult()
                .getResult()
                .isEmpty());
    }
}
