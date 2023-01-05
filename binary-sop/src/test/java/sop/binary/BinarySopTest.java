// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.binary;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import sop.SOP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("sop.binary.BinarySopTest#sopBinaryInstalled")
public class BinarySopTest {

    private static final String BINARY = "/usr/bin/sqop";

    private final SOP sop = new BinarySop(BINARY);

    public static boolean sopBinaryInstalled() {
        return new File(BINARY).exists();
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
