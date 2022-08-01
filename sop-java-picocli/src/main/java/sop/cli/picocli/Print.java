// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli;

public class Print {

    public static void outln(String string) {
        // CHECKSTYLE:OFF
        System.out.println(string);
        // CHECKSTYLE:ON
    }
}
