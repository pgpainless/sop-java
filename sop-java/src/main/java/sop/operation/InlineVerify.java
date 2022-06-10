// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.ReadyWithResult;
import sop.Verification;
import sop.exception.SOPGPException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * API for verification of inline-signed messages.
 */
public interface InlineVerify extends AbstractVerify<InlineVerify> {

    /**
     * Provide the inline-signed data.
     * The result can be used to write the plaintext message out and to get the verifications.
     *
     * @param data signed data
     * @return list of signature verifications
     *
     * @throws IOException in case of an IO error
     * @throws SOPGPException.NoSignature when no signature is found
     * @throws SOPGPException.BadData when the data is invalid OpenPGP data
     */
    ReadyWithResult<List<Verification>> data(InputStream data)
            throws IOException,
            SOPGPException.NoSignature,
            SOPGPException.BadData;

    /**
     * Provide the inline-signed data.
     * The result can be used to write the plaintext message out and to get the verifications.
     *
     * @param data signed data
     * @return list of signature verifications
     *
     * @throws IOException in case of an IO error
     * @throws SOPGPException.NoSignature when no signature is found
     * @throws SOPGPException.BadData when the data is invalid OpenPGP data
     */
    default ReadyWithResult<List<Verification>> data(byte[] data)
            throws IOException,
            SOPGPException.NoSignature,
            SOPGPException.BadData {
        return data(new ByteArrayInputStream(data));
    }
}
