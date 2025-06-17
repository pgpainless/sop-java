// SPDX-FileCopyrightText: 2025 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import sop.enums.SignatureMode;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VerificationJSONTest {

    // A hacky self-made "JSON parser" stand-in.
    // Only used for testing, do not use in production!
    private Verification.JSONParser dummyParser = new Verification.JSONParser() {
        @NotNull
        @Override
        public Verification.JSON parse(@NotNull String string) throws ParseException {
            if (!string.startsWith("{")) {
                throw new ParseException("Alleged JSON String does not begin with '{'", 0);
            }
            if (!string.endsWith("}")) {
                throw new ParseException("Alleged JSON String does not end with '}'", string.length() - 1);
            }

            List<String> signersList = new ArrayList<>();
            Matcher signersMat = Pattern.compile("\"signers\": \\[(.*?)\\]").matcher(string);
            if (signersMat.find()) {
                String signersCat = signersMat.group(1);
                String[] split = signersCat.split(",");
                for (String s : split) {
                    s = s.trim();
                    signersList.add(s.substring(1, s.length() - 1));
                }
            }

            String comment = null;
            Matcher commentMat = Pattern.compile("\"comment\": \"(.*?)\"").matcher(string);
            if (commentMat.find()) {
                comment = commentMat.group(1);
            }

            String ext = null;
            Matcher extMat = Pattern.compile("\"ext\": (.*?})}").matcher(string);
            if (extMat.find()) {
                ext = extMat.group(1);
            }

            return new Verification.JSON(signersList, comment, ext);
        }
    };

    // A just as hacky "JSON Serializer" lookalike.
    // Also don't use in production, for testing only!
    private Verification.JSONSerializer dummySerializer = new Verification.JSONSerializer() {
        @NotNull
        @Override
        public String serialize(@NotNull Verification.JSON json) {
            if (json.getSigners().isEmpty() && json.getComment() == null && json.getExt() == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder("{");
            boolean comma = false;

            if (!json.getSigners().isEmpty()) {
                comma = true;
                sb.append("\"signers\": [");
                for (Iterator<String> iterator = json.getSigners().iterator(); iterator.hasNext(); ) {
                    String signer = iterator.next();
                    sb.append('\"').append(signer).append('\"');
                    if (iterator.hasNext()) {
                        sb.append(", ");
                    }
                }
                sb.append(']');
            }

            if (json.getComment() != null) {
                if (comma) {
                    sb.append(", ");
                }
                comma = true;
                sb.append("\"comment\": \"").append(json.getComment()).append('\"');
            }

            if (json.getExt() != null) {
                if (comma) {
                    sb.append(", ");
                }
                comma = true;
                sb.append("\"ext\": ").append(json.getExt().toString());
            }
            return sb.append('}').toString();
        }
    };

    @Test
    public void testSimpleSerializeParse() throws ParseException {
        String signer = "alice.pub";
        Verification.JSON json = new Verification.JSON(signer);

        String string = dummySerializer.serialize(json);
        assertEquals("{\"signers\": [\"alice.pub\"]}", string);

        Verification.JSON parsed = dummyParser.parse(string);
        assertEquals(signer, parsed.getSigners().get(0));
        assertEquals(1, parsed.getSigners().size());
        assertNull(parsed.getComment());
        assertNull(parsed.getExt());
    }

    @Test
    public void testAdvancedSerializeParse() throws ParseException {
        Verification.JSON json = new Verification.JSON(
                Arrays.asList("../certs/alice.pub", "/etc/pgp/certs.pgp"),
                "This is a comment",
                "{\"Foo\": \"Bar\"}");

        String serialized = dummySerializer.serialize(json);
        assertEquals("{\"signers\": [\"../certs/alice.pub\", \"/etc/pgp/certs.pgp\"], \"comment\": \"This is a comment\", \"ext\": {\"Foo\": \"Bar\"}}",
                serialized);

        Verification.JSON parsed = dummyParser.parse(serialized);
        assertEquals(json.getSigners(), parsed.getSigners());
        assertEquals(json.getComment(), parsed.getComment());
        assertEquals(json.getExt(), parsed.getExt());
    }

    @Test
    public void testVerificationWithSimpleJson() {
        String string = "2019-10-29T18:36:45Z EB85BB5FA33A75E15E944E63F231550C4F47E38E EB85BB5FA33A75E15E944E63F231550C4F47E38E mode:text {\"signers\": [\"alice.pgp\"]}";
        Verification verification = Verification.fromString(string);

        assertTrue(verification.getContainsJson());
        assertEquals("EB85BB5FA33A75E15E944E63F231550C4F47E38E", verification.getSigningKeyFingerprint());
        assertEquals("EB85BB5FA33A75E15E944E63F231550C4F47E38E", verification.getSigningCertFingerprint());
        assertEquals(SignatureMode.text, verification.getSignatureMode().get());

        Verification.JSON json = verification.getJson(dummyParser);
        assertNotNull(json, "The verification string MUST contain valid extension json");

        assertEquals(Collections.singletonList("alice.pgp"), json.getSigners());
        assertNull(json.getComment());
        assertNull(json.getExt());

        verification = new Verification(verification.getCreationTime(), verification.getSigningKeyFingerprint(), verification.getSigningCertFingerprint(), verification.getSignatureMode().get(), json, dummySerializer);
        assertEquals(string, verification.toString());
    }
}
