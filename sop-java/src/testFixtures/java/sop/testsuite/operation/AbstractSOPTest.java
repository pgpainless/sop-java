// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import sop.SOP;
import sop.testsuite.SOPInstanceFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractSOPTest {

    private static final List<Arguments> backends = new ArrayList<>();

    static {
        // populate instances list via configured test subject factory
        String factoryName = System.getenv("test.implementation");
        if (factoryName != null) {
            try {
                Class testSubjectFactoryClass = Class.forName(factoryName);
                SOPInstanceFactory factory = (SOPInstanceFactory) testSubjectFactoryClass.newInstance();
                Map<String, SOP> testSubjects = factory.provideSOPInstances();

                for (String key : testSubjects.keySet()) {
                    backends.add(Arguments.of(Named.of(key, testSubjects.get(key))));
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Stream<Arguments> provideBackends() {
        return backends.stream();
    }

    public static boolean hasBackends() {
        return !backends.isEmpty();
    }

}
