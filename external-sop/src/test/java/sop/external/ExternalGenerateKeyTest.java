// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalGenerateKeyTest extends AbstractExternalSOPTest {

    @Test
    public void generateKeyTest() throws IOException {
        String key = new String(getSop().generateKey().userId("Alice").generate().getBytes());
        assertTrue(key.startsWith("-----BEGIN PGP PRIVATE KEY BLOCK-----\n"));
    }

    @Test
    public void generateKeyWithPasswordTest() throws IOException {
        String key = new String(getSop().generateKey().userId("Alice").withKeyPassword("swßrdf1sh").generate().getBytes());
        assertEquals("asd", key);
    }

}
