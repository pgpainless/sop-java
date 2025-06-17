// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import java.text.ParseException
import java.util.Date
import sop.enums.SignatureMode
import sop.util.Optional
import sop.util.UTCUtil

/**
 * Metadata about a verified signature.
 *
 * @param creationTime creation time of the signature
 * @param signingKeyFingerprint fingerprint of the (sub-)key that issued the signature
 * @param signingCertFingerprint fingerprint of the certificate that contains the signing key
 * @param signatureMode optional signature mode (text/binary)
 * @param jsonOrDescription arbitrary text or JSON data
 */
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

    @JvmOverloads
    constructor(
        creationTime: Date,
        signingKeyFingerprint: String,
        signingCertFingerprint: String,
        signatureMode: SignatureMode? = null,
        json: JSON,
        jsonSerializer: JSONSerializer
    ) : this(
        creationTime,
        signingKeyFingerprint,
        signingCertFingerprint,
        Optional.ofNullable(signatureMode),
        Optional.of(jsonSerializer.serialize(json)))

    @Deprecated("Replaced by jsonOrDescription", replaceWith = ReplaceWith("jsonOrDescription"))
    val description = jsonOrDescription

    /** This value is `true` if the [Verification] contains extension JSON. */
    val containsJson: Boolean =
        jsonOrDescription.get()?.trim()?.let { it.startsWith("{") && it.endsWith("}") } ?: false

    /**
     * Attempt to parse the [jsonOrDescription] field using the provided [JSONParser] and return the
     * result. This method returns `null` if parsing fails.
     *
     * @param parser [JSONParser] implementation
     * @return successfully parsed [JSON] POJO or `null`.
     */
    fun getJson(parser: JSONParser): JSON? {
        return jsonOrDescription.get()?.let {
            try {
                parser.parse(it)
            } catch (e: ParseException) {
                null
            }
        }
    }

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

    /**
     * POJO data class representing JSON metadata.
     *
     * @param signers list of supplied CERTS objects that could have issued the signature,
     *   identified by the name given on the command line.
     * @param comment a freeform UTF-8 encoded text describing the verification
     * @param ext an extension object containing arbitrary, implementation-specific data
     */
    data class JSON(val signers: List<String>, val comment: String?, val ext: Any?) {

        /** Create a JSON object with only a list of signers. */
        constructor(signers: List<String>) : this(signers, null, null)

        /** Create a JSON object with only a single signer. */
        constructor(signer: String) : this(listOf(signer))
    }

    /** Interface abstracting a JSON parser that parses [JSON] POJOs from single-line strings. */
    fun interface JSONParser {
        /**
         * Parse a [JSON] POJO from the given single-line [string]. If the string does not represent
         * a JSON object matching the [JSON] definition, this method throws a [ParseException].
         *
         * @param string [String] representation of the [JSON] object.
         * @return parsed [JSON] POJO
         * @throws ParseException if the [string] is not a JSON string representing the [JSON]
         *   object.
         */
        @Throws(ParseException::class) fun parse(string: String): JSON
    }

    /**
     * Interface abstracting a JSON serializer that converts [JSON] POJOs into single-line JSON
     * strings.
     */
    fun interface JSONSerializer {

        /**
         * Serialize the given [JSON] object into a single-line JSON string.
         *
         * @param json JSON POJO
         * @return JSON string
         */
        fun serialize(json: JSON): String
    }
}
