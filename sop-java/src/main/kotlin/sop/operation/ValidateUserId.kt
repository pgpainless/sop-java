// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.exception.SOPGPException

/** Subcommand to validate UserIDs on certificates. */
interface ValidateUserId {

    /**
     * If this is set, then the USERID is treated as an e-mail address, and matched only against the
     * e-mail address part of each correctly bound User ID. The rest of each correctly bound User ID
     * is ignored.
     *
     * @return this
     */
    @Throws(SOPGPException.UnsupportedOption::class) fun addrSpecOnly(): ValidateUserId

    /**
     * Set the UserID to validate. To match only the email address, call [addrSpecOnly].
     *
     * @param userId UserID or email address
     * @return this
     */
    fun userId(userId: String): ValidateUserId

    /**
     * Add certificates, which act as authorities. The [userId] is only considered correctly bound,
     * if it was bound by an authoritative certificate.
     *
     * @param certs authoritative certificates
     * @return this
     */
    @Throws(SOPGPException.BadData::class, IOException::class)
    fun authorities(certs: InputStream): ValidateUserId

    /**
     * Add certificates, which act as authorities. The [userId] is only considered correctly bound,
     * if it was bound by an authoritative certificate.
     *
     * @param certs authoritative certificates
     * @return this
     */
    @Throws(SOPGPException.BadData::class, IOException::class)
    fun authorities(certs: ByteArray): ValidateUserId = authorities(certs.inputStream())

    /**
     * Add subject certificates, on which UserID bindings are validated.
     *
     * @param certs subject certificates
     * @return true if all subject certificates have a correct binding to the UserID.
     * @throws SOPGPException.BadData if the subject certificates are malformed
     * @throws IOException if a parser exception happens
     * @throws SOPGPException.CertUserIdNoMatch if any subject certificate does not have a correctly
     *   bound UserID that matches [userId].
     */
    @Throws(
        SOPGPException.BadData::class, IOException::class, SOPGPException.CertUserIdNoMatch::class)
    fun subjects(certs: InputStream): Boolean

    /**
     * Add subject certificates, on which UserID bindings are validated.
     *
     * @param certs subject certificates
     * @return true if all subject certificates have a correct binding to the UserID.
     * @throws SOPGPException.BadData if the subject certificates are malformed
     * @throws IOException if a parser exception happens
     * @throws SOPGPException.CertUserIdNoMatch if any subject certificate does not have a correctly
     *   bound UserID that matches [userId].
     */
    @Throws(
        SOPGPException.BadData::class, IOException::class, SOPGPException.CertUserIdNoMatch::class)
    fun subjects(certs: ByteArray): Boolean = subjects(certs.inputStream())
}
