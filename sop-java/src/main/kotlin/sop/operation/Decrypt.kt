// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import java.util.*
import sop.DecryptionResult
import sop.ReadyWithResult
import sop.SessionKey
import sop.exception.SOPGPException.*
import sop.util.UTF8Util

/** Interface for decrypting encrypted OpenPGP messages. */
interface Decrypt {

    /**
     * Makes the SOP consider signatures before this date invalid.
     *
     * @param timestamp timestamp
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class) fun verifyNotBefore(timestamp: Date): Decrypt

    /**
     * Makes the SOP consider signatures after this date invalid.
     *
     * @param timestamp timestamp
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class) fun verifyNotAfter(timestamp: Date): Decrypt

    /**
     * Adds one or more verification cert.
     *
     * @param cert input stream containing the cert(s)
     * @return builder instance
     * @throws BadData if the [InputStream] doesn't provide an OpenPGP certificate
     * @throws UnsupportedAsymmetricAlgo if the cert uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, UnsupportedAsymmetricAlgo::class, IOException::class)
    fun verifyWithCert(cert: InputStream): Decrypt

    /**
     * Adds one or more verification cert.
     *
     * @param cert byte array containing the cert(s)
     * @return builder instance
     * @throws BadData if the byte array doesn't contain an OpenPGP certificate
     * @throws UnsupportedAsymmetricAlgo if the cert uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, UnsupportedAsymmetricAlgo::class, IOException::class)
    fun verifyWithCert(cert: ByteArray): Decrypt = verifyWithCert(cert.inputStream())

    /**
     * Tries to decrypt with the given session key.
     *
     * @param sessionKey session key
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class) fun withSessionKey(sessionKey: SessionKey): Decrypt

    /**
     * Tries to decrypt with the given password.
     *
     * @param password password
     * @return builder instance
     * @throws PasswordNotHumanReadable if the password is not human-readable
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(PasswordNotHumanReadable::class, UnsupportedOption::class)
    fun withPassword(password: String): Decrypt

    /**
     * Adds one or more decryption key.
     *
     * @param key input stream containing the key(s)
     * @return builder instance
     * @throws BadData if the [InputStream] does not provide an OpenPGP key
     * @throws UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, UnsupportedAsymmetricAlgo::class, IOException::class)
    fun withKey(key: InputStream): Decrypt

    /**
     * Adds one or more decryption key.
     *
     * @param key byte array containing the key(s)
     * @return builder instance
     * @throws BadData if the byte array does not contain an OpenPGP key
     * @throws UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, UnsupportedAsymmetricAlgo::class, IOException::class)
    fun withKey(key: ByteArray): Decrypt = withKey(key.inputStream())

    /**
     * Provide the decryption password for the secret key.
     *
     * @param password password
     * @return builder instance
     * @throws UnsupportedOption if the implementation does not support key passwords
     * @throws PasswordNotHumanReadable if the password is not human-readable
     */
    @Throws(UnsupportedOption::class, PasswordNotHumanReadable::class)
    fun withKeyPassword(password: String): Decrypt =
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
    fun withKeyPassword(password: ByteArray): Decrypt

    /**
     * Decrypts the given ciphertext, returning verification results and plaintext.
     *
     * @param ciphertext ciphertext
     * @return ready with result
     * @throws BadData if the [InputStream] does not provide an OpenPGP message
     * @throws MissingArg if an argument required for decryption was not provided
     * @throws CannotDecrypt in case decryption fails for some reason
     * @throws KeyIsProtected if the decryption key cannot be unlocked (e.g. missing passphrase)
     * @throws IOException in case of an IO error
     */
    @Throws(
        BadData::class,
        MissingArg::class,
        CannotDecrypt::class,
        KeyIsProtected::class,
        IOException::class)
    fun ciphertext(ciphertext: InputStream): ReadyWithResult<DecryptionResult>

    /**
     * Decrypts the given ciphertext, returning verification results and plaintext.
     *
     * @param ciphertext ciphertext
     * @return ready with result
     * @throws BadData if the byte array does not contain an encrypted OpenPGP message
     * @throws MissingArg in case of missing decryption method (password or key required)
     * @throws CannotDecrypt in case decryption fails for some reason
     * @throws KeyIsProtected if the decryption key cannot be unlocked (e.g. missing passphrase)
     * @throws IOException in case of an IO error
     */
    @Throws(
        BadData::class,
        MissingArg::class,
        CannotDecrypt::class,
        KeyIsProtected::class,
        IOException::class)
    fun ciphertext(ciphertext: ByteArray): ReadyWithResult<DecryptionResult> =
        ciphertext(ciphertext.inputStream())
}
