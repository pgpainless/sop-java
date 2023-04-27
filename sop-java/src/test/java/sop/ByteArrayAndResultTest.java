// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import org.junit.jupiter.api.Test;
import sop.util.UTCUtil;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteArrayAndResultTest {

    @Test
    public void testCreationAndGetters() throws ParseException {
        byte[] bytes = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        List<Verification> result = Collections.singletonList(
                new Verification(UTCUtil.parseUTCDate("2019-10-24T23:48:29Z"),
                        "C90E6D36200A1B922A1509E77618196529AE5FF8",
                        "C4BC2DDB38CCE96485EBE9C2F20691179038E5C6")
        );
        ByteArrayAndResult<List<Verification>> bytesAndResult = new ByteArrayAndResult<>(bytes, result);

        assertArrayEquals(bytes, bytesAndResult.getBytes());
        assertEquals(result, bytesAndResult.getResult());
    }
}
