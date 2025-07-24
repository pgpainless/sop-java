// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.InputStream
import sop.Ready
import sop.exception.SOPGPException.BadData
import sop.exception.SOPGPException.KeyIsProtected
import sop.exception.SOPGPException.PasswordNotHumanReadable
import sop.util.UTF8Util

/** Interface for changing key passwords. */
interface ChangeKeyPassword {

    /**
     * Disable ASCII armoring of the output.
     *
     * @return builder instance
     */
    fun noArmor(): ChangeKeyPassword

    /**
     * Provide a passphrase to unlock the secret key. This method can be provided multiple times to
     * provide separate passphrases that are tried as a means to unlock any secret key material
     * encountered.
     *
     * @param oldPassphrase old passphrase
     * @return builder instance
     */
    fun oldKeyPassphrase(oldPassphrase: CharArray): ChangeKeyPassword =
        oldKeyPassphrase(oldPassphrase.concatToString())

    /**
     * Provide a passphrase to unlock the secret key. This method can be provided multiple times to
     * provide separate passphrases that are tried as a means to unlock any secret key material
     * encountered.
     *
     * @param oldPassphrase old passphrase
     * @return builder instance
     */
    fun oldKeyPassphrase(oldPassphrase: String): ChangeKeyPassword

    /**
     * Provide a passphrase to unlock the secret key. This method can be provided multiple times to
     * provide separate passphrases that are tried as a means to unlock any secret key material
     * encountered.
     *
     * @param oldPassphrase old passphrase
     * @return builder instance
     * @throws PasswordNotHumanReadable if the old key passphrase is not human-readable
     */
    @Throws(PasswordNotHumanReadable::class)
    fun oldKeyPassphrase(oldPassphrase: ByteArray): ChangeKeyPassword =
        try {
            oldKeyPassphrase(UTF8Util.decodeUTF8(oldPassphrase))
        } catch (e: CharacterCodingException) {
            throw PasswordNotHumanReadable("Password MUST be a valid UTF8 string.")
        }

    /**
     * Provide a passphrase to re-lock the secret key with. This method can only be used once, and
     * all key material encountered will be encrypted with the given passphrase. If this method is
     * not called, the key material will not be protected.
     *
     * @param newPassphrase new passphrase
     * @return builder instance
     */
    fun newKeyPassphrase(newPassphrase: CharArray): ChangeKeyPassword =
        newKeyPassphrase(newPassphrase.concatToString())

    /**
     * Provide a passphrase to re-lock the secret key with. This method can only be used once, and
     * all key material encountered will be encrypted with the given passphrase. If this method is
     * not called, the key material will not be protected.
     *
     * @param newPassphrase new passphrase
     * @return builder instance
     */
    fun newKeyPassphrase(newPassphrase: String): ChangeKeyPassword

    /**
     * Provide a passphrase to re-lock the secret key with. This method can only be used once, and
     * all key material encountered will be encrypted with the given passphrase. If this method is
     * not called, the key material will not be protected.
     *
     * @param newPassphrase new passphrase
     * @return builder instance
     * @throws PasswordNotHumanReadable if the passphrase is not human-readable
     */
    @Throws(PasswordNotHumanReadable::class)
    fun newKeyPassphrase(newPassphrase: ByteArray): ChangeKeyPassword =
        try {
            newKeyPassphrase(UTF8Util.decodeUTF8(newPassphrase))
        } catch (e: CharacterCodingException) {
            throw PasswordNotHumanReadable("Password MUST be a valid UTF8 string.")
        }

    /**
     * Provide the key material.
     *
     * @param keys input stream of secret key material
     * @return ready
     * @throws KeyIsProtected if any (sub-) key encountered cannot be unlocked.
     * @throws BadData if the key material is malformed
     */
    @Throws(KeyIsProtected::class, BadData::class)
    fun keys(keys: ByteArray): Ready = keys(keys.inputStream())

    /**
     * Provide the key material.
     *
     * @param keys input stream of secret key material
     * @return ready
     * @throws KeyIsProtected if any (sub-) key encountered cannot be unlocked.
     * @throws BadData if the key material is malformed
     */
    @Throws(KeyIsProtected::class, BadData::class) fun keys(keys: InputStream): Ready
}
