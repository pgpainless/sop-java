// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import sop.Profile

/** Subcommand to list supported profiles of other subcommands. */
interface ListProfiles {

    /**
     * Provide the name of the subcommand for which profiles shall be listed. The returned list of
     * profiles MUST NOT contain more than 4 entries.
     *
     * @param command command name (e.g. `generate-key`)
     * @return list of profiles.
     */
    fun subcommand(command: String): List<Profile>

    /**
     * Return a list of [Profiles][Profile] supported by the [GenerateKey] implementation.
     *
     * @return profiles
     */
    fun generateKey(): List<Profile> = subcommand("generate-key")

    /**
     * Return a list of [Profiles][Profile] supported by the [Encrypt] implementation.
     *
     * @return profiles
     */
    fun encrypt(): List<Profile> = subcommand("encrypt")
}
