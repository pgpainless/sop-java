// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import java.io.InputStream

/**
 * Tuple of a [ByteArray] and associated result object.
 *
 * @param bytes byte array
 * @param result result object
 * @param <T> type of result
 */
data class ByteArrayAndResult<T>(val bytes: ByteArray, val result: T) {

    /**
     * [InputStream] returning the contents of [bytes].
     *
     * @return input stream
     */
    val inputStream: InputStream
        get() = bytes.inputStream()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ByteArrayAndResult<*>

        if (!bytes.contentEquals(other.bytes)) return false
        if (result != other.result) return false

        return true
    }

    override fun hashCode(): Int {
        var hashCode = bytes.contentHashCode()
        hashCode = 31 * hashCode + (result?.hashCode() ?: 0)
        return hashCode
    }
}
