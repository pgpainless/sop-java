// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.Ready
import sop.exception.SOPGPException.BadData

interface Armor {

    /**
     * Armor the provided data.
     *
     * @param data input stream of unarmored OpenPGP data
     * @return armored data
     * @throws BadData if the data appears to be OpenPGP packets, but those are broken
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, IOException::class) fun data(data: InputStream): Ready

    /**
     * Armor the provided data.
     *
     * @param data unarmored OpenPGP data
     * @return armored data
     * @throws BadData if the data appears to be OpenPGP packets, but those are broken
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, IOException::class)
    fun data(data: ByteArray): Ready = data(data.inputStream())
}
