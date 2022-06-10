// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.exception.SOPGPException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * API for verifying detached signatures.
 */
public interface DetachedVerify extends AbstractVerify<DetachedVerify>, VerifySignatures {

    /**
     * Provides the detached signatures.
     * @param signatures input stream containing encoded, detached signatures.
     *
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.BadData if the input stream does not contain OpenPGP signatures
     * @throws IOException in case of an IO error
     */
    VerifySignatures signatures(InputStream signatures)
            throws SOPGPException.BadData,
            IOException;

    /**
     * Provides the detached signatures.
     * @param signatures byte array containing encoded, detached signatures.
     *
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.BadData if the byte array does not contain OpenPGP signatures
     * @throws IOException in case of an IO error
     */
    default VerifySignatures signatures(byte[] signatures)
            throws SOPGPException.BadData,
            IOException {
        return signatures(new ByteArrayInputStream(signatures));
    }
}
