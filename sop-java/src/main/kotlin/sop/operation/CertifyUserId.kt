// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.Ready
import sop.exception.SOPGPException.*
import sop.util.UTF8Util

/** Interface for issuing certifications over UserIDs on certificates. */
interface CertifyUserId {

    /** Disable ASCII armor for the output. */
    @Throws(UnsupportedOption::class) fun noArmor(): CertifyUserId

    /**
     * Add a user-id that shall be certified on the certificates.
     *
     * @param userId user-id
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class) fun userId(userId: String): CertifyUserId

    /**
     * Provide the password for the secret key used for signing.
     *
     * @param password password
     * @return builder instance
     * @throws UnsupportedOption if key passwords are not supported
     */
    @Throws(UnsupportedOption::class)
    fun withKeyPassword(password: CharArray): CertifyUserId =
        withKeyPassword(password.concatToString())

    /**
     * Provide the password for the secret key used for signing.
     *
     * @param password password
     * @return builder instance
     * @throws UnsupportedOption if key passwords are not supported
     */
    @Throws(UnsupportedOption::class)
    fun withKeyPassword(password: String): CertifyUserId =
        withKeyPassword(password.toByteArray(UTF8Util.UTF8))

    /**
     * Provide the password for the secret key used for signing.
     *
     * @param password password
     * @return builder instance
     * @throws UnsupportedOption if key passwords are not supported
     * @throws PasswordNotHumanReadable if the provided password is not human-readable
     */
    @Throws(PasswordNotHumanReadable::class, UnsupportedOption::class)
    fun withKeyPassword(password: ByteArray): CertifyUserId

    /**
     * If this option is provided, it is possible to certify user-ids on certificates, which do not
     * have a self-certification for the user-id. You can use this option to add pet-name
     * certifications to certificates, e.g. "Mom".
     *
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class) fun noRequireSelfSig(): CertifyUserId

    /**
     * Provide signing keys for issuing the certifications.
     *
     * @param keys input stream containing one or more signing key
     * @return builder instance
     * @throws BadData if the keys cannot be read
     * @throws IOException if an IO error occurs
     */
    @Throws(BadData::class, IOException::class) fun keys(keys: InputStream): CertifyUserId

    /**
     * Provide signing keys for issuing the certifications.
     *
     * @param keys byte array containing one or more signing key
     * @return builder instance
     * @throws BadData if the keys cannot be read
     * @throws IOException if an IO error occurs
     */
    @Throws(BadData::class, IOException::class)
    fun keys(keys: ByteArray): CertifyUserId = keys(keys.inputStream())

    /**
     * Provide the certificates that you want to create certifications for.
     *
     * @param certs input stream containing the certificates
     * @return object to require the certified certificates from
     * @throws BadData if the certificates cannot be read
     * @throws IOException if an IO error occurs
     * @throws KeyIsProtected if one or more signing keys are passphrase protected and cannot be
     *   unlocked
     */
    @Throws(BadData::class, IOException::class, CertUserIdNoMatch::class, KeyIsProtected::class)
    fun certs(certs: InputStream): Ready

    /**
     * Provide the certificates that you want to create certifications for.
     *
     * @param certs byte array containing the certificates
     * @return object to require the certified certificates from
     * @throws BadData if the certificates cannot be read
     * @throws IOException if an IO error occurs
     * @throws KeyIsProtected if one or more signing keys are passphrase protected and cannot be
     *   unlocked
     */
    @Throws(BadData::class, IOException::class, CertUserIdNoMatch::class, KeyIsProtected::class)
    fun certs(certs: ByteArray): Ready = certs(certs.inputStream())
}
