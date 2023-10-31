// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.util

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

class UTF8Util {
    companion object {
        @JvmField val UTF8: Charset = Charset.forName("UTF8")

        @JvmStatic
        private val UTF8Decoder =
            UTF8.newDecoder()
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .onMalformedInput(CodingErrorAction.REPORT)

        /**
         * Detect non-valid UTF8 data.
         *
         * @param data utf-8 encoded bytes
         * @return decoded string
         * @throws CharacterCodingException if the input data does not resemble UTF8
         * @see [ante on StackOverflow](https://stackoverflow.com/a/1471193)
         */
        @JvmStatic
        @Throws(CharacterCodingException::class)
        fun decodeUTF8(data: ByteArray): String {
            val byteBuffer = ByteBuffer.wrap(data)
            val charBuffer = UTF8Decoder.decode(byteBuffer)
            return charBuffer.toString()
        }
    }
}
