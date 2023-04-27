// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.assertions;

import sop.Verification;
import sop.enums.SignatureMode;
import sop.testsuite.JUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class VerificationAssert {

    private final Verification verification;

    public static VerificationAssert assertThatVerification(Verification verification) {
        return new VerificationAssert(verification);
    }

    private VerificationAssert(Verification verification) {
        this.verification = verification;
    }

    public VerificationAssert issuedBy(String signingKeyFingerprint, String primaryFingerprint) {
        return isBySigningKey(signingKeyFingerprint)
                .issuedBy(primaryFingerprint);
    }

    public VerificationAssert issuedBy(String primaryFingerprint) {
        assertEquals(primaryFingerprint, verification.getSigningCertFingerprint());
        return this;
    }

    public VerificationAssert isBySigningKey(String signingKeyFingerprint) {
        assertEquals(signingKeyFingerprint, verification.getSigningKeyFingerprint());
        return this;
    }

    public VerificationAssert isCreatedAt(Date creationDate) {
        JUtils.assertDateEquals(creationDate, verification.getCreationTime());
        return this;
    }

    public VerificationAssert hasDescription(String description) {
        assertEquals(description, verification.getDescription().get());
        return this;
    }

    public VerificationAssert hasDescriptionOrNull(String description) {
        if (verification.getDescription().isEmpty()) {
            return this;
        }

        return hasDescription(description);
    }

    public VerificationAssert hasMode(SignatureMode mode) {
        assertEquals(mode, verification.getSignatureMode().get());
        return this;
    }

    public VerificationAssert hasModeOrNull(SignatureMode mode) {
        if (verification.getSignatureMode().isEmpty()) {
            return this;
        }
        return hasMode(mode);
    }
}
