// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.Ready
import sop.exception.SOPGPException.BadData

interface ExtractCert {

    /**
     * Disable ASCII armor encoding.
     *
     * @return builder instance
     */
    fun noArmor(): ExtractCert

    /**
     * Extract the cert(s) from the provided key(s).
     *
     * @param keyInputStream input stream containing the encoding of one or more OpenPGP keys
     * @return result containing the encoding of the keys certs
     * @throws IOException in case of an IO error
     * @throws BadData if the [InputStream] does not contain an OpenPGP key
     */
    @Throws(IOException::class, BadData::class) fun key(keyInputStream: InputStream): Ready

    /**
     * Extract the cert(s) from the provided key(s).
     *
     * @param key byte array containing the encoding of one or more OpenPGP key
     * @return result containing the encoding of the keys certs
     * @throws IOException in case of an IO error
     * @throws BadData if the byte array does not contain an OpenPGP key
     */
    @Throws(IOException::class, BadData::class)
    fun key(key: ByteArray): Ready = key(key.inputStream())
}
