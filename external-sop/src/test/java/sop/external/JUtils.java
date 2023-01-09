// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.fail;

public class JUtils {

    public static boolean arrayStartsWith(byte[] array, byte[] start) {
        return arrayStartsWith(array, start, 0);
    }

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

    public static void assertArrayStartsWith(byte[] array, byte[] start) {
        if (!arrayStartsWith(array, start)) {
            byte[] actual = new byte[Math.min(start.length, array.length)];
            System.arraycopy(array, 0, actual, 0, actual.length);
            fail("Array does not start with expected bytes.\n" +
                    "Expected: <" + Arrays.toString(start) + ">\n" +
                    "Actual: <" + Arrays.toString(actual) + ">");
        }
    }

    public static void assertArrayStartsWith(byte[] array, byte[] start, int offset) {
        if (!arrayStartsWith(array, start, offset)) {
            byte[] actual = new byte[Math.min(start.length, array.length - offset)];
            System.arraycopy(array, offset, actual, 0, actual.length);
            fail("Array does not start with expected bytes at offset " + offset + ".\n" +
                    "Expected: <" + Arrays.toString(start) + ">\n" +
                    "Actual: <" + Arrays.toString(actual) + ">");
        }
    }
}
