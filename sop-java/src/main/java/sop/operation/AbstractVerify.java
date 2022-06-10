// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.exception.SOPGPException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Common API methods shared between verification of inline signatures ({@link InlineVerify})
 * and verification of detached signatures ({@link DetachedVerify}).
 *
 * @param <T> Builder type ({@link DetachedVerify}, {@link InlineVerify})
 */
public interface AbstractVerify<T> {

    /**
     * Makes the SOP implementation consider signatures before this date invalid.
     *
     * @param timestamp timestamp
     * @return builder instance
     */
    T notBefore(Date timestamp)
            throws SOPGPException.UnsupportedOption;

    /**
     * Makes the SOP implementation consider signatures after this date invalid.
     *
     * @param timestamp timestamp
     * @return builder instance
     */
    T notAfter(Date timestamp)
            throws SOPGPException.UnsupportedOption;

    /**
     * Add one or more verification cert.
     *
     * @param cert input stream containing the encoded certs
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.BadData if the input stream does not contain an OpenPGP certificate
     * @throws IOException in case of an IO error
     */
    T cert(InputStream cert)
            throws SOPGPException.BadData,
            IOException;

    /**
     * Add one or more verification cert.
     *
     * @param cert byte array containing the encoded certs
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.BadData if the byte array does not contain an OpenPGP certificate
     * @throws IOException in case of an IO error
     */
    default T cert(byte[] cert)
            throws SOPGPException.BadData,
            IOException {
        return cert(new ByteArrayInputStream(cert));
    }

}
