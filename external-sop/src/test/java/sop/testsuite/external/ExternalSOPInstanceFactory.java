// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.external;

import com.google.gson.Gson;
import sop.SOP;
import sop.external.ExternalSOP;
import sop.testsuite.SOPInstanceFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This implementation of {@link SOPInstanceFactory} reads the JSON file at
 * <pre>external-sop/src/main/resources/sop/testsuite/external/config.json</pre>
 * to determine configured external test backends
 */
public class ExternalSOPInstanceFactory extends SOPInstanceFactory {

    @Override
    public Map<String, SOP> provideSOPInstances() {
        Map<String, SOP> backends = new HashMap<>();
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
                backends.put(subject.name, sop);
            }
        }
        return backends;
    }


    public static TestSuite readConfiguration() {
        Gson gson = new Gson();
        InputStream inputStream = ExternalSOPInstanceFactory.class.getResourceAsStream("config.json");
        if (inputStream == null) {
            return null;
        }

        InputStreamReader reader = new InputStreamReader(inputStream);
        return gson.fromJson(reader, TestSuite.class);
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
