// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;
import sop.cli.picocli.commands.AbstractSopCmd;
import sop.cli.picocli.commands.ArmorCmd;
import sop.util.UTCUtil;

public class DateParsingTest {
    private AbstractSopCmd cmd = new ArmorCmd(); // we use ArmorCmd as a concrete implementation.

    @Test
    public void parseNotAfterDashReturnsEndOfTime() {
        assertEquals(AbstractSopCmd.END_OF_TIME, cmd.parseNotAfter("-"));
    }

    @Test
    public void parseNotBeforeDashReturnsBeginningOfTime() {
        assertEquals(AbstractSopCmd.BEGINNING_OF_TIME, cmd.parseNotBefore("-"));
    }

    @Test
    public void parseNotAfterNowReturnsNow() {
        assertEquals(new Date().getTime(), cmd.parseNotAfter("now").getTime(), 1000);
    }

    @Test
    public void parseNotBeforeNowReturnsNow() {
        assertEquals(new Date().getTime(), cmd.parseNotBefore("now").getTime(), 1000);
    }

    @Test
    public void parseNotAfterTimestamp() {
        String timestamp = "2019-10-24T23:48:29Z";
        Date date = cmd.parseNotAfter(timestamp);
        assertEquals(timestamp, UTCUtil.formatUTCDate(date));
    }

    @Test
    public void parseNotBeforeTimestamp() {
        String timestamp = "2019-10-29T18:36:45Z";
        Date date = cmd.parseNotBefore(timestamp);
        assertEquals(timestamp, UTCUtil.formatUTCDate(date));
    }
}
