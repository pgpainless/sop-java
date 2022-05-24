// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.exception.SOPGPException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * API for verifying detached signatures.
 */
public interface Verify extends AbstractVerify<Verify>, VerifySignatures {

    /**
     * Provides the detached signatures.
     * @param signatures input stream containing encoded, detached signatures.
     *
     * @return builder instance
     */
    VerifySignatures signatures(InputStream signatures) throws SOPGPException.BadData;

    /**
     * Provides the detached signatures.
     * @param signatures byte array containing encoded, detached signatures.
     *
     * @return builder instance
     */
    default VerifySignatures signatures(byte[] signatures) throws SOPGPException.BadData {
        return signatures(new ByteArrayInputStream(signatures));
    }
}
