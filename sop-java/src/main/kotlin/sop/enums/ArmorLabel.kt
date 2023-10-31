// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.enums

@Deprecated("Use of armor labels is deprecated.")
enum class ArmorLabel {
    auto,
    sig,
    key,
    cert,
    message
}
