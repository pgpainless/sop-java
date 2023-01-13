// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.enums;

public enum SignAs {
    /**
     * Signature is made over the binary message.
     */
    Binary,

    /**
     * Signature is made over the message in text mode.
     */
    Text,
    ;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
