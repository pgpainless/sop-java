// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import com.google.gson.Gson;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import sop.SOP;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public abstract class AbstractExternalSOPTest {

    private static final List<Arguments> backends = new ArrayList<>();

    static {
        TestSuite suite = readConfiguration();
        if (suite != null && !suite.backends.isEmpty()) {
            for (TestSubject subject : suite.backends) {
                if (!new File(subject.sop).exists()) {
                    continue;
                }

                Properties env = new Properties();
                if (subject.env != null) {
                    for (Var var : subject.env) {
                        env.put(var.key, var.value);
                    }
                }

                SOP sop = new ExternalSOP(subject.sop, env);
                backends.add(Arguments.of(Named.of(subject.name, sop)));
            }
        }
    }

    public static Stream<Arguments> provideBackends() {
        return backends.stream();
    }

    public static TestSuite readConfiguration() {
        Gson gson = new Gson();
        InputStream inputStream = AbstractExternalSOPTest.class.getResourceAsStream("config.json");
        if (inputStream == null) {
            return null;
        }

        InputStreamReader reader = new InputStreamReader(inputStream);
        TestSuite suite = gson.fromJson(reader, TestSuite.class);
        return suite;
    }

    public static boolean hasBackends() {
        return !backends.isEmpty();
    }

    // JSON DTOs

    public static class TestSuite {
        List<TestSubject> backends;
    }

    public static class TestSubject {
        String name;
        String sop;
        List<Var> env;
    }

    public static class Var {
        String key;
        String value;
    }
}
