// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.enums

enum class SignAs(val mode: Int) {
    /** Signature is made over the binary message. */
    binary(0x00),
    /** Signature is made over the message in text mode. */
    text(0x01)
}
