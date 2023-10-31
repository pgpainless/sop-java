// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.util

class HexUtil {

    companion object {
        /**
         * Encode a byte array to a hex string.
         *
         * @param bytes bytes
         * @return hex encoding
         * @see
         *   [Convert Byte Arrays to Hex Strings in Kotlin](https://www.baeldung.com/kotlin/byte-arrays-to-hex-strings)
         */
        @JvmStatic fun bytesToHex(bytes: ByteArray): String = bytes.toHex()

        /**
         * Decode a hex string into a byte array.
         *
         * @param s hex string
         * @return decoded byte array
         * @see
         *   [Kotlin convert hex string to ByteArray](https://stackoverflow.com/a/66614516/11150851)
         */
        @JvmStatic fun hexToBytes(s: String): ByteArray = s.decodeHex()
    }
}

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Hex encoding must have even number of digits." }

    return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02X".format(eachByte) }
