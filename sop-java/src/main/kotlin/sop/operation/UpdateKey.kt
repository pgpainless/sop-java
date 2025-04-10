// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.Ready
import sop.exception.SOPGPException
import sop.util.UTF8Util

interface UpdateKey {

    /**
     * Disable ASCII armor encoding of the output.
     *
     * @return builder instance
     */
    fun noArmor(): UpdateKey

    /**
     * Allow key to be used for signing only.
     * If this option is not present, the operation may add a new, encryption-capable component key.
     */
    @Throws(SOPGPException.UnsupportedOption::class) fun signingOnly(): UpdateKey

    /**
     * Do not allow adding new capabilities to the key.
     * If this option is not present, the operation may add support for new capabilities to the key.
     */
    @Throws(SOPGPException.UnsupportedOption::class) fun noAddedCapabilities(): UpdateKey

    /**
     * Provide a passphrase for unlocking the secret key.
     *
     * @param password password
     */
    @Throws(SOPGPException.PasswordNotHumanReadable::class, SOPGPException.UnsupportedOption::class)
    fun withKeyPassword(password: String): UpdateKey =
        withKeyPassword(password.toByteArray(UTF8Util.UTF8))

    /**
     * Provide a passphrase for unlocking the secret key.
     *
     * @param password password
     */
    @Throws(SOPGPException.PasswordNotHumanReadable::class, SOPGPException.UnsupportedOption::class)
    fun withKeyPassword(password: ByteArray): UpdateKey

    /**
     * Provide certificates that might contain updated signatures or third-party certifications.
     * These certificates will be merged into the key.
     *
     * @param certs input stream of certificates
     */
    @Throws(
        SOPGPException.UnsupportedOption::class, SOPGPException.BadData::class, IOException::class)
    fun mergeCerts(certs: InputStream): UpdateKey

    /**
     * Provide certificates that might contain updated signatures or third-party certifications.
     * These certificates will be merged into the key.
     *
     * @param certs binary certificates
     */
    @Throws(
        SOPGPException.UnsupportedOption::class, SOPGPException.BadData::class, IOException::class)
    fun mergeCerts(certs: ByteArray): UpdateKey = mergeCerts(certs.inputStream())

    /**
     * Provide the OpenPGP key to update.
     *
     * @param key input stream containing the key
     * @return handle to acquire the updated OpenPGP key from
     */
    @Throws(
        SOPGPException.BadData::class,
        IOException::class,
        SOPGPException.KeyIsProtected::class,
        SOPGPException.PrimaryKeyBad::class)
    fun key(key: InputStream): Ready

    /**
     * Provide the OpenPGP key to update.
     *
     * @param key binary OpenPGP key
     * @return handle to acquire the updated OpenPGP key from
     */
    @Throws(
        SOPGPException.BadData::class,
        IOException::class,
        SOPGPException.KeyIsProtected::class,
        SOPGPException.PrimaryKeyBad::class)
    fun key(key: ByteArray): Ready = key(key.inputStream())
}
