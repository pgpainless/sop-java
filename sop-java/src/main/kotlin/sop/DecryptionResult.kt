// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import sop.util.Optional

data class DecryptionResult
internal constructor(val sessionKey: Optional<SessionKey>, val verifications: List<Verification>) {

    constructor(
        sessionKey: SessionKey?,
        verifications: List<Verification>
    ) : this(Optional.ofNullable(sessionKey), verifications)
}
