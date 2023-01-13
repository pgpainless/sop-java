// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import sop.Verification;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
    public void signVerifyWithFreshEncryptedKey() throws IOException {
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

        byte[] signature = getSop().detachedSign()
                .key(key)
                .withKeyPassword(keyPassword)
                .data(message)
                .toByteArrayAndResult()
                .getBytes();

        assertArrayStartsWith(signature, BEGIN_PGP_SIGNATURE_BYTES);

        List<Verification> verificationList = getSop().detachedVerify()
                .cert(cert)
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
}
