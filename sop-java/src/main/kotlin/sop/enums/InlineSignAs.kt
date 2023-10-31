// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.enums

enum class InlineSignAs {

    /** Signature is made over the binary message. */
    binary,

    /** Signature is made over the message in text mode. */
    text,

    /** Signature is made using the Cleartext Signature Framework. */
    clearsigned
}
