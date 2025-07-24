// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.Ready
import sop.exception.SOPGPException.*
import sop.util.UTF8Util

/** Interface for bringing an OpenPGP key up to date. */
interface UpdateKey {

    /**
     * Disable ASCII armor encoding of the output.
     *
     * @return builder instance
     */
    fun noArmor(): UpdateKey

    /**
     * Allow key to be used for signing only. If this option is not present, the operation may add a
     * new, encryption-capable component key.
     *
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class) fun signingOnly(): UpdateKey

    /**
     * Do not allow adding new capabilities to the key. If this option is not present, the operation
     * may add support for new capabilities to the key.
     *
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class) fun noAddedCapabilities(): UpdateKey

    /**
     * Provide a passphrase for unlocking the secret key.
     *
     * @param password password
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class)
    fun withKeyPassword(password: CharArray): UpdateKey = withKeyPassword(password.concatToString())

    /**
     * Provide a passphrase for unlocking the secret key.
     *
     * @param password password
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class)
    fun withKeyPassword(password: String): UpdateKey =
        withKeyPassword(password.toByteArray(UTF8Util.UTF8))

    /**
     * Provide a passphrase for unlocking the secret key.
     *
     * @param password password
     * @return builder instance
     * @throws PasswordNotHumanReadable if the password is not human-readable
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(PasswordNotHumanReadable::class, UnsupportedOption::class)
    fun withKeyPassword(password: ByteArray): UpdateKey

    /**
     * Provide certificates that might contain updated signatures or third-party certifications.
     * These certificates will be merged into the key.
     *
     * @param certs input stream of certificates
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     * @throws BadData if the certificate cannot be read
     * @throws IOException if an IO error occurs
     */
    @Throws(UnsupportedOption::class, BadData::class, IOException::class)
    fun mergeCerts(certs: InputStream): UpdateKey

    /**
     * Provide certificates that might contain updated signatures or third-party certifications.
     * These certificates will be merged into the key.
     *
     * @param certs binary certificates
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     * @throws BadData if the certificate cannot be read
     * @throws IOException if an IO error occurs
     */
    @Throws(UnsupportedOption::class, BadData::class, IOException::class)
    fun mergeCerts(certs: ByteArray): UpdateKey = mergeCerts(certs.inputStream())

    /**
     * Provide the OpenPGP key to update.
     *
     * @param key input stream containing the key
     * @return handle to acquire the updated OpenPGP key from
     * @throws BadData if the key cannot be read
     * @throws IOException if an IO error occurs
     * @throws KeyIsProtected if the key is passphrase protected and cannot be unlocked
     * @throws PrimaryKeyBad if the primary key is bad (e.g. expired, too weak)
     */
    @Throws(BadData::class, IOException::class, KeyIsProtected::class, PrimaryKeyBad::class)
    fun key(key: InputStream): Ready

    /**
     * Provide the OpenPGP key to update.
     *
     * @param key binary OpenPGP key
     * @return handle to acquire the updated OpenPGP key from
     * @throws BadData if the key cannot be read
     * @throws IOException if an IO error occurs
     * @throws KeyIsProtected if the key is passphrase protected and cannot be unlocked
     * @throws PrimaryKeyBad if the primary key is bad (e.g. expired, too weak)
     */
    @Throws(BadData::class, IOException::class, KeyIsProtected::class, PrimaryKeyBad::class)
    fun key(key: ByteArray): Ready = key(key.inputStream())
}
