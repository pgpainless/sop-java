// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import sop.util.Optional

/**
 * Result of decrypting a message.
 *
 * @param sessionKey optional session key retrieved during decryption.
 * If this value is null, the implementation did not support extracting the session key.
 * @param verifications successfully verified signatures.
 */
class DecryptionResult(sessionKey: SessionKey?, val verifications: List<Verification>) {
    val sessionKey: Optional<SessionKey> = Optional.ofNullable(sessionKey)
}
