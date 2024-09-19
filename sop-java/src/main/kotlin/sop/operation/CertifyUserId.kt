// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.Ready
import sop.exception.SOPGPException
import sop.util.UTF8Util

interface CertifyUserId {

    @Throws(SOPGPException.UnsupportedOption::class) fun noArmor(): CertifyUserId

    @Throws(SOPGPException.UnsupportedOption::class) fun userId(userId: String): CertifyUserId

    @Throws(SOPGPException.PasswordNotHumanReadable::class, SOPGPException.UnsupportedOption::class)
    fun withKeyPassword(password: String): CertifyUserId =
        withKeyPassword(password.toByteArray(UTF8Util.UTF8))

    @Throws(SOPGPException.PasswordNotHumanReadable::class, SOPGPException.UnsupportedOption::class)
    fun withKeyPassword(password: ByteArray): CertifyUserId

    @Throws(SOPGPException.UnsupportedOption::class) fun noRequireSelfSig(): CertifyUserId

    @Throws(SOPGPException.BadData::class, IOException::class, SOPGPException.KeyIsProtected::class)
    fun keys(keys: InputStream): CertifyUserId

    @Throws(SOPGPException.BadData::class, IOException::class, SOPGPException.KeyIsProtected::class)
    fun keys(keys: ByteArray): CertifyUserId = keys(keys.inputStream())

    @Throws(
        SOPGPException.BadData::class, IOException::class, SOPGPException.CertUserIdNoMatch::class)
    fun certs(certs: InputStream): Ready

    @Throws(
        SOPGPException.BadData::class, IOException::class, SOPGPException.CertUserIdNoMatch::class)
    fun certs(certs: ByteArray): Ready = certs(certs.inputStream())
}
