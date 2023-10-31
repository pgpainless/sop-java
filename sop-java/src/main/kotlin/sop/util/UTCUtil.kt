// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class UTCUtil {

    companion object {

        @JvmField val UTC_FORMATTER = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        @JvmField
        val UTC_PARSERS =
            arrayOf(
                    UTC_FORMATTER,
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"),
                    SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'"),
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"))
                .onEach { fmt -> fmt.timeZone = TimeZone.getTimeZone("UTC") }

        /**
         * Parse an ISO-8601 UTC timestamp from a string.
         *
         * @param dateString string
         * @return date
         * @throws ParseException if the date string is malformed and cannot be parsed
         */
        @JvmStatic
        @Throws(ParseException::class)
        fun parseUTCDate(dateString: String): Date {
            var exception: ParseException? = null
            for (parser in UTC_PARSERS) {
                try {
                    return parser.parse(dateString)
                } catch (e: ParseException) {
                    // Store first exception (that of UTC_FORMATTER) to throw if we fail to parse
                    // the date
                    if (exception == null) {
                        exception = e
                    }
                    // Try next parser
                }
            }
            throw exception!!
        }

        /**
         * Format a date as ISO-8601 UTC timestamp.
         *
         * @param date date
         * @return timestamp string
         */
        @JvmStatic
        fun formatUTCDate(date: Date): String {
            return UTC_FORMATTER.format(date)
        }
    }
}
