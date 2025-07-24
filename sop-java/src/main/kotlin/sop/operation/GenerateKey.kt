// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import sop.Profile
import sop.Ready
import sop.exception.SOPGPException.*
import sop.util.UTF8Util

/** Interface for generating OpenPGP keys. */
interface GenerateKey {

    /**
     * Disable ASCII armor encoding.
     *
     * @return builder instance
     */
    fun noArmor(): GenerateKey

    /**
     * Adds a user-id.
     *
     * @param userId user-id
     * @return builder instance
     */
    fun userId(userId: String): GenerateKey

    /**
     * Set a password for the key.
     *
     * @param password password to protect the key
     * @return builder instance
     * @throws UnsupportedOption if key passwords are not supported
     * @throws PasswordNotHumanReadable if the password is not human-readable
     */
    @Throws(PasswordNotHumanReadable::class, UnsupportedOption::class)
    fun withKeyPassword(password: String): GenerateKey

    /**
     * Set a password for the key.
     *
     * @param password password to protect the key
     * @return builder instance
     * @throws PasswordNotHumanReadable if the password is not human-readable
     * @throws UnsupportedOption if key passwords are not supported
     */
    @Throws(PasswordNotHumanReadable::class, UnsupportedOption::class)
    fun withKeyPassword(password: ByteArray): GenerateKey =
        try {
            withKeyPassword(UTF8Util.decodeUTF8(password))
        } catch (e: CharacterCodingException) {
            throw PasswordNotHumanReadable()
        }

    /**
     * Pass in a profile.
     *
     * @param profile profile
     * @return builder instance
     */
    fun profile(profile: Profile): GenerateKey = profile(profile.name)

    /**
     * Pass in a profile identifier.
     *
     * @param profile profile identifier
     * @return builder instance
     */
    fun profile(profile: String): GenerateKey

    /**
     * If this options is set, the generated key will not be capable of encryption / decryption.
     *
     * @return builder instance
     */
    fun signingOnly(): GenerateKey

    /**
     * Generate the OpenPGP key and return it encoded as an [java.io.InputStream].
     *
     * @return key
     * @throws MissingArg if no user-id was provided
     * @throws UnsupportedAsymmetricAlgo if the generated key uses an unsupported asymmetric
     *   algorithm
     * @throws IOException in case of an IO error
     */
    @Throws(MissingArg::class, UnsupportedAsymmetricAlgo::class, IOException::class)
    fun generate(): Ready
}
