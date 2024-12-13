// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite

import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler
import sop.exception.SOPGPException

class AbortOnUnsupportedOptionExtension : TestExecutionExceptionHandler {
    override fun handleTestExecutionException(context: ExtensionContext, throwable: Throwable) {
        val testClass = context.requiredTestClass
        val annotation = testClass.getAnnotation(AbortOnUnsupportedOption::class.java)
        if (annotation != null && SOPGPException.UnsupportedOption::class.isInstance(throwable)) {
            Assumptions.assumeTrue(false, "Test aborted due to: " + throwable.message)
        }
        throw throwable
    }
}
