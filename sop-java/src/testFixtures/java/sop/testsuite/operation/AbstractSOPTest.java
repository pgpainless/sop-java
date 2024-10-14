// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import sop.SOP;
import sop.testsuite.AbortOnUnsupportedOption;
import sop.testsuite.AbortOnUnsupportedOptionExtension;
import sop.testsuite.SOPInstanceFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ExtendWith(AbortOnUnsupportedOptionExtension.class)
@AbortOnUnsupportedOption
public abstract class AbstractSOPTest {

    private static final List<Arguments> backends = new ArrayList<>();

    static {
        initBackends();
    }

    // populate instances list via configured test subject factory
    private static void initBackends() {
        String factoryName = System.getenv("test.implementation");
        if (factoryName == null) {
            return;
        }

        SOPInstanceFactory factory;
        try {
            Class<?> testSubjectFactoryClass = Class.forName(factoryName);
            factory = (SOPInstanceFactory) testSubjectFactoryClass
                    .getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        Map<String, SOP> testSubjects = factory.provideSOPInstances();
        for (String key : testSubjects.keySet()) {
            backends.add(Arguments.of(Named.of(key, testSubjects.get(key))));
        }
    }

    public static Stream<Arguments> provideBackends() {
        return backends.stream();
    }

    public static boolean hasBackends() {
        return !backends.isEmpty();
    }

}
