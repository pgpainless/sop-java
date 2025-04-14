// SPDX-FileCopyrightText: 2025 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import sop.exception.SOPGPException;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class AbortOnUnsupportedOptionExtension implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        Class<?> testClass = extensionContext.getRequiredTestClass();
        Annotation annotation = testClass.getAnnotation(AbortOnUnsupportedOption.class);
        if (annotation != null && throwable instanceof SOPGPException.UnsupportedOption) {
            assumeTrue(false, "Test aborted due to: " + throwable.getMessage());
        }
        throw throwable;
    }
}
