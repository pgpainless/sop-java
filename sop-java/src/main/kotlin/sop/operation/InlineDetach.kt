// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.ReadyWithResult
import sop.Signatures
import sop.exception.SOPGPException.BadData

interface InlineDetach {

    /**
     * Do not wrap the signatures in ASCII armor.
     *
     * @return builder
     */
    fun noArmor(): InlineDetach

    /**
     * Detach the provided signed message from its signatures.
     *
     * @param messageInputStream input stream containing the signed message
     * @return result containing the detached message
     * @throws IOException in case of an IO error
     * @throws BadData if the input stream does not contain a signed message
     */
    @Throws(IOException::class, BadData::class)
    fun message(messageInputStream: InputStream): ReadyWithResult<Signatures>

    /**
     * Detach the provided cleartext signed message from its signatures.
     *
     * @param message byte array containing the signed message
     * @return result containing the detached message
     * @throws IOException in case of an IO error
     * @throws BadData if the byte array does not contain a signed message
     */
    @Throws(IOException::class, BadData::class)
    fun message(message: ByteArray): ReadyWithResult<Signatures> = message(message.inputStream())
}
