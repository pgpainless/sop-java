// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import org.junit.jupiter.api.Test;
import sop.util.UTCUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ByteArrayAndResultTest {

    @Test
    public void testCreationAndGetters() throws ParseException {
        byte[] bytes = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        List<Verification> result = Collections.singletonList(
                new Verification(UTCUtil.parseUTCDate("2019-10-24T23:48:29Z"),
                        "C90E6D36200A1B922A1509E77618196529AE5FF8",
                        "C4BC2DDB38CCE96485EBE9C2F20691179038E5C6")
        );
        ByteArrayAndResult<List<Verification>> bytesAndResult = new ByteArrayAndResult<>(bytes, result);

        assertArrayEquals(bytes, bytesAndResult.getBytes());
        assertEquals(result, bytesAndResult.getResult());
    }

    @Test
    public void testInputStream() throws IOException {
        String string = "Hello, World!\n";
        ByteArrayAndResult<String> bytesAndResult = new ByteArrayAndResult<>(string.getBytes(StandardCharsets.UTF_8), string);
        InputStream inputStream = bytesAndResult.getInputStream();
        try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
            int i;
            while ((i = inputStream.read()) != -1) {
                bOut.write(i);
            }
            assertEquals(string, bOut.toString());
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        String s1 = "Hello, World!\n";
        String s2 = "Foo Bar!\n";

        ByteArrayAndResult<String> b11 = new ByteArrayAndResult<>(s1.getBytes(StandardCharsets.UTF_8), s1);
        ByteArrayAndResult<String> b22 = new ByteArrayAndResult<>(s2.getBytes(StandardCharsets.UTF_8), s2);
        ByteArrayAndResult<String> b12 = new ByteArrayAndResult<>(s1.getBytes(StandardCharsets.UTF_8), s2);
        ByteArrayAndResult<String> b21 = new ByteArrayAndResult<>(s2.getBytes(StandardCharsets.UTF_8), s1);

        assertEquals(b11, b11);
        assertSame(b11, b11);
        assertNotEquals(b11, b22);
        assertNotEquals(b11, s1);
        assertNotEquals(b12, b11);
        assertNotEquals(b21, b11);
        assertFalse(b12.equals(b11));
        assertFalse(b12.equals(b22));
        assertTrue(b11.equals(b11));
        ByteArrayAndResult<String> b11_ = new ByteArrayAndResult<>(s1.getBytes(StandardCharsets.UTF_8), s1);
        assertEquals(b11, b11_);
        assertTrue(b11.equals(b11_));
        assertFalse(b11.equals(null));
        assertEquals(b11.hashCode(), b11_.hashCode());
    }

    @Test
    public void testNullableResult() {
        ByteArrayAndResult<String> b = new ByteArrayAndResult<>("Hello".getBytes(StandardCharsets.UTF_8), null);
        assertNull(b.getResult());
        ByteArrayAndResult<String> b2 = new ByteArrayAndResult<>("Hello".getBytes(StandardCharsets.UTF_8), null);
        assertEquals(b, b2);
        assertEquals(b.hashCode(), b2.hashCode());
    }
}
