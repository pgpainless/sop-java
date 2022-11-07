// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import java.util.Date;

import sop.util.UTCUtil;

public class Verification {

    private final Date creationTime;
    private final String signingKeyFingerprint;
    private final String signingCertFingerprint;

    public Verification(Date creationTime, String signingKeyFingerprint, String signingCertFingerprint) {
        this.creationTime = creationTime;
        this.signingKeyFingerprint = signingKeyFingerprint;
        this.signingCertFingerprint = signingCertFingerprint;
    }

    public static Verification fromString(String toString) {
        String[] split = toString.trim().split(" ");
        if (split.length != 3) {
            throw new IllegalArgumentException("Verification must be of the format 'UTC-DATE OpenPGPFingerprint OpenPGPFingerprint'");
        }

        return new Verification(UTCUtil.parseUTCDate(split[0]), split[1], split[2]);
    }

    /**
     * Return the signatures' creation time.
     *
     * @return signature creation time
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * Return the fingerprint of the signing (sub)key.
     *
     * @return signing key fingerprint
     */
    public String getSigningKeyFingerprint() {
        return signingKeyFingerprint;
    }

    /**
     * Return the fingerprint fo the signing certificate.
     *
     * @return signing certificate fingerprint
     */
    public String getSigningCertFingerprint() {
        return signingCertFingerprint;
    }

    @Override
    public String toString() {
        return UTCUtil.formatUTCDate(getCreationTime()) +
                ' ' +
                getSigningKeyFingerprint() +
                ' ' +
                getSigningCertFingerprint();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Verification)) {
            return false;
        }
        Verification other = (Verification) obj;
        return toString().equals(other.toString());
    }
}
