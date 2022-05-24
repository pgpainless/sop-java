// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.enums;

public enum InlineSignAs {

    /**
     * Signature is made over the binary message.
     */
    Binary,

    /**
     * Signature is made over the message in text mode.
     */
    Text,

    /**
     * Signature is made using the Cleartext Signature Framework.
     */
    CleartextSigned,
}

