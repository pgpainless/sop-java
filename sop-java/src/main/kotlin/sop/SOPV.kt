// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import sop.operation.DetachedVerify
import sop.operation.InlineVerify
import sop.operation.ValidateUserId
import sop.operation.Version

/** Subset of [SOP] implementing only OpenPGP signature verification. */
interface SOPV {

    /** Get information about the implementations name and version. */
    fun version(): Version?

    /**
     * Verify detached signatures. If you need to verify an inline-signed message, use
     * [inlineVerify] instead.
     */
    fun verify(): DetachedVerify? = detachedVerify()

    /**
     * Verify detached signatures. If you need to verify an inline-signed message, use
     * [inlineVerify] instead.
     */
    fun detachedVerify(): DetachedVerify?

    /**
     * Verify signatures of an inline-signed message. If you need to verify detached signatures over
     * a message, use [detachedVerify] instead.
     */
    fun inlineVerify(): InlineVerify?

    /** Validate a UserID in an OpenPGP certificate. */
    fun validateUserId(): ValidateUserId?
}
