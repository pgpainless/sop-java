// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import sop.operation.Armor
import sop.operation.ChangeKeyPassword
import sop.operation.Dearmor
import sop.operation.Decrypt
import sop.operation.DetachedSign
import sop.operation.DetachedVerify
import sop.operation.Encrypt
import sop.operation.ExtractCert
import sop.operation.GenerateKey
import sop.operation.InlineDetach
import sop.operation.InlineSign
import sop.operation.InlineVerify
import sop.operation.ListProfiles
import sop.operation.RevokeKey
import sop.operation.Version

/**
 * Stateless OpenPGP Interface. This class provides a stateless interface to various OpenPGP related
 * operations. Note: Subcommand objects acquired by calling any method of this interface are not
 * intended for reuse. If you for example need to generate multiple keys, make a dedicated call to
 * [generateKey] once per key generation.
 */
interface SOP {

    /** Get information about the implementations name and version. */
    fun version(): Version

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

    /**
     * Verify detached signatures. If you need to verify an inline-signed message, use
     * [inlineVerify] instead.
     */
    fun verify(): DetachedVerify = detachedVerify()

    /**
     * Verify detached signatures. If you need to verify an inline-signed message, use
     * [inlineVerify] instead.
     */
    fun detachedVerify(): DetachedVerify

    /**
     * Verify signatures of an inline-signed message. If you need to verify detached signatures over
     * a message, use [detachedVerify] instead.
     */
    fun inlineVerify(): InlineVerify

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
}
