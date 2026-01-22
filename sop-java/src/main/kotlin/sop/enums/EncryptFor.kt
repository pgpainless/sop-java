// SPDX-FileCopyrightText: 2026 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.enums

/**
 * Enum mapping the purpose of message encryption. The purpose is signaled by the two
 * encryption-replated [key flags](https://www.rfc-editor.org/rfc/rfc9580.html#name-key-flags)
 * `0x04` (encrypt communications) and `0x08` (encrypt storage).
 */
enum class EncryptFor(vararg flagMask: Int) {
    /** Encrypt for the purpose of long-term storage. */
    storage(0x08),

    /** Encrypt for the purpose of (online) communications. */
    communications(0x04),

    /** Encrypt for any purpose. */
    any(0x04, 0x08),
    ;

    val flags: IntArray = flagMask
}
