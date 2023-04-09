// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.util;

import org.junit.jupiter.api.Test;
import sop.Verification;
import sop.enums.SignatureMode;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VerificationTest {

    @Test
    public void limitedConstructorTest() {
        Date signDate = UTCUtil.parseUTCDate("2022-11-07T15:01:24Z");
        String keyFP = "F9E6F53F7201C60A87064EAB0B27F2B0760A1209";
        String certFP = "4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B";
        Verification verification = new Verification(signDate, keyFP, certFP);
        assertEquals("2022-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B", verification.toString());
    }

    public void limitedParsingTest() {
        String string = "2022-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B";
        Verification verification = Verification.fromString(string);
        assertEquals(string, verification.toString());
    }

    @Test
    public void extendedConstructorTest() {
        Date signDate = UTCUtil.parseUTCDate("2022-11-07T15:01:24Z");
        String keyFP = "F9E6F53F7201C60A87064EAB0B27F2B0760A1209";
        String certFP = "4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B";
        SignatureMode mode = SignatureMode.binary;
        String description = "certificate from dkg.asc";
        Verification verification = new Verification(signDate, keyFP, certFP, mode, description);
        assertEquals("2022-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B mode:binary certificate from dkg.asc", verification.toString());
        assertEquals(SignatureMode.binary, verification.getSignatureMode());
        assertEquals(description, verification.getDescription());
    }

    @Test
    public void extendedParsingTest() {
        String string = "2022-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B mode:binary certificate from dkg.asc";
        Verification verification = Verification.fromString(string);
        assertEquals(string, verification.toString());

        // no mode
        string = "2022-11-07T15:01:24Z F9E6F53F7201C60A87064EAB0B27F2B0760A1209 4E2C78519512C2AE9A8BFE7EB3298EB2FBE5F51B certificate from dkg.asc";
        verification = Verification.fromString(string);
        assertEquals(string, verification.toString());
    }
}
