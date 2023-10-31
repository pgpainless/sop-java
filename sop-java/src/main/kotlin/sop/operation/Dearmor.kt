// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.Ready
import sop.exception.SOPGPException.BadData
import sop.util.UTF8Util

interface Dearmor {

    /**
     * Dearmor armored OpenPGP data.
     *
     * @param data armored OpenPGP data
     * @return input stream of unarmored data
     * @throws BadData in case of non-OpenPGP data
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, IOException::class) fun data(data: InputStream): Ready

    /**
     * Dearmor armored OpenPGP data.
     *
     * @param data armored OpenPGP data
     * @return input stream of unarmored data
     * @throws BadData in case of non-OpenPGP data
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, IOException::class)
    fun data(data: ByteArray): Ready = data(data.inputStream())

    /**
     * Dearmor amored OpenPGP data.
     *
     * @param data armored OpenPGP data
     * @return input stream of unarmored data
     * @throws BadData in case of non-OpenPGP data
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, IOException::class)
    fun data(data: String): Ready = data(data.toByteArray(UTF8Util.UTF8))
}
