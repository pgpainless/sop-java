// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import sop.util.Optional;
import sop.util.UTF8Util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Tuple class bundling a profile name and description.
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-dkg-openpgp-stateless-cli-05.html#name-profile">
 *     SOP Spec - Profile</a>
 */
public class Profile {

    private final String name;
    private final Optional<String> description;

    /**
     * Create a new {@link Profile} object.
     * The {@link #toString()} representation MUST NOT exceed a length of 1000 bytes.
     *
     * @param name profile name
     * @param description profile description
     */
    public Profile(@Nonnull String name, @Nullable String description) {
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (name.contains(":")) {
            throw new IllegalArgumentException("Name cannot contain ':'.");
        }
        if (name.contains(" ") || name.contains("\n") || name.contains("\t") || name.contains("\r")) {
            throw new IllegalArgumentException("Name cannot contain whitespace characters.");
        }

        this.name = name;

        if (description == null) {
            this.description = Optional.ofEmpty();
        } else {
            String trimmedDescription = description.trim();
            if (trimmedDescription.isEmpty()) {
                this.description = Optional.ofEmpty();
            } else {
                this.description = Optional.of(trimmedDescription);
            }
        }

        if (exceeds1000CharLineLimit(this)) {
            throw new IllegalArgumentException("The line representation of a profile MUST NOT exceed 1000 bytes.");
        }
    }

    public Profile(String name) {
        this(name, null);
    }

    /**
     * Parse a {@link Profile} from its string representation.
     *
     * @param string string representation
     * @return profile
     */
    public static Profile parse(String string) {
        if (string.contains(": ")) {
            // description after colon, e.g. "default: Use implementers recommendations."
            String name = string.substring(0, string.indexOf(": "));
            String description = string.substring(string.indexOf(": ") + 2);
            return new Profile(name, description.trim());
        }

        if (string.endsWith(":")) {
            // empty description, e.g. "default:"
            return new Profile(string.substring(0, string.length() - 1));
        }

        // no description
        return new Profile(string.trim());
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
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Return a free-form description of the profile.
     *
     * @return description
     */
    @Nonnull
    public Optional<String> getDescription() {
        return description;
    }

    public boolean hasDescription() {
        return description.isPresent();
    }

    /**
     * Convert the profile into a String for displaying.
     *
     * @return string
     */
    @Override
    public String toString() {
        if (getDescription().isEmpty()) {
            return getName();
        }
        return getName() + ": " + getDescription().get();
    }

    /**
     * Test if the string representation of the profile exceeds the limit of 1000 bytes length.
     * @param profile profile
     * @return <pre>true</pre> if the profile exceeds 1000 bytes, <pre>false</pre> otherwise.
     */
    private static boolean exceeds1000CharLineLimit(Profile profile) {
        String line = profile.toString();
        return line.getBytes(UTF8Util.UTF8).length > 1000;
    }
}
