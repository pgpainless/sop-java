// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.enums

enum class SignAs {
    /** Signature is made over the binary message. */
    binary,
    /** Signature is made over the message in text mode. */
    text
}
