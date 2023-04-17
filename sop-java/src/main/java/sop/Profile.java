// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import java.nio.charset.Charset;

/**
 * Tuple class bundling a profile name and description.
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-dkg-openpgp-stateless-cli-05.html#name-profile">
 *     SOP Spec - Profile</a>
 */
public class Profile {

    private final String name;
    private final String description;

    /**
     * Create a new {@link Profile} object.
     * The {@link #toString()} representation MUST NOT exceed a length of 1000 bytes.
     *
     * @param name profile name
     * @param description profile description
     */
    public Profile(String name, String description) {
        this.name = name;
        this.description = description;

        if (exceeds1000CharLineLimit(this)) {
            throw new IllegalArgumentException("The line representation of a profile MUST NOT exceed 1000 bytes.");
        }
    }

    /**
     * Return the name (also known as identifier) of the profile.
     * A profile name is a UTF-8 string that has no whitespace in it.
     * Similar to OpenPGP Notation names, profile names are divided into two namespaces:
     * The IETF namespace and the user namespace.
     * A profile name in the user namespace ends with the <pre>@</pre> character (0x40) followed by a DNS domain name.
     * A profile name in the IETF namespace does not have an <pre>@</pre> character.
     * A profile name in the user space is owned and controlled by the owner of the domain in the suffix.
     * A profile name in the IETF namespace that begins with the string <pre>rfc</pre> should have semantics that hew as
     * closely as possible to the referenced RFC.
     * Similarly, a profile name in the IETF namespace that begins with the string <pre>draft-</pre> should have
     * semantics that hew as closely as possible to the referenced Internet Draft.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Return a free-form description of the profile.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Convert the profile into a String for displaying.
     *
     * @return string
     */
    public String toString() {
        return getName() + ": " + getDescription();
    }

    /**
     * Test if the string representation of the profile exceeds the limit of 1000 bytes length.
     * @param profile profile
     * @return <pre>true</pre> if the profile exceeds 1000 bytes, <pre>false</pre> otherwise.
     */
    private static boolean exceeds1000CharLineLimit(Profile profile) {
        String line = profile.toString();
        return line.getBytes(Charset.forName("UTF8")).length > 1000;
    }
}
