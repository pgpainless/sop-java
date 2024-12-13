// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.assertions;

import sop.exception.SOPGPException;

import java.util.function.IntSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * DSL for testing the return values of SOP method calls.
 */
public class SopExecutionAssertions {

    /**
     * Assert that the execution of the given function returns 0.
     *
     * @param function function to execute
     */
    public static void assertSuccess(IntSupplier function) {
        assertEquals(0, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns a generic error with error code 1.
     *
     * @param function function to execute.
     */
    public static void assertGenericError(IntSupplier function) {
        assertEquals(1, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns a non-zero error code.
     *
     * @param function function to execute
     */
    public static void assertAnyError(IntSupplier function) {
        assertNotEquals(0, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 3
     * (which corresponds to {@link sop.exception.SOPGPException.NoSignature}).
     *
     * @param function function to execute.
     */
    public static void assertNoSignature(IntSupplier function) {
        assertEquals(SOPGPException.NoSignature.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 13
     * (which corresponds to {@link sop.exception.SOPGPException.UnsupportedAsymmetricAlgo}).
     *
     * @param function function to execute.
     */
    public static void assertUnsupportedAsymmetricAlgo(IntSupplier function) {
        assertEquals(SOPGPException.UnsupportedAsymmetricAlgo.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 17
     * (which corresponds to {@link sop.exception.SOPGPException.CertCannotEncrypt}).
     *
     * @param function function to execute.
     */
    public static void assertCertCannotEncrypt(IntSupplier function) {
        assertEquals(SOPGPException.CertCannotEncrypt.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 19
     * (which corresponds to {@link sop.exception.SOPGPException.MissingArg}).
     *
     * @param function function to execute.
     */
    public static void assertMissingArg(IntSupplier function) {
        assertEquals(SOPGPException.MissingArg.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 23
     * (which corresponds to {@link sop.exception.SOPGPException.IncompleteVerification}).
     *
     * @param function function to execute.
     */
    public static void assertIncompleteVerification(IntSupplier function) {
        assertEquals(SOPGPException.IncompleteVerification.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 29
     * (which corresponds to {@link sop.exception.SOPGPException.CannotDecrypt}).
     *
     * @param function function to execute.
     */
    public static void assertCannotDecrypt(IntSupplier function) {
        assertEquals(SOPGPException.CannotDecrypt.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 31
     * (which corresponds to {@link sop.exception.SOPGPException.PasswordNotHumanReadable}).
     *
     * @param function function to execute.
     */
    public static void assertPasswordNotHumanReadable(IntSupplier function) {
        assertEquals(SOPGPException.PasswordNotHumanReadable.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 37
     * (which corresponds to {@link sop.exception.SOPGPException.UnsupportedOption}).
     *
     * @param function function to execute.
     */
    public static void assertUnsupportedOption(IntSupplier function) {
        assertEquals(SOPGPException.UnsupportedOption.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 41
     * (which corresponds to {@link sop.exception.SOPGPException.BadData}).
     *
     * @param function function to execute.
     */
    public static void assertBadData(IntSupplier function) {
        assertEquals(SOPGPException.BadData.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 53
     * (which corresponds to {@link sop.exception.SOPGPException.ExpectedText}).
     *
     * @param function function to execute.
     */
    public static void assertExpectedText(IntSupplier function) {
        assertEquals(SOPGPException.ExpectedText.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 59
     * (which corresponds to {@link sop.exception.SOPGPException.OutputExists}).
     *
     * @param function function to execute.
     */
    public static void assertOutputExists(IntSupplier function) {
        assertEquals(SOPGPException.OutputExists.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 61
     * (which corresponds to {@link sop.exception.SOPGPException.MissingInput}).
     *
     * @param function function to execute.
     */
    public static void assertMissingInput(IntSupplier function) {
        assertEquals(SOPGPException.MissingInput.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 67
     * (which corresponds to {@link sop.exception.SOPGPException.KeyIsProtected}).
     *
     * @param function function to execute.
     */
    public static void assertKeyIsProtected(IntSupplier function) {
        assertEquals(SOPGPException.KeyIsProtected.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 69
     * (which corresponds to {@link sop.exception.SOPGPException.UnsupportedSubcommand}).
     *
     * @param function function to execute.
     */
    public static void assertUnsupportedSubcommand(IntSupplier function) {
        assertEquals(SOPGPException.UnsupportedSubcommand.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 71
     * (which corresponds to {@link sop.exception.SOPGPException.UnsupportedSpecialPrefix}).
     *
     * @param function function to execute.
     */
    public static void assertUnsupportedSpecialPrefix(IntSupplier function) {
        assertEquals(SOPGPException.UnsupportedSpecialPrefix.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 73
     * (which corresponds to {@link sop.exception.SOPGPException.AmbiguousInput}).
     *
     * @param function function to execute.
     */
    public static void assertAmbiguousInput(IntSupplier function) {
        assertEquals(SOPGPException.AmbiguousInput.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 79
     * (which corresponds to {@link sop.exception.SOPGPException.KeyCannotSign}).
     *
     * @param function function to execute.
     */
    public static void assertKeyCannotSign(IntSupplier function) {
        assertEquals(SOPGPException.KeyCannotSign.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 83
     * (which corresponds to {@link sop.exception.SOPGPException.IncompatibleOptions}).
     *
     * @param function function to execute.
     */
    public static void assertIncompatibleOptions(IntSupplier function) {
        assertEquals(SOPGPException.IncompatibleOptions.EXIT_CODE, function.getAsInt());
    }

    /**
     * Assert that the execution of the given function returns error code 89
     * (which corresponds to {@link sop.exception.SOPGPException.UnsupportedProfile}).
     *
     * @param function function to execute.
     */
    public static void assertUnsupportedProfile(IntSupplier function) {
        assertEquals(SOPGPException.UnsupportedProfile.EXIT_CODE, function.getAsInt());
    }
}
