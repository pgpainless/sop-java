// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.util;

import sop.exception.SOPGPException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

public class UTF8Util {

    private static final CharsetDecoder UTF8Decoder = Charset.forName("UTF8")
            .newDecoder()
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .onMalformedInput(CodingErrorAction.REPORT);

    /**
     * Detect non-valid UTF8 data.
     *
     * @see <a href="https://stackoverflow.com/a/1471193">ante on StackOverflow</a>
     * @param data
     * @return
     */
    public static String decodeUTF8(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        try {
            CharBuffer charBuffer = UTF8Decoder.decode(byteBuffer);
            return charBuffer.toString();
        } catch (CharacterCodingException e) {
            throw new SOPGPException.PasswordNotHumanReadable();
        }
    }
}
