// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import java.util.*
import sop.exception.SOPGPException.BadData
import sop.exception.SOPGPException.UnsupportedOption

/**
 * Common API methods shared between verification of inline signatures ([InlineVerify]) and
 * verification of detached signatures ([DetachedVerify]).
 *
 * @param <T> Builder type ([DetachedVerify], [InlineVerify])
 */
interface AbstractVerify<T> {

    /**
     * Makes the SOP implementation consider signatures before this date invalid.
     *
     * @param timestamp timestamp
     * @return builder instance
     */
    @Throws(UnsupportedOption::class) fun notBefore(timestamp: Date): T

    /**
     * Makes the SOP implementation consider signatures after this date invalid.
     *
     * @param timestamp timestamp
     * @return builder instance
     */
    @Throws(UnsupportedOption::class) fun notAfter(timestamp: Date): T

    /**
     * Add one or more verification cert.
     *
     * @param cert input stream containing the encoded certs
     * @return builder instance
     * @throws BadData if the input stream does not contain an OpenPGP certificate
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, IOException::class) fun cert(cert: InputStream): T

    /**
     * Add one or more verification cert.
     *
     * @param cert byte array containing the encoded certs
     * @return builder instance
     * @throws BadData if the byte array does not contain an OpenPGP certificate
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, IOException::class)
    fun cert(cert: ByteArray): T = cert(cert.inputStream())
}
