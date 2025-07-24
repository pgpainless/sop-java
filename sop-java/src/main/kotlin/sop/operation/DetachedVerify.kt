// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.exception.SOPGPException.BadData

/** Interface for verifying detached OpenPGP signatures over plaintext messages. */
interface DetachedVerify : AbstractVerify<DetachedVerify>, VerifySignatures {

    /**
     * Provides the detached signatures.
     *
     * @param signatures input stream containing encoded, detached signatures.
     * @return builder instance
     * @throws BadData if the input stream does not contain OpenPGP signatures
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, IOException::class)
    fun signatures(signatures: InputStream): VerifySignatures

    /**
     * Provides the detached signatures.
     *
     * @param signatures byte array containing encoded, detached signatures.
     * @return builder instance
     * @throws BadData if the byte array does not contain OpenPGP signatures
     * @throws IOException in case of an IO error
     */
    @Throws(BadData::class, IOException::class)
    fun signatures(signatures: ByteArray): VerifySignatures = signatures(signatures.inputStream())
}
