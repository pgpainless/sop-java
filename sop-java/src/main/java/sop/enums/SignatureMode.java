// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.enums;

/**
 * Enum referencing relevant signature types.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc4880#section-5.2.1">
 *     RFC4880 §5.2.1 - Signature Types</a>
 */
public enum SignatureMode {
    /**
     * Signature of a binary document (<pre>0x00</pre>).
     */
    binary,

    /**
     * Signature of a canonical text document (<pre>0x01</pre>).
     */
    text

    // Other Signature Types are irrelevant.
}
