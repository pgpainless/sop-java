// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import sop.util.Optional

class EncryptionResult(sessionKey: SessionKey?) {
    val sessionKey: Optional<SessionKey>

    init {
        this.sessionKey = Optional.ofNullable(sessionKey)
    }
}
