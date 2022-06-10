// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import sop.ReadyWithResult;
import sop.Signatures;
import sop.exception.SOPGPException;

/**
 * Split cleartext signed messages up into data and signatures.
 */
public interface InlineDetach {

    /**
     * Do not wrap the signatures in ASCII armor.
     * @return builder
     */
    InlineDetach noArmor();

    /**
     * Detach the provided signed message from its signatures.
     *
     * @param messageInputStream input stream containing the signed message
     * @return result containing the detached message
     *
     * @throws IOException in case of an IO error
     * @throws sop.exception.SOPGPException.BadData if the input stream does not contain a signed message
     */
    ReadyWithResult<Signatures> message(InputStream messageInputStream)
            throws IOException,
            SOPGPException.BadData;

    /**
     * Detach the provided cleartext signed message from its signatures.
     *
     * @param message byte array containing the signed message
     * @return result containing the detached message
     * @throws IOException in case of an IO error
     * @throws sop.exception.SOPGPException.BadData if the byte array does not contain a signed message
     */
    default ReadyWithResult<Signatures> message(byte[] message)
            throws IOException,
            SOPGPException.BadData {
        return message(new ByteArrayInputStream(message));
    }
}
