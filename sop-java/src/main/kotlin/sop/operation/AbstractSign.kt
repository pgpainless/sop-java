// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.exception.SOPGPException.BadData
import sop.exception.SOPGPException.KeyCannotSign
import sop.exception.SOPGPException.PasswordNotHumanReadable
import sop.exception.SOPGPException.UnsupportedAsymmetricAlgo
import sop.exception.SOPGPException.UnsupportedOption
import sop.util.UTF8Util

/**
 * Interface for signing operations.
 *
 * @param <T> builder subclass
 */
interface AbstractSign<T> {

    /**
     * Disable ASCII armor encoding.
     *
     * @return builder instance
     */
    fun noArmor(): T

    /**
     * Add one or more signing keys.
     *
     * @param key input stream containing encoded keys
     * @return builder instance
     * @throws KeyCannotSign if the key cannot be used for signing
     * @throws BadData if the [InputStream] does not contain an OpenPGP key
     * @throws UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    @Throws(
        KeyCannotSign::class, BadData::class, UnsupportedAsymmetricAlgo::class, IOException::class)
    fun key(key: InputStream): T

    /**
     * Add one or more signing keys.
     *
     * @param key byte array containing encoded keys
     * @return builder instance
     * @throws KeyCannotSign if the key cannot be used for signing
     * @throws BadData if the byte array does not contain an OpenPGP key
     * @throws UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    @Throws(
        KeyCannotSign::class, BadData::class, UnsupportedAsymmetricAlgo::class, IOException::class)
    fun key(key: ByteArray): T = key(key.inputStream())

    /**
     * Provide the password for the secret key used for signing.
     *
     * @param password password
     * @return builder instance
     * @throws UnsupportedOption if key passwords are not supported
     * @throws PasswordNotHumanReadable if the provided passphrase is not human-readable
     */
    @Throws(UnsupportedOption::class, PasswordNotHumanReadable::class)
    fun withKeyPassword(password: String): T = withKeyPassword(password.toByteArray(UTF8Util.UTF8))

    /**
     * Provide the password for the secret key used for signing.
     *
     * @param password password
     * @return builder instance
     * @throws UnsupportedOption if key passwords are not supported
     * @throws PasswordNotHumanReadable if the provided passphrase is not human-readable
     */
    @Throws(UnsupportedOption::class, PasswordNotHumanReadable::class)
    fun withKeyPassword(password: ByteArray): T
}
