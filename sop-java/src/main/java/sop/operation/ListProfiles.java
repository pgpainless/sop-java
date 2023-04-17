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

}
