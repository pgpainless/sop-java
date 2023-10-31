// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** Abstract class that encapsulates output data, waiting to be consumed. */
abstract class Ready {

    /**
     * Write the data to the provided output stream.
     *
     * @param outputStream output stream
     * @throws IOException in case of an IO error
     */
    @Throws(IOException::class) abstract fun writeTo(outputStream: OutputStream)

    /**
     * Return the data as a byte array by writing it to a [ByteArrayOutputStream] first and then
     * returning the array.
     *
     * @return data as byte array
     * @throws IOException in case of an IO error
     */
    val bytes: ByteArray
        @Throws(IOException::class)
        get() =
            ByteArrayOutputStream()
                .let {
                    writeTo(it)
                    it
                }
                .toByteArray()

    /**
     * Return an input stream containing the data.
     *
     * @return input stream
     * @throws IOException in case of an IO error
     */
    val inputStream: InputStream
        @Throws(IOException::class) get() = ByteArrayInputStream(bytes)
}
