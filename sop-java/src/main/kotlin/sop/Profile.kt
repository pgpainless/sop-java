// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import sop.util.Optional
import sop.util.UTF8Util

/**
 * Tuple class bundling a profile name and description.
 *
 * @param name profile name. A profile name is a UTF-8 string that has no whitespace in it. Similar
 *   to OpenPGP Notation names, profile names are divided into two namespaces: The IETF namespace
 *   and the user namespace. A profile name in the user namespace ends with the `@` character (0x40)
 *   followed by a DNS domain name. A profile name in the IETF namespace does not have an `@`
 *   character. A profile name in the user space is owned and controlled by the owner of the domain
 *   in the suffix. A profile name in the IETF namespace that begins with the string `rfc` should
 *   have semantics that hew as closely as possible to the referenced RFC. Similarly, a profile name
 *   in the IETF namespace that begins with the string `draft-` should have semantics that hew as
 *   closely as possible to the referenced Internet Draft.
 * @param description a free-form description of the profile.
 * @see <a
 *   href="https://www.ietf.org/archive/id/draft-dkg-openpgp-stateless-cli-05.html#name-profile">
 *   SOP Spec - Profile</a>
 */
data class Profile(val name: String, val description: Optional<String>) {

    @JvmOverloads
    constructor(
        name: String,
        description: String? = null
    ) : this(name, Optional.ofNullable(description?.trim()?.ifBlank { null }))

    init {
        require(name.trim().isNotBlank()) { "Name cannot be empty." }
        require(!name.contains(":")) { "Name cannot contain ':'." }
        require(listOf(" ", "\n", "\t", "\r").none { name.contains(it) }) {
            "Name cannot contain whitespace characters."
        }
        require(!exceeds1000CharLineLimit(this)) {
            "The line representation of a profile MUST NOT exceed 1000 bytes."
        }
    }

    fun hasDescription() = description.isPresent

    /**
     * Convert the profile into a String for displaying.
     *
     * @return string
     */
    override fun toString(): String =
        if (description.isEmpty) name else "$name: ${description.get()}"

    companion object {

        /**
         * Parse a [Profile] from its string representation.
         *
         * @param string string representation
         * @return profile
         */
        @JvmStatic
        fun parse(string: String): Profile {
            return if (string.contains(": ")) {
                Profile(
                    string.substring(0, string.indexOf(": ")),
                    string.substring(string.indexOf(": ") + 2).trim())
            } else if (string.endsWith(":")) {
                Profile(string.substring(0, string.length - 1))
            } else {
                Profile(string.trim())
            }
        }

        /**
         * Test if the string representation of the profile exceeds the limit of 1000 bytes length.
         *
         * @param profile profile
         * @return `true` if the profile exceeds 1000 bytes, `false` otherwise.
         */
        @JvmStatic
        private fun exceeds1000CharLineLimit(profile: Profile): Boolean =
            profile.toString().toByteArray(UTF8Util.UTF8).size > 1000
    }
}
