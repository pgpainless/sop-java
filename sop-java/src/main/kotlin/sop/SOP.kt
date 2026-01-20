// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import sop.operation.*

/**
 * Stateless OpenPGP Interface. This class provides a stateless interface to various OpenPGP related
 * operations. Note: Subcommand objects acquired by calling any method of this interface are not
 * intended for reuse. If you for example need to generate multiple keys, make a dedicated call to
 * [generateKey] once per key generation.
 */
interface SOP : SOPV {

    /**
     * Generate a secret key.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun generateKey(): GenerateKey

    /**
     * Extract a certificate (public key) from a secret key.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun extractCert(): ExtractCert

    /**
     * Create detached signatures. If you want to sign a message inline, use [inlineSign] instead.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun sign(): DetachedSign = detachedSign()

    /**
     * Create detached signatures. If you want to sign a message inline, use [inlineSign] instead.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun detachedSign(): DetachedSign

    /**
     * Sign a message using inline signatures. If you need to create detached signatures, use
     * [detachedSign] instead.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun inlineSign(): InlineSign

    /**
     * Detach signatures from an inline signed message.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun inlineDetach(): InlineDetach

    /**
     * Encrypt a message.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun encrypt(): Encrypt

    /**
     * Decrypt a message.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun decrypt(): Decrypt

    /**
     * Convert binary OpenPGP data to ASCII.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun armor(): Armor

    /**
     * Converts ASCII armored OpenPGP data to binary.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun dearmor(): Dearmor

    /**
     * List supported [Profiles][Profile] of a subcommand.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun listProfiles(): ListProfiles

    /**
     * Revoke one or more secret keys.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun revokeKey(): RevokeKey

    /**
     * Update a key's password.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun changeKeyPassword(): ChangeKeyPassword

    /**
     * Keep a secret key up-to-date.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun updateKey(): UpdateKey

    /**
     * Merge OpenPGP certificates.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun mergeCerts(): MergeCerts

    /**
     * Certify OpenPGP Certificate User-IDs.
     *
     * @throws sop.exception.SOPGPException.UnsupportedSubcommand if the command is not implemented.
     */
    fun certifyUserId(): CertifyUserId
}
