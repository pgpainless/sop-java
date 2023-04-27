// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SigningResultTest {

    @Test
    public void basicBuilderTest() {
        SigningResult result = SigningResult.builder()
                .setMicAlg(MicAlg.fromHashAlgorithmId(10))
                .build();

        assertEquals("pgp-sha512", result.getMicAlg().getMicAlg());
    }
}
