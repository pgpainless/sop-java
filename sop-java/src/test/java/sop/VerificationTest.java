// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import org.junit.jupiter.api.Test;
import sop.enums.SignatureMode;
import sop.testsuite.assertions.VerificationAssert;
import sop.util.UTCUtil;

import java.text.ParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VerificationTest {

    @Test
    public void limitedConstructorTest() throws ParseException {
        Date signDate = UTCUtil.parseUTCDate("2022-11-07T15:01:24Z");
        String keyFP = "F9E6F53F7201C60A87064EAB0B27F2B0760A1209";
        String certFP = "4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B";
        Verification verification = new Verification(signDate, keyFP, certFP);
        assertEquals("2022-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B", verification.toString());

        assertFalse(verification.getContainsJson());

        VerificationAssert.assertThatVerification(verification)
                .issuedBy(certFP)
                .isBySigningKey(keyFP)
                .isCreatedAt(signDate)
                .hasMode(null)
                .hasDescription(null);
    }

    @Test
    public void limitedParsingTest() throws ParseException {
        String string = "2022-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B";
        Verification verification = Verification.fromString(string);
        assertEquals(string, verification.toString());
        VerificationAssert.assertThatVerification(verification)
                .isCreatedAt(UTCUtil.parseUTCDate("2022-11-07T15:01:24Z"))
                .issuedBy("F9E6F53F7201C60A87064EAB0B27F2B0760A1209", "4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B")
                .hasMode(null)
                .hasDescription(null);
    }

    @Test
    public void parsingWithModeTest() throws ParseException {
        String string = "2022-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B mode:text";
        Verification verification = Verification.fromString(string);
        assertEquals(string, verification.toString());
        VerificationAssert.assertThatVerification(verification)
                .isCreatedAt(UTCUtil.parseUTCDate("2022-11-07T15:01:24Z"))
                .issuedBy("F9E6F53F7201C60A87064EAB0B27F2B0760A1209", "4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B")
                .hasMode(SignatureMode.text)
                .hasDescription(null);
    }

    @Test
    public void extendedConstructorTest() throws ParseException {
        Date signDate = UTCUtil.parseUTCDate("2022-11-07T15:01:24Z");
        String keyFP = "F9E6F53F7201C60A87064EAB0B27F2B0760A1209";
        String certFP = "4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B";
        SignatureMode mode = SignatureMode.binary;
        String description = "certificate from dkg.asc";
        Verification verification = new Verification(signDate, keyFP, certFP, mode, description);

        assertEquals("2022-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B mode:binary certificate from dkg.asc", verification.toString());
        VerificationAssert.assertThatVerification(verification)
                .isCreatedAt(signDate)
                .issuedBy(keyFP, certFP)
                .hasMode(SignatureMode.binary)
                .hasDescription(description);
    }

    @Test
    public void extendedParsingTest() throws ParseException {
        String string = "2022-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B mode:binary certificate from dkg.asc";
        Verification verification = Verification.fromString(string);
        assertEquals(string, verification.toString());

        // no mode
        string = "2022-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B certificate from dkg.asc";
        verification = Verification.fromString(string);
        assertEquals(string, verification.toString());
        VerificationAssert.assertThatVerification(verification)
                .isCreatedAt(UTCUtil.parseUTCDate("2022-11-07T15:01:24Z"))
                .issuedBy("F9E6F53F7201C60A87064EAB0B27F2B0760A1209", "4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B")
                .hasMode(null)
                .hasDescription("certificate from dkg.asc");
    }

    @Test
    public void missingFingerprintFails() {
        String string = "2022-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209";
        assertThrows(IllegalArgumentException.class, () -> Verification.fromString(string));
    }

    @Test
    public void malformedTimestampFails() {
        String shorter = "'99-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B";
        assertThrows(IllegalArgumentException.class, () -> Verification.fromString(shorter));

        String longer = "'99-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B mode:binary certificate from dkg.asc";
        assertThrows(IllegalArgumentException.class, () -> Verification.fromString(longer));

    }
}
