// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.util;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class to parse and format dates as ISO-8601 UTC timestamps.
 */
public class UTCUtil {

    public static final SimpleDateFormat UTC_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final SimpleDateFormat[] UTC_PARSERS = new SimpleDateFormat[] {
            UTC_FORMATTER,
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"),
            new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    };

    static {
        for (SimpleDateFormat f : UTC_PARSERS) {
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
    }
    /**
     * Parse an ISO-8601 UTC timestamp from a string.
     *
     * @param dateString string
     * @return date
     * @throws ParseException if the date string is malformed and cannot be parsed
     */
    @Nonnull
    public static Date parseUTCDate(String dateString) throws ParseException {
        ParseException exception = null;
        for (SimpleDateFormat parser : UTC_PARSERS) {
            try {
                return parser.parse(dateString);
            } catch (ParseException e) {
                // Store first exception (that of UTC_FORMATTER) to throw if we fail to parse the date
                if (exception == null) {
                    exception = e;
                }
                // Try next parser
            }
        }
        // No parser worked, so we throw the store exception
        throw exception;
    }

    /**
     * Format a date as ISO-8601 UTC timestamp.
     *
     * @param date date
     * @return timestamp string
     */
    public static String formatUTCDate(Date date) {
        return UTC_FORMATTER.format(date);
    }
}
