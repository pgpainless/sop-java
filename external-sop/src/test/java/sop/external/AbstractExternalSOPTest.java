// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sop.SOP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractExternalSOPTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExternalSOPTest.class);

    private final SOP sop;

    public AbstractExternalSOPTest() {
        String backend = readSopBackendFromProperties();
        Properties environment = readBackendEnvironment();
        sop = new ExternalSOP(backend, environment);
    }

    public SOP getSop() {
        return sop;
    }

    public static boolean isExternalSopInstalled() {
        String binary = readSopBackendFromProperties();
        if (binary == null) {
            return false;
        }
        return new File(binary).exists();
    }

    private static String readSopBackendFromProperties() {
        Properties properties = new Properties();
        try {
            InputStream resourceIn = AbstractExternalSOPTest.class.getResourceAsStream("backend.local.properties");
            if (resourceIn == null) {
                LOGGER.info("Could not find backend.local.properties file. Try backend.properties instead.");
                resourceIn = AbstractExternalSOPTest.class.getResourceAsStream("backend.properties");
            }
            if (resourceIn == null) {
                throw new FileNotFoundException("Could not find backend.properties file.");
            }

            properties.load(resourceIn);
            String backend = properties.getProperty("sop.backend");
            return backend;
        } catch (IOException e) {
            return null;
        }
    }

    protected static Properties readBackendEnvironment() {
        Properties properties = new Properties();
        try {
            InputStream resourceIn = AbstractExternalSOPTest.class.getResourceAsStream("backend.env");
            if (resourceIn == null) {
                LOGGER.info("Could not read backend.env file.");
            } else {
                properties.load(resourceIn);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
