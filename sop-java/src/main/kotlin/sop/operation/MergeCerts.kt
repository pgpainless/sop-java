// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.Ready
import sop.exception.SOPGPException

interface MergeCerts {

    @Throws(SOPGPException.UnsupportedOption::class) fun noArmor(): MergeCerts

    @Throws(SOPGPException.BadData::class, IOException::class)
    fun updates(updateCerts: InputStream): MergeCerts

    @Throws(SOPGPException.BadData::class, IOException::class)
    fun updates(updateCerts: ByteArray): MergeCerts = updates(updateCerts.inputStream())

    @Throws(SOPGPException.BadData::class, IOException::class)
    fun baseCertificates(certs: InputStream): Ready

    @Throws(SOPGPException.BadData::class, IOException::class)
    fun baseCertificates(certs: ByteArray): Ready = baseCertificates(certs.inputStream())
}
