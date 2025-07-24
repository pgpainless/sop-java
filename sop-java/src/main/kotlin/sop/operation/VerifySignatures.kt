// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.Verification
import sop.exception.SOPGPException.BadData
import sop.exception.SOPGPException.NoSignature

/** API handle for verifying signatures. */
interface VerifySignatures {

    /**
     * Provide the signed data (without signatures).
     *
     * @param data signed data
     * @return list of signature verifications
     * @throws IOException in case of an IO error
     * @throws NoSignature when no valid signature is found
     * @throws BadData when the data is invalid OpenPGP data
     */
    @Throws(IOException::class, NoSignature::class, BadData::class)
    fun data(data: InputStream): List<Verification>

    /**
     * Provide the signed data (without signatures).
     *
     * @param data signed data
     * @return list of signature verifications
     * @throws IOException in case of an IO error
     * @throws NoSignature when no valid signature is found
     * @throws BadData when the data is invalid OpenPGP data
     */
    @Throws(IOException::class, NoSignature::class, BadData::class)
    fun data(data: ByteArray): List<Verification> = data(data.inputStream())
}
