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

    @Throws(SOPGPException.UnsupportedOption::class) fun signingOnly(): UpdateKey

    @Throws(SOPGPException.UnsupportedOption::class) fun noNewMechanisms(): UpdateKey

    @Throws(SOPGPException.PasswordNotHumanReadable::class, SOPGPException.UnsupportedOption::class)
    fun withKeyPassword(password: String): UpdateKey =
        withKeyPassword(password.toByteArray(UTF8Util.UTF8))

    @Throws(SOPGPException.PasswordNotHumanReadable::class, SOPGPException.UnsupportedOption::class)
    fun withKeyPassword(password: ByteArray): UpdateKey

    @Throws(
        SOPGPException.UnsupportedOption::class, SOPGPException.BadData::class, IOException::class)
    fun mergeCerts(certs: InputStream): UpdateKey

    @Throws(
        SOPGPException.UnsupportedOption::class, SOPGPException.BadData::class, IOException::class)
    fun mergeCerts(certs: ByteArray): UpdateKey = mergeCerts(certs.inputStream())

    @Throws(
        SOPGPException.BadData::class,
        IOException::class,
        SOPGPException.KeyIsProtected::class,
        SOPGPException.PrimaryKeyBad::class)
    fun key(key: InputStream): Ready

    @Throws(
        SOPGPException.BadData::class,
        IOException::class,
        SOPGPException.KeyIsProtected::class,
        SOPGPException.PrimaryKeyBad::class)
    fun key(key: ByteArray): Ready = key(key.inputStream())
}
