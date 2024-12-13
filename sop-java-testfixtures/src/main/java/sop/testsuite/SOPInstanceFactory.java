// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite;

import sop.SOP;

import java.util.Map;

/**
 * Factory class to instantiate SOP implementations for testing.
 * Overwrite this class and the {@link #provideSOPInstances()} method to return the SOP instances you want
 * to test.
 * Then, add the following line to your <pre>build.gradle</pre> files <pre>dependencies</pre> section:
 * <pre>{@code
 *     testImplementation(testFixtures("org.pgpainless:sop-java:<version>"))
 * }</pre>
 * To inject the factory class into the test suite, add the following line to your modules <pre>test</pre> task:
 * <pre>{@code
 *     environment("test.implementation", "org.example.YourTestSubjectFactory")
 * }</pre>
 * Next, in your <pre>test</pre> sources, extend all test classes from the <pre>testFixtures</pre>
 * <pre>sop.operation</pre> package.
 * Take a look at the <pre>external-sop</pre> module for an example.
 */
public abstract class SOPInstanceFactory {

    public abstract Map<String, SOP> provideSOPInstances();
}
