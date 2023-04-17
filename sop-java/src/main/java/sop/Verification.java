// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import java.util.Date;

import sop.enums.SignatureMode;
import sop.util.UTCUtil;

/**
 * Class bundling information about a verified signature.
 */
public class Verification {

    private final Date creationTime;
    private final String signingKeyFingerprint;
    private final String signingCertFingerprint;
    private final SignatureMode signatureMode;
    private final String description;

    private static final String MODE = "mode:";

    /**
     * Create a new {@link Verification} without mode and description.
     *
     * @param creationTime signature creation time
     * @param signingKeyFingerprint fingerprint of the signing (sub-) key
     * @param signingCertFingerprint fingerprint of the certificate
     */
    public Verification(Date creationTime, String signingKeyFingerprint, String signingCertFingerprint) {
        this(creationTime, signingKeyFingerprint, signingCertFingerprint, null, null);
    }

    /**
     * Create a new {@link Verification}.
     *
     * @param creationTime signature creation time
     * @param signingKeyFingerprint fingerprint of the signing (sub-) key
     * @param signingCertFingerprint fingerprint of the certificate
     * @param signatureMode signature mode (optional, may be <pre>null</pre>)
     * @param description free-form description, e.g. <pre>certificate from dkg.asc</pre> (optional, may be <pre>null</pre>)
     */
    public Verification(Date creationTime, String signingKeyFingerprint, String signingCertFingerprint, SignatureMode signatureMode, String description) {
        this.creationTime = creationTime;
        this.signingKeyFingerprint = signingKeyFingerprint;
        this.signingCertFingerprint = signingCertFingerprint;
        this.signatureMode = signatureMode;
        this.description = description == null ? null : description.trim();
    }

    public static Verification fromString(String toString) {
        String[] split = toString.trim().split(" ");
        if (split.length < 3) {
            throw new IllegalArgumentException("Verification must be of the format 'UTC-DATE OpenPGPFingerprint OpenPGPFingerprint [mode] [info]'");
        }

        if (split.length == 3) {
            return new Verification(
                    UTCUtil.parseUTCDate(split[0]), // timestamp
                    split[1],                       // key FP
                    split[2]                        // cert FP
            );
        }

        SignatureMode mode = null;
        int index = 3;
        if (split[index].startsWith(MODE)) {
            mode = SignatureMode.valueOf(split[3].substring(MODE.length()));
            index++;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = index; i < split.length; i++) {
            if (sb.length() != 0) {
                sb.append(' ');
            }
            sb.append(split[i]);
        }

        return new Verification(
                UTCUtil.parseUTCDate(split[0]),         // timestamp
                split[1],                               // key FP
                split[2],                               // cert FP
                mode,                                   // signature mode
                sb.length() != 0 ? sb.toString() : null // description
        );
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

    /**
     * Return the mode of the signature.
     * Optional, may return <pre>null</pre>.
     *
     * @return signature mode
     */
    public SignatureMode getSignatureMode() {
        return signatureMode;
    }

    /**
     * Return an optional description.
     * Optional, may return <pre>null</pre>.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(UTCUtil.formatUTCDate(getCreationTime()))
                .append(' ')
                .append(getSigningKeyFingerprint())
                .append(' ')
                .append(getSigningCertFingerprint());

        if (signatureMode != null) {
            sb.append(' ').append(MODE).append(signatureMode);
        }

        if (description != null) {
            sb.append(' ').append(description);
        }

        return sb.toString();
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
