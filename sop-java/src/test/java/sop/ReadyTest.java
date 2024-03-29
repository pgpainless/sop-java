// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ReadyTest {

    @Test
    public void readyTest() throws IOException {
        byte[] data = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        Ready ready = new Ready() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                outputStream.write(data);
            }
        };

        assertArrayEquals(data, ready.getBytes());
    }
}
