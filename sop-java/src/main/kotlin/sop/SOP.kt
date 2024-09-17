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

    /** Generate a secret key. */
    fun generateKey(): GenerateKey

    /** Extract a certificate (public key) from a secret key. */
    fun extractCert(): ExtractCert

    /**
     * Create detached signatures. If you want to sign a message inline, use [inlineSign] instead.
     */
    fun sign(): DetachedSign = detachedSign()

    /**
     * Create detached signatures. If you want to sign a message inline, use [inlineSign] instead.
     */
    fun detachedSign(): DetachedSign

    /**
     * Sign a message using inline signatures. If you need to create detached signatures, use
     * [detachedSign] instead.
     */
    fun inlineSign(): InlineSign

    /** Detach signatures from an inline signed message. */
    fun inlineDetach(): InlineDetach

    /** Encrypt a message. */
    fun encrypt(): Encrypt

    /** Decrypt a message. */
    fun decrypt(): Decrypt

    /** Convert binary OpenPGP data to ASCII. */
    fun armor(): Armor

    /** Converts ASCII armored OpenPGP data to binary. */
    fun dearmor(): Dearmor

    /** List supported [Profiles][Profile] of a subcommand. */
    fun listProfiles(): ListProfiles

    /** Revoke one or more secret keys. */
    fun revokeKey(): RevokeKey

    /** Update a key's password. */
    fun changeKeyPassword(): ChangeKeyPassword

    /**
     * Keep a secret key up-to-date.
     */
    fun updateKey(): UpdateKey
}
