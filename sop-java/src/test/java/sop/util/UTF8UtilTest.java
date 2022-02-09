// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.util;

import org.junit.jupiter.api.Test;
import sop.exception.SOPGPException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UTF8UtilTest {

    @Test
    public void testValidUtf8Decoding() {
        String utf8String = "Hello, World\n";
        String decoded = UTF8Util.decodeUTF8(utf8String.getBytes(StandardCharsets.UTF_8));

        assertEquals(utf8String, decoded);
    }

    /**
     * Test detection of non-uft8 data.
     * @see <a href="https://www.cl.cam.ac.uk/~mgk25/ucs/examples/UTF-8-test.txt">
     *     Markus Kuhn's UTF8 decoder capability and stress test file</a>
     */
    @Test
    public void testInvalidUtf8StringThrows() {
        assertThrows(SOPGPException.PasswordNotHumanReadable.class,
                () -> UTF8Util.decodeUTF8(new byte[] {(byte) 0xa0, (byte) 0xa1}));
        assertThrows(SOPGPException.PasswordNotHumanReadable.class,
                () -> UTF8Util.decodeUTF8(new byte[] {(byte) 0xc0, (byte) 0xaf}));
        assertThrows(SOPGPException.PasswordNotHumanReadable.class,
                () -> UTF8Util.decodeUTF8(new byte[] {(byte) 0x80, (byte) 0xbf}));
    }
}
