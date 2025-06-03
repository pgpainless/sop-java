// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import java.text.ParseException
import java.util.Date
import sop.enums.SignatureMode
import sop.util.Optional
import sop.util.UTCUtil

data class Verification(
    val creationTime: Date,
    val signingKeyFingerprint: String,
    val signingCertFingerprint: String,
    val signatureMode: Optional<SignatureMode>,
    val jsonOrDescription: Optional<String>
) {
    @JvmOverloads
    constructor(
        creationTime: Date,
        signingKeyFingerprint: String,
        signingCertFingerprint: String,
        signatureMode: SignatureMode? = null,
        description: String? = null
    ) : this(
        creationTime,
        signingKeyFingerprint,
        signingCertFingerprint,
        Optional.ofNullable(signatureMode),
        Optional.ofNullable(description?.trim()))

    @Deprecated("Replaced by jsonOrDescription",
        replaceWith = ReplaceWith("jsonOrDescription")
    )
    val description = jsonOrDescription

    override fun toString(): String =
        "${UTCUtil.formatUTCDate(creationTime)} $signingKeyFingerprint $signingCertFingerprint" +
            (if (signatureMode.isPresent) " mode:${signatureMode.get()}" else "") +
            (if (jsonOrDescription.isPresent) " ${jsonOrDescription.get()}" else "")

    companion object {
        @JvmStatic
        fun fromString(string: String): Verification {
            val split = string.trim().split(" ")
            require(split.size >= 3) {
                "Verification must be of the format 'UTC-DATE OpenPGPFingerprint OpenPGPFingerprint [mode] [info]'."
            }
            if (split.size == 3) {
                return Verification(parseUTCDate(split[0]), split[1], split[2])
            }

            var index = 3
            val mode =
                if (split[3].startsWith("mode:")) {
                    index += 1
                    SignatureMode.valueOf(split[3].substring("mode:".length))
                } else null

            val description = split.subList(index, split.size).joinToString(" ").ifBlank { null }

            return Verification(
                parseUTCDate(split[0]),
                split[1],
                split[2],
                Optional.ofNullable(mode),
                Optional.ofNullable(description))
        }

        @JvmStatic
        private fun parseUTCDate(string: String): Date {
            return try {
                UTCUtil.parseUTCDate(string)
            } catch (e: ParseException) {
                throw IllegalArgumentException("Malformed UTC timestamp.", e)
            }
        }
    }
}
