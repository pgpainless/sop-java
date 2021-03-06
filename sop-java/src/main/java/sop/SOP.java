// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop;

import sop.operation.Armor;
import sop.operation.Dearmor;
import sop.operation.Decrypt;
import sop.operation.Encrypt;
import sop.operation.ExtractCert;
import sop.operation.GenerateKey;
import sop.operation.InlineDetach;
import sop.operation.InlineSign;
import sop.operation.InlineVerify;
import sop.operation.DetachedSign;
import sop.operation.DetachedVerify;
import sop.operation.Version;

/**
 * Stateless OpenPGP Interface.
 */
public interface SOP {

    /**
     * Get information about the implementations name and version.
     *
     * @return version
     */
    Version version();

    /**
     * Generate a secret key.
     * Customize the operation using the builder {@link GenerateKey}.
     *
     * @return builder instance
     */
    GenerateKey generateKey();

    /**
     * Extract a certificate (public key) from a secret key.
     * Customize the operation using the builder {@link ExtractCert}.
     *
     * @return builder instance
     */
    ExtractCert extractCert();

    /**
     * Create detached signatures.
     * Customize the operation using the builder {@link DetachedSign}.
     *
     * If you want to sign a message inline, use {@link #inlineSign()} instead.
     *
     * @return builder instance
     */
    default DetachedSign sign() {
        return detachedSign();
    }

    /**
     * Create detached signatures.
     * Customize the operation using the builder {@link DetachedSign}.
     *
     * If you want to sign a message inline, use {@link #inlineSign()} instead.
     *
     * @return builder instance
     */
    DetachedSign detachedSign();

    /**
     * Sign a message using inline signatures.
     *
     * If you need to create detached signatures, use {@link #detachedSign()} instead.
     *
     * @return builder instance
     */
    InlineSign inlineSign();

    /**
     * Verify detached signatures.
     * Customize the operation using the builder {@link DetachedVerify}.
     *
     * If you need to verify an inline-signed message, use {@link #inlineVerify()} instead.
     *
     * @return builder instance
     */
    default DetachedVerify verify() {
        return detachedVerify();
    }

    /**
     * Verify detached signatures.
     * Customize the operation using the builder {@link DetachedVerify}.
     *
     * If you need to verify an inline-signed message, use {@link #inlineVerify()} instead.
     *
     * @return builder instance
     */
    DetachedVerify detachedVerify();

    /**
     * Verify signatures of an inline-signed message.
     *
     * If you need to verify detached signatures over a message, use {@link #detachedVerify()} instead.
     *
     * @return builder instance
     */
    InlineVerify inlineVerify();

    /**
     * Detach signatures from an inline signed message.
     *
     * @return builder instance
     */
    InlineDetach inlineDetach();

    /**
     * Encrypt a message.
     * Customize the operation using the builder {@link Encrypt}.
     *
     * @return builder instance
     */
    Encrypt encrypt();

    /**
     * Decrypt a message.
     * Customize the operation using the builder {@link Decrypt}.
     *
     * @return builder instance
     */
    Decrypt decrypt();

    /**
     * Convert binary OpenPGP data to ASCII.
     * Customize the operation using the builder {@link Armor}.
     *
     * @return builder instance
     */
    Armor armor();

    /**
     * Converts ASCII armored OpenPGP data to binary.
     * Customize the operation using the builder {@link Dearmor}.
     *
     * @return builder instance
     */
    Dearmor dearmor();

}
