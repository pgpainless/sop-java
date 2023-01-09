// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("sop.external.AbstractExternalSOPTest#isExternalSopInstalled")
public class ExternalExtractCertTest extends AbstractExternalSOPTest {

    @Test
    public void extractCertTest() throws IOException {
        InputStream keyIn = getSop().generateKey().userId("Alice").generate().getInputStream();
        String cert = new String(getSop().extractCert().key(keyIn).getBytes());
        assertTrue(cert.startsWith("-----BEGIN PGP PUBLIC KEY BLOCK-----\n"));
    }
}
