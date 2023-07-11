// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.util.UTF8Util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;

public interface ChangeKeyPassword {

    /**
     * Disable ASCII armoring of the output.
     *
     * @return builder instance
     */
    ChangeKeyPassword noArmor();

    default ChangeKeyPassword oldKeyPassphrase(byte[] password) {
        try {
            return oldKeyPassphrase(UTF8Util.decodeUTF8(password));
        } catch (CharacterCodingException e) {
            throw new SOPGPException.PasswordNotHumanReadable("Password MUST be a valid UTF8 string.");
        }
    }

    /**
     * Provide a passphrase to unlock the secret key.
     * This method can be provided multiple times to provide separate passphrases that are tried as a
     * means to unlock any secret key material encountered.
     *
     * @param oldPassphrase old passphrase
     * @return builder instance
     */
    ChangeKeyPassword oldKeyPassphrase(String oldPassphrase);

    /**
     * Provide a passphrase to re-lock the secret key with.
     * This method can only be used once, and all key material encountered will be encrypted with the given passphrase.
     * If this method is not called, the key material will not be protected.
     *
     * @param newPassphrase new passphrase
     * @return builder instance
     */
    default ChangeKeyPassword newKeyPassphrase(byte[] newPassphrase) {
        try {
            return newKeyPassphrase(UTF8Util.decodeUTF8(newPassphrase));
        } catch (CharacterCodingException e) {
            throw new SOPGPException.PasswordNotHumanReadable("Password MUST be a valid UTF8 string.");
        }
    }

    /**
     * Provide a passphrase to re-lock the secret key with.
     * This method can only be used once, and all key material encountered will be encrypted with the given passphrase.
     * If this method is not called, the key material will not be protected.
     *
     * @param newPassphrase new passphrase
     * @return builder instance
     */
    ChangeKeyPassword newKeyPassphrase(String newPassphrase);

    default Ready keys(byte[] keys) throws SOPGPException.KeyIsProtected, SOPGPException.BadData {
        return keys(new ByteArrayInputStream(keys));
    }

    /**
     * Provide the key material.
     *
     * @param inputStream input stream of secret key material
     * @return ready
     *
     * @throws sop.exception.SOPGPException.KeyIsProtected if any (sub-) key encountered cannot be unlocked.
     * @throws sop.exception.SOPGPException.BadData if the key material is malformed
     */
    Ready keys(InputStream inputStream) throws SOPGPException.KeyIsProtected, SOPGPException.BadData;

}
