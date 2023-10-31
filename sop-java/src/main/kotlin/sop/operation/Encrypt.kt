// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.Profile
import sop.Ready
import sop.enums.EncryptAs
import sop.exception.SOPGPException.*
import sop.util.UTF8Util

interface Encrypt {

    /**
     * Disable ASCII armor encoding.
     *
     * @return builder instance
     */
    fun noArmor(): Encrypt

    /**
     * Sets encryption mode.
     *
     * @param mode mode
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class) fun mode(mode: EncryptAs): Encrypt

    /**
     * Adds the signer key.
     *
     * @param key input stream containing the encoded signer key
     * @return builder instance
     * @throws KeyCannotSign if the key cannot be used for signing
     * @throws UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     * @throws BadData if the [InputStream] does not contain an OpenPGP key
     * @throws IOException in case of an IO error
     */
    @Throws(
        KeyCannotSign::class, UnsupportedAsymmetricAlgo::class, BadData::class, IOException::class)
    fun signWith(key: InputStream): Encrypt

    /**
     * Adds the signer key.
     *
     * @param key byte array containing the encoded signer key
     * @return builder instance
     * @throws KeyCannotSign if the key cannot be used for signing
     * @throws UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     * @throws BadData if the byte array does not contain an OpenPGP key
     * @throws IOException in case of an IO error
     */
    @Throws(
        KeyCannotSign::class, UnsupportedAsymmetricAlgo::class, BadData::class, IOException::class)
    fun signWith(key: ByteArray): Encrypt = signWith(key.inputStream())

    /**
     * Provide the password for the secret key used for signing.
     *
     * @param password password
     * @return builder instance
     * @throws PasswordNotHumanReadable if the password is not human-readable
     * @throws UnsupportedOption if key password are not supported
     */
    @Throws(PasswordNotHumanReadable::class, UnsupportedOption::class)
    fun withKeyPassword(password: String): Encrypt =
        withKeyPassword(password.toByteArray(UTF8Util.UTF8))

    /**
     * Provide the password for the secret key used for signing.
     *
     * @param password password
     * @return builder instance
     * @throws PasswordNotHumanReadable if the password is not human-readable
     * @throws UnsupportedOption if key password are not supported
     */
    @Throws(PasswordNotHumanReadable::class, UnsupportedOption::class)
    fun withKeyPassword(password: ByteArray): Encrypt

    /**
     * Encrypt with the given password.
     *
     * @param password password
     * @return builder instance
     * @throws PasswordNotHumanReadable if the password is not human-readable
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(PasswordNotHumanReadable::class, UnsupportedOption::class)
    fun withPassword(password: String): Encrypt

    /**
     * Encrypt with the given cert.
     *
     * @param cert input stream containing the encoded cert.
     * @return builder instance
     * @throws CertCannotEncrypt if the certificate is not encryption capable
     * @throws UnsupportedAsymmetricAlgo if the certificate uses an unsupported asymmetric algorithm
     * @throws BadData if the [InputStream] does not contain an OpenPGP certificate
     * @throws IOException in case of an IO error
     */
    @Throws(
        CertCannotEncrypt::class,
        UnsupportedAsymmetricAlgo::class,
        BadData::class,
        IOException::class)
    fun withCert(cert: InputStream): Encrypt

    /**
     * Encrypt with the given cert.
     *
     * @param cert byte array containing the encoded cert.
     * @return builder instance
     * @throws CertCannotEncrypt if the certificate is not encryption capable
     * @throws UnsupportedAsymmetricAlgo if the certificate uses an unsupported asymmetric algorithm
     * @throws BadData if the byte array does not contain an OpenPGP certificate
     * @throws IOException in case of an IO error
     */
    @Throws(
        CertCannotEncrypt::class,
        UnsupportedAsymmetricAlgo::class,
        BadData::class,
        IOException::class)
    fun withCert(cert: ByteArray): Encrypt = withCert(cert.inputStream())

    /**
     * Pass in a profile.
     *
     * @param profile profile
     * @return builder instance
     */
    fun profile(profile: Profile): Encrypt = profile(profile.name)

    /**
     * Pass in a profile identifier.
     *
     * @param profileName profile identifier
     * @return builder instance
     */
    fun profile(profileName: String): Encrypt

    /**
     * Encrypt the given data yielding the ciphertext.
     *
     * @param plaintext plaintext
     * @return input stream containing the ciphertext
     * @throws IOException in case of an IO error
     * @throws KeyIsProtected if at least one signing key cannot be unlocked
     */
    @Throws(IOException::class, KeyIsProtected::class) fun plaintext(plaintext: InputStream): Ready

    /**
     * Encrypt the given data yielding the ciphertext.
     *
     * @param plaintext plaintext
     * @return input stream containing the ciphertext
     * @throws IOException in case of an IO error
     * @throws KeyIsProtected if at least one signing key cannot be unlocked
     */
    @Throws(IOException::class, KeyIsProtected::class)
    fun plaintext(plaintext: ByteArray): Ready = plaintext(plaintext.inputStream())
}
