// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import sop.exception.SOPGPException

abstract class ReadyWithResult<T> {

    /**
     * Write the data e.g. decrypted plaintext to the provided output stream and return the result
     * of the processing operation.
     *
     * @param outputStream output stream
     * @return result, eg. signatures
     * @throws IOException in case of an IO error
     * @throws SOPGPException in case of a SOP protocol error
     */
    @Throws(IOException::class, SOPGPException::class)
    abstract fun writeTo(outputStream: OutputStream): T

    /**
     * Return the data as a [ByteArrayAndResult]. Calling [ByteArrayAndResult.bytes] will give you
     * access to the data as byte array, while [ByteArrayAndResult.result] will grant access to the
     * appended result.
     *
     * @return byte array and result
     * @throws IOException in case of an IO error
     * @throws SOPGPException.NoSignature if there are no valid signatures found
     */
    @Throws(IOException::class, SOPGPException::class)
    fun toByteArrayAndResult() =
        ByteArrayOutputStream().let {
            val result = writeTo(it)
            ByteArrayAndResult(it.toByteArray(), result)
        }
}
