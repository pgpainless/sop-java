// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sop.SOP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("sop.external.ExternalSOPTest#externalSopInstalled")
public class ExternalSOPTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalSOPTest.class);

    private final SOP sop;

    public ExternalSOPTest() {
        String backend = readSopBackendFromProperties();
        sop = new ExternalSOP(backend);
    }

    private static String readSopBackendFromProperties() {
        Properties properties = new Properties();
        try {
            InputStream resourceIn = ExternalSOPTest.class.getResourceAsStream("backend.local.properties");
            if (resourceIn == null) {
                LOGGER.info("Could not find backend.local.properties file. Try backend.properties instead.");
                resourceIn = ExternalSOPTest.class.getResourceAsStream("backend.properties");
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

    public static boolean externalSopInstalled() {
        String binary = readSopBackendFromProperties();
        if (binary == null) {
            return false;
        }
        return new File(binary).exists();
    }

    @Test
    public void versionNameTest() {
        assertEquals("sqop", sop.version().getName());
    }

    @Test
    public void versionVersionTest() {
        String version = sop.version().getVersion();
        assertTrue(version.matches("\\d+(\\.\\d+)*"));
    }

    @Test
    public void backendVersionTest() {
        String backend = sop.version().getBackendVersion();
        assertFalse(backend.isEmpty());
    }

    @Test
    public void extendedVersionTest() {
        String extended = sop.version().getExtendedVersion();
        assertFalse(extended.isEmpty());
    }

    @Test
    public void generateKeyTest() throws IOException {
        String key = new String(sop.generateKey().userId("Alice").generate().getBytes());
        assertTrue(key.startsWith("-----BEGIN PGP PRIVATE KEY BLOCK-----\n"));
    }

    @Test
    @Disabled
    public void extractCertTest() throws IOException {
        InputStream keyIn = sop.generateKey().userId("Alice").generate().getInputStream();
        String cert = new String(sop.extractCert().key(keyIn).getBytes());
        assertTrue(cert.startsWith("-----BEGIN PGP PUBLIC KEY BLOCK-----\n"));
    }
}
