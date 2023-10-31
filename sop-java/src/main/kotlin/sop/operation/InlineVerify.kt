// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.ReadyWithResult
import sop.Verification
import sop.exception.SOPGPException.BadData
import sop.exception.SOPGPException.NoSignature

/** API for verification of inline-signed messages. */
interface InlineVerify : AbstractVerify<InlineVerify> {

    /**
     * Provide the inline-signed data. The result can be used to write the plaintext message out and
     * to get the verifications.
     *
     * @param data signed data
     * @return list of signature verifications
     * @throws IOException in case of an IO error
     * @throws NoSignature when no signature is found
     * @throws BadData when the data is invalid OpenPGP data
     */
    @Throws(IOException::class, NoSignature::class, BadData::class)
    fun data(data: InputStream): ReadyWithResult<List<Verification>>

    /**
     * Provide the inline-signed data. The result can be used to write the plaintext message out and
     * to get the verifications.
     *
     * @param data signed data
     * @return list of signature verifications
     * @throws IOException in case of an IO error
     * @throws NoSignature when no signature is found
     * @throws BadData when the data is invalid OpenPGP data
     */
    @Throws(IOException::class, NoSignature::class, BadData::class)
    fun data(data: ByteArray): ReadyWithResult<List<Verification>> = data(data.inputStream())
}
