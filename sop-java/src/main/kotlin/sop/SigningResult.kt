// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

/**
 * This class contains various information about a signed message.
 *
 * @param micAlg string identifying the digest mechanism used to create the signed message. This is
 *   useful for setting the `micalg=` parameter for the multipart/signed content-type of a PGP/MIME
 *   object as described in section 5 of [RFC3156]. If more than one signature was generated and
 *   different digest mechanisms were used, the value of the micalg object is an empty string.
 */
data class SigningResult(val micAlg: MicAlg) {

    class Builder internal constructor() {
        private var micAlg = MicAlg.empty()

        fun setMicAlg(micAlg: MicAlg) = apply { this.micAlg = micAlg }

        fun build() = SigningResult(micAlg)
    }

    companion object {
        @JvmStatic fun builder() = Builder()
    }
}
