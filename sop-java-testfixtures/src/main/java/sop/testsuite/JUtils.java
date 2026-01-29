// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite;

import sop.util.UTCUtil;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Contains utility functions for JUnit tests.
 */
public class JUtils {

    /**
     * Return true, if the given <pre>array</pre> starts with <pre>start</pre>.
     *
     * @param array array
     * @param start start
     * @return true if array starts with start, false otherwise
     */
    public static boolean arrayStartsWith(byte[] array, byte[] start) {
        return arrayStartsWith(array, start, 0);
    }

    /**
     * Return true, if the given <pre>array</pre> contains the given <pre>start</pre> at offset <pre>offset</pre>.
     *
     * @param array array
     * @param start start
     * @param offset offset
     * @return true, if array contains start at offset, false otherwise
     */
    public static boolean arrayStartsWith(byte[] array, byte[] start, int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative");
        }

        if (start.length + offset > array.length) {
            return false;
        }

        for (int i = 0; i < start.length; i++) {
            if (array[offset + i] != start[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Assert that the given <pre>array</pre> starts with <pre>start</pre>.
     *
     * @param array array
     * @param start start
     */
    public static void assertArrayStartsWith(byte[] array, byte[] start) {
        if (!arrayStartsWith(array, start)) {
            byte[] actual = new byte[Math.min(start.length, array.length)];
            System.arraycopy(array, 0, actual, 0, actual.length);
            fail("Array does not start with expected bytes.\n" +
                    "Expected: <" + Arrays.toString(start) + ">\n" +
                    "Actual: <" + Arrays.toString(actual) + ">");
        }
    }

    /**
     * Assert that the given <pre>array</pre> contains <pre>start</pre> at <pre>offset</pre>.
     *
     * @param array array
     * @param start start
     * @param offset offset
     */
    public static void assertArrayStartsWith(byte[] array, byte[] start, int offset) {
        if (!arrayStartsWith(array, start, offset)) {
            byte[] actual = new byte[Math.min(start.length, array.length - offset)];
            System.arraycopy(array, offset, actual, 0, actual.length);
            fail("Array does not start with expected bytes at offset " + offset + ".\n" +
                    "Expected: <" + Arrays.toString(start) + ">\n" +
                    "Actual: <" + Arrays.toString(actual) + ">");
        }
    }

    /**
     * Returns true if the given <pre>array</pre> ends with the given <pre>end</pre> bytes.
     *
     * @param array array to examine
     * @param end expected ending bytes
     * @return true if array ends with end
     */
    public static boolean arrayEndsWith(byte[] array, byte[] end) {
        return arrayEndsWith(array, end, 0);
    }

    /**
     * Returns true if the given <pre>array</pre> ends with the given <pre>end</pre> bytes.
     *
     * @param array array to examine
     * @param end expected ending bytes
     * @param offset from the end
     * @return true if array ends with end
     */
    public static boolean arrayEndsWith(byte[] array, byte[] end, int offset) {
        if (end.length + offset > array.length) {
            return false;
        }

        int arrOff = array.length - end.length - offset;
        for (int i = 0; i < end.length; i++) {
            if (end[i] != array[arrOff + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Assert hat the given array ends with the given end bytes.
     *
     * @param array array
     * @param end ending bytes
     */
    public static void assertArrayEndsWith(byte[] array, byte[] end) {
        assertArrayEndsWith(array, end, 0);
    }

    /**
     * Assert hat the given array contains the given end bytes at the end, shifted by offset.
     *
     * @param array array
     * @param end ending bytes
     * @param offset offset from the end
     */
    public static void assertArrayEndsWith(byte[] array, byte[] end, int offset) {
        if (!arrayEndsWith(array, end, offset)) {
            byte[] actual = new byte[Math.min(end.length, array.length - offset)];
            System.arraycopy(array, array.length - actual.length, actual, 0, actual.length);
            fail("Array does not end with the expected bytes.\n" +
                    "Expected: <" + Arrays.toString(end) + ">\n" +
                    "Actual: <" + Arrays.toString(actual) + ">");
        }
    }

    /**
     * Assert hat the given array contains the given end bytes at the end, ignoring new lines.
     *
     * @param array array
     * @param end ending bytes
     */
    public static void assertArrayEndsWithIgnoreNewlines(byte[] array, byte[] end) {
        int offset = 0;
        while (offset < array.length && array[array.length - 1 - offset] == (byte) 10) {
            offset++;
        }

        assertArrayEndsWith(array, end, offset);
    }

    /**
     * Assert equality of the given two ascii armored byte arrays, ignoring armor header lines.
     *
     * @param first first ascii armored bytes
     * @param second second ascii armored bytes
     */
    public static void assertAsciiArmorEquals(byte[] first, byte[] second) {
        byte[] firstCleaned = removeArmorHeaders(first);
        byte[] secondCleaned = removeArmorHeaders(second);

        assertArrayEquals(firstCleaned, secondCleaned);
    }

    /**
     * Remove armor headers "Comment:", "Version:", "MessageID:", "Hash:" and "Charset:" along with their values
     * from the given ascii armored byte array.
     *
     * @param armor ascii armored byte array
     * @return ascii armored byte array with header lines removed
     */
    public static byte[] removeArmorHeaders(byte[] armor) {
        String string = new String(armor, StandardCharsets.UTF_8);
        string = string.replaceAll("Comment: .+\\R", "")
                .replaceAll("Version: .+\\R", "")
                .replaceAll("MessageID: .+\\R", "")
                .replaceAll("Hash: .+\\R", "")
                .replaceAll("Charset: .+\\R", "");
        return string.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Assert that the expected date equals the actual instance.
     *
     * @param expected expected date, non-null
     * @param actual actual date, non-null
     */
    public static void assertDateEquals(Date expected, Date actual) {
        assertEquals(UTCUtil.formatUTCDate(expected), UTCUtil.formatUTCDate(actual));
    }

    /**
     * Returns true if the actual date equals the expected date.
     *
     * @param expected expected data, non-null
     * @param actual actual date, non-null
     * @return true if expected equals actual
     */
    public static boolean dateEquals(Date expected, Date actual) {
        return UTCUtil.formatUTCDate(expected).equals(UTCUtil.formatUTCDate(actual));
    }

}
