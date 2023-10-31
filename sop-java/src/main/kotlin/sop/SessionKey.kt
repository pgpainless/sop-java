// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import sop.util.HexUtil

/**
 * Class representing a symmetric session key.
 *
 * @param algorithm symmetric key algorithm ID
 * @param key [ByteArray] containing the session key
 */
data class SessionKey(val algorithm: Byte, val key: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionKey

        if (algorithm != other.algorithm) return false
        if (!key.contentEquals(other.key)) return false

        return true
    }

    override fun hashCode(): Int {
        var hashCode = algorithm.toInt()
        hashCode = 31 * hashCode + key.contentHashCode()
        return hashCode
    }

    override fun toString(): String = "$algorithm:${HexUtil.bytesToHex(key)}"

    companion object {

        @JvmStatic private val PATTERN = "^(\\d):([0-9A-F]+)$".toPattern()

        @JvmStatic
        fun fromString(string: String): SessionKey {
            val matcher = PATTERN.matcher(string.trim().uppercase().replace("\n", ""))
            require(matcher.matches()) { "Provided session key does not match expected format." }
            return SessionKey(matcher.group(1).toByte(), HexUtil.hexToBytes(matcher.group(2)))
        }
    }
}
