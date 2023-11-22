// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.enums;

public enum ArmorLabel {
    Auto,
    Sig,
    Key,
    Cert,
    Message,
    ;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}