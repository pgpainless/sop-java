// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.Ready
import sop.exception.SOPGPException.*

/** Interface for merging multiple copies of the same certificate into one. */
interface MergeCerts {

    /**
     * Disable ASCII armor for the output certificate.
     *
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class) fun noArmor(): MergeCerts

    /**
     * Provide updated copies of the base certificate.
     *
     * @param updateCerts input stream containing an updated copy of the base cert
     * @return builder instance
     * @throws BadData if the update cannot be read
     * @throws IOException if an IO error occurs
     */
    @Throws(BadData::class, IOException::class) fun updates(updateCerts: InputStream): MergeCerts

    /**
     * Provide updated copies of the base certificate.
     *
     * @param updateCerts byte array containing an updated copy of the base cert
     * @return builder instance
     * @throws BadData if the update cannot be read
     * @throws IOException if an IO error occurs
     */
    @Throws(BadData::class, IOException::class)
    fun updates(updateCerts: ByteArray): MergeCerts = updates(updateCerts.inputStream())

    /**
     * Provide the base certificate into which updates shall be merged.
     *
     * @param certs input stream containing the base OpenPGP certificate
     * @return object to require the merged certificate from
     * @throws BadData if the base certificate cannot be read
     * @throws IOException if an IO error occurs
     */
    @Throws(BadData::class, IOException::class) fun baseCertificates(certs: InputStream): Ready

    /**
     * Provide the base certificate into which updates shall be merged.
     *
     * @param certs byte array containing the base OpenPGP certificate
     * @return object to require the merged certificate from
     * @throws BadData if the base certificate cannot be read
     * @throws IOException if an IO error occurs
     */
    @Throws(BadData::class, IOException::class)
    fun baseCertificates(certs: ByteArray): Ready = baseCertificates(certs.inputStream())
}
