// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

/**
 * Tuple class bundling a profile name and description.
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-dkg-openpgp-stateless-cli-05.html#name-profile">
 *     SOP Spec - Profile</a>
 */
public class Profile {

    private final String name;
    private final String description;

    public Profile(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return getName() + ": " + getDescription();
    }
}
