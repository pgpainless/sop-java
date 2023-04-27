// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

public class UTF8Util {

    public static final Charset UTF8 = Charset.forName("UTF8");
    private static final CharsetDecoder UTF8Decoder = UTF8
            .newDecoder()
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .onMalformedInput(CodingErrorAction.REPORT);

    /**
     * Detect non-valid UTF8 data.
     *
     * @see <a href="https://stackoverflow.com/a/1471193">ante on StackOverflow</a>
     * @param data utf-8 encoded bytes
     *
     * @return decoded string
     * @throws CharacterCodingException if the input data does not resemble UTF8
     */
    public static String decodeUTF8(byte[] data)
            throws CharacterCodingException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        CharBuffer charBuffer = UTF8Decoder.decode(byteBuffer);
        return charBuffer.toString();
    }
}
