// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.InputStream
import sop.Ready
import sop.exception.SOPGPException.PasswordNotHumanReadable
import sop.exception.SOPGPException.UnsupportedOption
import sop.util.UTF8Util

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
     * @throws PasswordNotHumanReadable if the password is not human-readable
     */
    @Throws(UnsupportedOption::class, PasswordNotHumanReadable::class)
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

    fun keys(bytes: ByteArray): Ready = keys(bytes.inputStream())

    fun keys(keys: InputStream): Ready
}
