// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import sop.Verification;
import sop.util.UTCUtil;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

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

    public static void assertSignedBy(List<Verification> verifications, String primaryFingerprint) {
        for (Verification verification : verifications) {
            if (verification.getSigningCertFingerprint().equals(primaryFingerprint)) {
                return;
            }
        }

        if (verifications.isEmpty()) {
            fail("Verification list is empty.");
        }

        fail("Verification list does not contain verification by cert " + primaryFingerprint + ":\n" +
                Arrays.toString(verifications.toArray(new Verification[0])));
    }

    public static void assertSignedBy(List<Verification> verifications, String signingFingerprint, String primaryFingerprint) {
        for (Verification verification : verifications) {
            if (verification.getSigningCertFingerprint().equals(primaryFingerprint) && verification.getSigningKeyFingerprint().equals(signingFingerprint)) {
                return;
            }
        }

        if (verifications.isEmpty()) {
            fail("Verification list is empty.");
        }

        fail("Verification list does not contain verification by key " + signingFingerprint + " on cert " + primaryFingerprint + ":\n" +
                Arrays.toString(verifications.toArray(new Verification[0])));
    }

    public static void assertSignedBy(List<Verification> verifications, String primaryFingerprint, Date signatureDate) {
        for (Verification verification : verifications) {
            if (verification.getSigningCertFingerprint().equals(primaryFingerprint) &&
                    verification.getCreationTime().equals(signatureDate)) {
                return;
            }
        }

        if (verifications.isEmpty()) {
            fail("Verification list is empty.");
        }

        fail("Verification list does not contain verification by cert " + primaryFingerprint + " made at " + UTCUtil.formatUTCDate(signatureDate) + ":\n" +
                Arrays.toString(verifications.toArray(new Verification[0])));
    }

    public static void assertSignedBy(List<Verification> verifications, String signingFingerprint, String primaryFingerprint, Date signatureDate) {
        for (Verification verification : verifications) {
            if (verification.getSigningCertFingerprint().equals(primaryFingerprint) &&
                    verification.getSigningKeyFingerprint().equals(signingFingerprint) &&
                    verification.getCreationTime().equals(signatureDate)) {
                return;
            }
        }

        if (verifications.isEmpty()) {
            fail("Verification list is empty.");
        }

        fail("Verification list does not contain verification by key" + signingFingerprint + " on cert " + primaryFingerprint + " made at " + UTCUtil.formatUTCDate(signatureDate) + ":\n" +
                Arrays.toString(verifications.toArray(new Verification[0])));
    }
}
