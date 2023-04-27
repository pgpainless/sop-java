// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.DecryptionResult;
import sop.ReadyWithResult;
import sop.SessionKey;
import sop.exception.SOPGPException;
import sop.util.UTF8Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public interface Decrypt {

    /**
     * Makes the SOP consider signatures before this date invalid.
     *
     * @param timestamp timestamp
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.UnsupportedOption if this option is not supported
     */
    Decrypt verifyNotBefore(Date timestamp)
            throws SOPGPException.UnsupportedOption;

    /**
     * Makes the SOP consider signatures after this date invalid.
     *
     * @param timestamp timestamp
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.UnsupportedOption if this option is not supported
     */
    Decrypt verifyNotAfter(Date timestamp)
            throws SOPGPException.UnsupportedOption;

    /**
     * Adds one or more verification cert.
     *
     * @param cert input stream containing the cert(s)
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.BadData if the {@link InputStream} doesn't provide an OpenPGP certificate
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the cert uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    Decrypt verifyWithCert(InputStream cert)
            throws SOPGPException.BadData,
            SOPGPException.UnsupportedAsymmetricAlgo,
            IOException;

    /**
     * Adds one or more verification cert.
     *
     * @param cert byte array containing the cert(s)
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.BadData if the byte array doesn't contain an OpenPGP certificate
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the cert uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    default Decrypt verifyWithCert(byte[] cert)
            throws SOPGPException.BadData,
            SOPGPException.UnsupportedAsymmetricAlgo,
            IOException {
        return verifyWithCert(new ByteArrayInputStream(cert));
    }

    /**
     * Tries to decrypt with the given session key.
     *
     * @param sessionKey session key
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.UnsupportedOption if this option is not supported
     */
    Decrypt withSessionKey(SessionKey sessionKey)
            throws SOPGPException.UnsupportedOption;

    /**
     * Tries to decrypt with the given password.
     *
     * @param password password
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.PasswordNotHumanReadable if the password is not human-readable
     * @throws sop.exception.SOPGPException.UnsupportedOption if this option is not supported
     */
    Decrypt withPassword(String password)
            throws SOPGPException.PasswordNotHumanReadable,
            SOPGPException.UnsupportedOption;

    /**
     * Adds one or more decryption key.
     *
     * @param key input stream containing the key(s)
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.BadData if the {@link InputStream} does not provide an OpenPGP key
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    Decrypt withKey(InputStream key)
            throws SOPGPException.BadData,
            SOPGPException.UnsupportedAsymmetricAlgo,
            IOException;

    /**
     * Adds one or more decryption key.
     *
     * @param key byte array containing the key(s)
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.BadData if the byte array does not contain an OpenPGP key
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    default Decrypt withKey(byte[] key)
            throws SOPGPException.BadData,
            SOPGPException.UnsupportedAsymmetricAlgo,
            IOException {
        return withKey(new ByteArrayInputStream(key));
    }

    /**
     * Provide the decryption password for the secret key.
     *
     * @param password password
     * @return builder instance
     * @throws sop.exception.SOPGPException.UnsupportedOption if the implementation does not support key passwords
     * @throws sop.exception.SOPGPException.PasswordNotHumanReadable if the password is not human-readable
     */
    default Decrypt withKeyPassword(String password)
            throws SOPGPException.UnsupportedOption,
            SOPGPException.PasswordNotHumanReadable {
        return withKeyPassword(password.getBytes(UTF8Util.UTF8));
    }

    /**
     * Provide the decryption password for the secret key.
     *
     * @param password password
     * @return builder instance
     * @throws sop.exception.SOPGPException.UnsupportedOption if the implementation does not support key passwords
     * @throws sop.exception.SOPGPException.PasswordNotHumanReadable if the password is not human-readable
     */
    Decrypt withKeyPassword(byte[] password)
            throws SOPGPException.UnsupportedOption,
            SOPGPException.PasswordNotHumanReadable;

    /**
     * Decrypts the given ciphertext, returning verification results and plaintext.
     * @param ciphertext ciphertext
     * @return ready with result
     *
     * @throws sop.exception.SOPGPException.BadData if the {@link InputStream} does not provide an OpenPGP message
     * @throws sop.exception.SOPGPException.MissingArg if an argument required for decryption was not provided
     * @throws sop.exception.SOPGPException.CannotDecrypt in case decryption fails for some reason
     * @throws sop.exception.SOPGPException.KeyIsProtected if the decryption key cannot be unlocked (e.g. missing passphrase)
     * @throws IOException in case of an IO error
     */
    ReadyWithResult<DecryptionResult> ciphertext(InputStream ciphertext)
            throws SOPGPException.BadData,
            SOPGPException.MissingArg,
            SOPGPException.CannotDecrypt,
            SOPGPException.KeyIsProtected,
            IOException;

    /**
     * Decrypts the given ciphertext, returning verification results and plaintext.
     * @param ciphertext ciphertext
     * @return ready with result
     *
     * @throws sop.exception.SOPGPException.BadData if the byte array does not contain an encrypted OpenPGP message
     * @throws sop.exception.SOPGPException.MissingArg in case of missing decryption method (password or key required)
     * @throws sop.exception.SOPGPException.CannotDecrypt in case decryption fails for some reason
     * @throws sop.exception.SOPGPException.KeyIsProtected if the decryption key cannot be unlocked (e.g. missing passphrase)
     * @throws IOException in case of an IO error
     */
    default ReadyWithResult<DecryptionResult> ciphertext(byte[] ciphertext)
            throws SOPGPException.BadData,
            SOPGPException.MissingArg,
            SOPGPException.CannotDecrypt,
            SOPGPException.KeyIsProtected,
            IOException {
        return ciphertext(new ByteArrayInputStream(ciphertext));
    }
}
