// SPDX-FileCopyrightText: 2025 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.text.ParseException

class GsonSerializerAndParserTest {

    private val serializer: GsonSerializer = GsonSerializer()
    private val parser: GsonParser = GsonParser()

    @Test
    fun simpleSingleTest() {
        val before = Verification.JSON("/tmp/alice.pgp")

        val json = serializer.serialize(before)
        assertEquals("{\"signers\":[\"/tmp/alice.pgp\"]}", json)

        val after = parser.parse(json)

        assertEquals(before, after)
    }

    @Test
    fun simpleListTest() {
        val before = Verification.JSON(listOf("/tmp/alice.pgp", "/tmp/bob.asc"))

        val json = serializer.serialize(before)
        assertEquals("{\"signers\":[\"/tmp/alice.pgp\",\"/tmp/bob.asc\"]}", json)

        val after = parser.parse(json)

        assertEquals(before, after)
    }

    @Test
    fun withCommentTest() {
        val before = Verification.JSON(
            listOf("/tmp/alice.pgp"),
            "This is a comment.",
            null)

        val json = serializer.serialize(before)
        assertEquals("{\"signers\":[\"/tmp/alice.pgp\"],\"comment\":\"This is a comment.\"}", json)

        val after = parser.parse(json)

        assertEquals(before, after)
    }

    @Test
    fun withExtStringTest() {
        val before = Verification.JSON(
            listOf("/tmp/alice.pgp"),
            "This is a comment.",
            "This is an ext object string.")

        val json = serializer.serialize(before)
        assertEquals("{\"signers\":[\"/tmp/alice.pgp\"],\"comment\":\"This is a comment.\",\"ext\":\"This is an ext object string.\"}", json)

        val after = parser.parse(json)

        assertEquals(before, after)
    }

    @Test
    fun withExtListTest() {
        val before = Verification.JSON(
            listOf("/tmp/alice.pgp"),
            "This is a comment.",
            listOf(1.0,2.0,3.0))

        val json = serializer.serialize(before)
        assertEquals("{\"signers\":[\"/tmp/alice.pgp\"],\"comment\":\"This is a comment.\",\"ext\":[1.0,2.0,3.0]}", json)

        val after = parser.parse(json)

        assertEquals(before, after)
    }

    @Test
    fun parseInvalidJSON() {
        assertThrows<ParseException> { parser.parse("Invalid") }
    }

    @Test
    fun parseMalformedJSON() {
        // Missing '}'
        assertThrows<ParseException> { parser.parse("{\"signers\":[\"Alice\"]") }
    }
}