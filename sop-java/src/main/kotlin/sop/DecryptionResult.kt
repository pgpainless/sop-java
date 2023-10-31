// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import sop.util.Optional

class DecryptionResult(sessionKey: SessionKey?, val verifications: List<Verification>) {
    val sessionKey: Optional<SessionKey>

    init {
        this.sessionKey = Optional.ofNullable(sessionKey)
    }
}
