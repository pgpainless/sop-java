// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.enums

/**
 * Enum referencing relevant signature types.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc4880#section-5.2.1"> RFC4880 §5.2.1 - Signature
 *   Types</a>
 */
enum class SignatureMode(val mode: Int) {
    /** Signature of a binary document (type `0x00`). */
    binary(0x00),
    /** Signature of a canonical text document (type `0x01`). */
    text(0x01)
}
