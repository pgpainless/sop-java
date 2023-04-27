// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import org.junit.jupiter.api.Test;
import sop.Profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProfileTest {

    @Test
    public void toStringFull() {
        Profile profile = new Profile("default", "Use the implementers recommendations.");
        assertEquals("default: Use the implementers recommendations.", profile.toString());
    }

    @Test
    public void toStringNameOnly() {
        Profile profile = new Profile("default");
        assertEquals("default", profile.toString());
    }

    @Test
    public void parseFull() {
        String string = "default: Use the implementers recommendations.";
        Profile profile = Profile.parse(string);
        assertEquals("default", profile.getName());
        assertTrue(profile.hasDescription());
        assertEquals("Use the implementers recommendations.", profile.getDescription().get());
    }

    @Test
    public void parseNameOnly() {
        String string = "rfc4880";
        Profile profile = Profile.parse(string);
        assertEquals("rfc4880", profile.getName());
        assertFalse(profile.hasDescription());
    }

    @Test
    public void parseEmptyDescription() {
        String string = "rfc4880: ";
        Profile profile = Profile.parse(string);
        assertEquals("rfc4880", profile.getName());
        assertFalse(profile.hasDescription());

        string = "rfc4880:";
        profile = Profile.parse(string);
        assertEquals("rfc4880", profile.getName());
        assertFalse(profile.hasDescription());
    }

    @Test
    public void parseTooLongProfile() {
        // 1200 chars
        String string = "longDescription: Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
        assertThrows(IllegalArgumentException.class, () -> Profile.parse(string));
    }

    @Test
    public void constructTooLongProfile() {
        // name + description = 1200 chars
        String name = "longDescription";
        String description = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
        assertThrows(IllegalArgumentException.class, () -> new Profile(name, description));
    }

    @Test
    public void nameCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Profile(""));
        assertThrows(IllegalArgumentException.class, () -> new Profile(""), "Description Text.");
    }

    @Test
    public void nameCannotContainColons() {
        assertThrows(IllegalArgumentException.class, () -> new Profile("default:"));
        assertThrows(IllegalArgumentException.class, () -> new Profile("default:", "DescriptionText"));
        assertThrows(IllegalArgumentException.class, () -> new Profile("rfc:4880"));
        assertThrows(IllegalArgumentException.class, () -> new Profile("rfc:4880", "OpenPGP Message Format"));
    }

    @Test
    public void nameCannotContainWhitespace() {
        assertThrows(IllegalArgumentException.class, () -> new Profile("default profile"));
        assertThrows(IllegalArgumentException.class, () -> new Profile("default profile", "With description."));
        assertThrows(IllegalArgumentException.class, () -> new Profile("default\nprofile"));
        assertThrows(IllegalArgumentException.class, () -> new Profile("default\nprofile", "With description"));
        assertThrows(IllegalArgumentException.class, () -> new Profile("default\tprofile"));
        assertThrows(IllegalArgumentException.class, () -> new Profile("default\tprofile", "With description"));
        assertThrows(IllegalArgumentException.class, () -> new Profile("default\r\nprofile"));
        assertThrows(IllegalArgumentException.class, () -> new Profile("default\r\nprofile", "With description"));
        assertThrows(IllegalArgumentException.class, () -> new Profile("default\rprofile"));
        assertThrows(IllegalArgumentException.class, () -> new Profile("default\rprofile", "With description"));
    }
}
