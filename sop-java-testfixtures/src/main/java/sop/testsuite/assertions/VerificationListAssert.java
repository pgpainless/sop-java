// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.assertions;

import sop.Verification;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public final class VerificationListAssert {

    private final List<Verification> verificationList = new ArrayList<>();

    private VerificationListAssert(List<Verification> verifications) {
        this.verificationList.addAll(verifications);
    }

    public static VerificationListAssert assertThatVerificationList(List<Verification> verifications) {
        return new VerificationListAssert(verifications);
    }

    public VerificationListAssert isEmpty() {
        assertTrue(verificationList.isEmpty());
        return this;
    }

    public VerificationListAssert isNotEmpty() {
        assertFalse(verificationList.isEmpty());
        return this;
    }

    public VerificationListAssert sizeEquals(int size) {
        assertEquals(size, verificationList.size());
        return this;
    }

    public VerificationAssert hasSingleItem() {
        sizeEquals(1);
        return VerificationAssert.assertThatVerification(verificationList.get(0));
    }

    public VerificationListAssert containsVerificationByCert(String primaryFingerprint) {
        for (Verification verification : verificationList) {
            if (primaryFingerprint.equals(verification.getSigningCertFingerprint())) {
                return this;
            }
        }
        fail("No verification was issued by certificate " + primaryFingerprint);
        return this;
    }

    public VerificationListAssert containsVerificationBy(String signingKeyFingerprint, String primaryFingerprint) {
        for (Verification verification : verificationList) {
            if (primaryFingerprint.equals(verification.getSigningCertFingerprint()) &&
                    signingKeyFingerprint.equals(verification.getSigningKeyFingerprint())) {
                return this;
            }
        }

        fail("No verification was issued by key " + signingKeyFingerprint + " of cert " + primaryFingerprint);
        return this;
    }
}
