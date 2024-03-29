// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import org.junit.jupiter.api.Test;
import sop.exception.SOPGPException;
import sop.util.UTCUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadyWithResultTest {

    @Test
    public void testReadyWithResult() throws SOPGPException.NoSignature, IOException, ParseException {
        byte[] data = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        List<Verification> result = Collections.singletonList(
                new Verification(UTCUtil.parseUTCDate("2019-10-24T23:48:29Z"),
                        "C90E6D36200A1B922A1509E77618196529AE5FF8",
                        "C4BC2DDB38CCE96485EBE9C2F20691179038E5C6")
        );
        ReadyWithResult<List<Verification>> readyWithResult = new ReadyWithResult<List<Verification>>() {
            @Override
            public List<Verification> writeTo(OutputStream outputStream) throws IOException, SOPGPException.NoSignature {
                outputStream.write(data);
                return result;
            }
        };

        ByteArrayAndResult<List<Verification>> bytesAndResult = readyWithResult.toByteArrayAndResult();
        assertArrayEquals(data, bytesAndResult.getBytes());
        assertEquals(result, bytesAndResult.getResult());
    }
}
