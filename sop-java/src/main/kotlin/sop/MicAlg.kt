// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import java.io.OutputStream
import java.io.PrintWriter

data class MicAlg(val micAlg: String) {

    fun writeTo(outputStream: OutputStream) {
        PrintWriter(outputStream).use { it.write(micAlg) }
    }

    companion object {
        @JvmStatic fun empty() = MicAlg("")

        @JvmStatic
        fun fromHashAlgorithmId(id: Int) =
            when (id) {
                1 -> "pgp-md5"
                2 -> "pgp-sha1"
                3 -> "pgp-ripemd160"
                8 -> "pgp-sha256"
                9 -> "pgp-sha384"
                10 -> "pgp-sha512"
                11 -> "pgp-sha224"
                12 -> "pgp-sha3-256"
                14 -> "pgp-sha3-512"
                else -> throw IllegalArgumentException("Unsupported hash algorithm ID: $id")
            }.let { MicAlg(it) }
    }
}
