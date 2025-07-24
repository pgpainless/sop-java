// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import java.util.*
import sop.exception.SOPGPException.*

/** Interface to validate UserIDs on certificates. */
interface ValidateUserId {

    /**
     * If this is set, then the USERID is treated as an e-mail address, and matched only against the
     * e-mail address part of each correctly bound User ID. The rest of each correctly bound User ID
     * is ignored.
     *
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class) fun addrSpecOnly(): ValidateUserId

    /**
     * Set the UserID to validate. To match only the email address, call [addrSpecOnly].
     *
     * @param userId UserID or email address
     * @return builder instance
     */
    fun userId(userId: String): ValidateUserId

    /**
     * Add certificates, which act as authorities. The [userId] is only considered correctly bound,
     * if it was bound by an authoritative certificate.
     *
     * @param certs authoritative certificates
     * @return builder instance
     * @throws BadData if the authority certificates cannot be read
     * @throws IOException if an IO error occurs
     */
    @Throws(BadData::class, IOException::class) fun authorities(certs: InputStream): ValidateUserId

    /**
     * Add certificates, which act as authorities. The [userId] is only considered correctly bound,
     * if it was bound by an authoritative certificate.
     *
     * @param certs authoritative certificates
     * @return builder instance
     * @throws BadData if the authority certificates cannot be read
     * @throws IOException if an IO error occurs
     */
    @Throws(BadData::class, IOException::class)
    fun authorities(certs: ByteArray): ValidateUserId = authorities(certs.inputStream())

    /**
     * Add subject certificates, on which UserID bindings are validated.
     *
     * @param certs subject certificates
     * @return true if all subject certificates have a correct binding to the UserID.
     * @throws BadData if the subject certificates are malformed
     * @throws IOException if a parser exception happens
     * @throws CertUserIdNoMatch if any subject certificate does not have a correctly bound UserID
     *   that matches [userId].
     */
    @Throws(BadData::class, IOException::class, CertUserIdNoMatch::class)
    fun subjects(certs: InputStream): Boolean

    /**
     * Add subject certificates, on which UserID bindings are validated.
     *
     * @param certs subject certificates
     * @return true if all subject certificates have a correct binding to the UserID.
     * @throws BadData if the subject certificates are malformed
     * @throws IOException if a parser exception happens
     * @throws CertUserIdNoMatch if any subject certificate does not have a correctly bound UserID
     *   that matches [userId].
     */
    @Throws(BadData::class, IOException::class, CertUserIdNoMatch::class)
    fun subjects(certs: ByteArray): Boolean = subjects(certs.inputStream())

    /**
     * Provide a reference time for user-id validation.
     *
     * @param date reference time
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class) fun validateAt(date: Date): ValidateUserId
}
