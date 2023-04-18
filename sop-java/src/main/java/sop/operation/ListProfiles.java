// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.Profile;

import java.util.List;

/**
 * Subcommand to list supported profiles of other subcommands.
 */
public interface ListProfiles {

    /**
     * Provide the name of the subcommand for which profiles shall be listed.
     * The returned list of profiles MUST NOT contain more than 4 entries.
     *
     * @param command command name (e.g. <pre>generate-key</pre>)
     * @return list of profiles.
     */
    List<Profile> subcommand(String command);

    /**
     * Return a list of {@link Profile Profiles} supported by the {@link GenerateKey} implementation.
     *
     * @return profiles
     */
    default List<Profile> generateKey() {
        return subcommand("generate-key");
    }

    /**
     * Return a list of {@link Profile Profiles} supported by the {@link Encrypt} implementation.
     *
     * @return profiles
     */
    default List<Profile> encrypt() {
        return subcommand("encrypt");
    }

}
