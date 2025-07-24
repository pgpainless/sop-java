// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.Ready
import sop.exception.SOPGPException.*
import sop.util.UTF8Util

/** Interface for creating certificate revocations. */
interface RevokeKey {

    /**
     * Disable ASCII armor encoding.
     *
     * @return builder instance
     */
    fun noArmor(): RevokeKey

    /**
     * Provide the decryption password for the secret key.
     *
     * @param password password
     * @return builder instance
     * @throws UnsupportedOption if the implementation does not support key passwords
     */
    @Throws(UnsupportedOption::class)
    fun withKeyPassword(password: CharArray): RevokeKey = withKeyPassword(password.concatToString())

    /**
     * Provide the decryption password for the secret key.
     *
     * @param password password
     * @return builder instance
     * @throws UnsupportedOption if the implementation does not support key passwords
     */
    @Throws(UnsupportedOption::class)
    fun withKeyPassword(password: String): RevokeKey =
        withKeyPassword(password.toByteArray(UTF8Util.UTF8))

    /**
     * Provide the decryption password for the secret key.
     *
     * @param password password
     * @return builder instance
     * @throws UnsupportedOption if the implementation does not support key passwords
     * @throws PasswordNotHumanReadable if the password is not human-readable
     */
    @Throws(UnsupportedOption::class, PasswordNotHumanReadable::class)
    fun withKeyPassword(password: ByteArray): RevokeKey

    /**
     * Provide the key that you want to revoke.
     *
     * @param bytes byte array containing the OpenPGP key
     * @return object to require the revocation certificate from
     * @throws BadData if the key cannot be read
     * @throws KeyIsProtected if the key is protected and cannot be unlocked
     * @throws IOException if an IO error occurs
     */
    @Throws(BadData::class, KeyIsProtected::class, IOException::class)
    fun keys(bytes: ByteArray): Ready = keys(bytes.inputStream())

    /**
     * Provide the key that you want to revoke.
     *
     * @param keys input stream containing the OpenPGP key
     * @return object to require the revocation certificate from
     * @throws BadData if the key cannot be read
     * @throws KeyIsProtected if the key is protected and cannot be unlocked
     * @throws IOException if an IO error occurs
     */
    @Throws(BadData::class, KeyIsProtected::class, IOException::class)
    fun keys(keys: InputStream): Ready
}
