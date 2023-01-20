// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import sop.Verification;
import sop.enums.SignAs;
import sop.exception.SOPGPException;
import sop.util.UTCUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sop.external.JUtils.assertArrayStartsWith;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalDetachedSignVerifyRoundTripTest extends AbstractExternalSOPTest {

    private static final String BEGIN_PGP_SIGNATURE = "-----BEGIN PGP SIGNATURE-----\n";
    private static final byte[] BEGIN_PGP_SIGNATURE_BYTES = BEGIN_PGP_SIGNATURE.getBytes(StandardCharsets.UTF_8);

    @Test
    public void signVerifyWithAliceKey() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] signature = getSop().detachedSign()
                .key(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = getSop().detachedVerify()
                .cert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertTrue(verificationList.get(0).toString().contains("EB85BB5FA33A75E15E944E63F231550C4F47E38E EB85BB5FA33A75E15E944E63F231550C4F47E38E"));
    }

    @Test
    public void signVerifyTextModeWithAliceKey() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] signature = getSop().detachedSign()
                .key(TestKeys.ALICE_KEY.getBytes(StandardCharsets.UTF_8))
                .mode(SignAs.Text)
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        List<Verification> verificationList = getSop().detachedVerify()
                .cert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertTrue(verificationList.get(0).toString().contains("EB85BB5FA33A75E15E944E63F231550C4F47E38E EB85BB5FA33A75E15E944E63F231550C4F47E38E"));
    }

    @Test
    public void signVerifyWithEncryptedKey() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] signature = getSop().detachedSign()
                .key(TestKeys.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                .withKeyPassword(TestKeys.PASSWORD)
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayStartsWith(signature, BEGIN_PGP_SIGNATURE_BYTES);

        List<Verification> verificationList = getSop().detachedVerify()
                .cert(TestKeys.PASSWORD_PROTECTED_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message);

        assertFalse(verificationList.isEmpty());
    }

    @Test
    public void signArmorVerifyWithBobKey() throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] signature = getSop().detachedSign()
                .key(TestKeys.BOB_KEY.getBytes(StandardCharsets.UTF_8))
                .noArmor()
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        byte[] armored = getSop().armor()
                .data(signature)
                .getBytes();

        List<Verification> verificationList = getSop().detachedVerify()
                .cert(TestKeys.BOB_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(armored)
                .data(message);

        assertFalse(verificationList.isEmpty());
        assertTrue(verificationList.get(0).toString().contains("D1A66E1A23B182C9980F788CFBFCC82A015E7330 D1A66E1A23B182C9980F788CFBFCC82A015E7330"));
    }

    @Test
    public void verifyNotAfterThrowsNoSignature() {
        ignoreIf("sqop", Is.leq, "0.27.2"); // returns 1 instead of 3 (NO_SIGNATURE)

        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        byte[] signature = ("-----BEGIN PGP SIGNATURE-----\n" +
                "\n" +
                "iHUEABYKACcFAmPBjZUJEPIxVQxPR+OOFiEE64W7X6M6deFelE5j8jFVDE9H444A\n" +
                "ADI/AQC6Bux6WpGYf7HO+QPV/D5iIrqZt9xPLgfUVoNJBmMZZwD+Ib+tn5pSyWUw\n" +
                "0K1UgT5roym9Fln8U5W8R03TSbfNiwE=\n" +
                "=bxPN\n" +
                "-----END PGP SIGNATURE-----").getBytes(StandardCharsets.UTF_8);
        Date signatureDate = UTCUtil.parseUTCDate("2023-01-13T16:57:57Z");
        Date beforeSignature = new Date(signatureDate.getTime() - 1000); // 1 sec before sig

        assertThrows(SOPGPException.NoSignature.class, () -> getSop().detachedVerify()
                .cert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .notAfter(beforeSignature)
                .signatures(signature)
                .data(message));
    }

    @Test
    public void verifyNotBeforeThrowsNoSignature() {
        ignoreIf("sqop", Is.leq, "0.27.2"); // returns 1 instead of 3 (NO_SIGNATURE)

        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        byte[] signature = ("-----BEGIN PGP SIGNATURE-----\n" +
                "\n" +
                "iHUEABYKACcFAmPBjZUJEPIxVQxPR+OOFiEE64W7X6M6deFelE5j8jFVDE9H444A\n" +
                "ADI/AQC6Bux6WpGYf7HO+QPV/D5iIrqZt9xPLgfUVoNJBmMZZwD+Ib+tn5pSyWUw\n" +
                "0K1UgT5roym9Fln8U5W8R03TSbfNiwE=\n" +
                "=bxPN\n" +
                "-----END PGP SIGNATURE-----").getBytes(StandardCharsets.UTF_8);
        Date signatureDate = UTCUtil.parseUTCDate("2023-01-13T16:57:57Z");
        Date afterSignature = new Date(signatureDate.getTime() + 1000); // 1 sec after sig

        assertThrows(SOPGPException.NoSignature.class, () -> getSop().detachedVerify()
                .cert(TestKeys.ALICE_CERT.getBytes(StandardCharsets.UTF_8))
                .notBefore(afterSignature)
                .signatures(signature)
                .data(message));
    }


    @Test
    public void signVerifyWithEncryptedKeyWithoutPassphraseFails() {
        ignoreIf("sqop", Is.leq, "0.27.2"); // does not return exit code 67 for encrypted keys without passphrase

        assertThrows(SOPGPException.KeyIsProtected.class, () ->
                getSop().detachedSign()
                        .key(TestKeys.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                        .data("Hello, World!\n".getBytes(StandardCharsets.UTF_8))
                        .toByteArrayAndResult()
                        .getBytes());
    }

    @Test
    public void signWithProtectedKeyAndMultiplePassphrasesTest()
            throws IOException {
        byte[] message = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);

        byte[] signature = getSop().sign()
                .key(TestKeys.PASSWORD_PROTECTED_KEY.getBytes(StandardCharsets.UTF_8))
                .withKeyPassword("wrong")
                .withKeyPassword(TestKeys.PASSWORD) // correct
                .withKeyPassword("wrong2")
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        assertFalse(getSop().verify()
                .cert(TestKeys.PASSWORD_PROTECTED_CERT.getBytes(StandardCharsets.UTF_8))
                .signatures(signature)
                .data(message)
                .isEmpty());
    }

}
