// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.DecryptionResult;
import sop.ReadyWithResult;
import sop.SessionKey;
import sop.exception.SOPGPException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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
     * @throws IOException in case of an IO error
     */
    Decrypt verifyWithCert(InputStream cert)
            throws SOPGPException.BadData,
            IOException;

    /**
     * Adds one or more verification cert.
     *
     * @param cert byte array containing the cert(s)
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.BadData if the byte array doesn't contain an OpenPGP certificate
     * @throws IOException in case of an IO error
     */
    default Decrypt verifyWithCert(byte[] cert)
            throws SOPGPException.BadData, IOException {
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
     * @throws sop.exception.SOPGPException.KeyIsProtected if the key is password protected
     * @throws sop.exception.SOPGPException.BadData if the {@link InputStream} does not provide an OpenPGP key
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     */
    Decrypt withKey(InputStream key)
            throws SOPGPException.KeyIsProtected,
            SOPGPException.BadData,
            SOPGPException.UnsupportedAsymmetricAlgo;

    /**
     * Adds one or more decryption key.
     *
     * @param key byte array containing the key(s)
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.KeyIsProtected if the key is password protected
     * @throws sop.exception.SOPGPException.BadData if the byte array does not contain an OpenPGP key
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     */
    default Decrypt withKey(byte[] key)
            throws SOPGPException.KeyIsProtected,
            SOPGPException.BadData,
            SOPGPException.UnsupportedAsymmetricAlgo {
        return withKey(new ByteArrayInputStream(key));
    }

    /**
     * Provide the decryption password for the secret key.
     *
     * @param password password
     * @return builder instance
     */
    default Decrypt withKeyPassword(String password) {
        return withKeyPassword(password.getBytes(Charset.forName("UTF8")));
    }

    /**
     * Provide the decryption password for the secret key.
     *
     * @param password password
     * @return builder instance
     */
    Decrypt withKeyPassword(byte[] password);

    /**
     * Decrypts the given ciphertext, returning verification results and plaintext.
     * @param ciphertext ciphertext
     * @return ready with result
     *
     * @throws sop.exception.SOPGPException.BadData if the {@link InputStream} does not provide an OpenPGP message
     * @throws sop.exception.SOPGPException.MissingArg in case of missing decryption method (password or key required)
     * @throws sop.exception.SOPGPException.CannotDecrypt in case decryption fails for some reason
     */
    ReadyWithResult<DecryptionResult> ciphertext(InputStream ciphertext)
            throws SOPGPException.BadData, SOPGPException.MissingArg, SOPGPException.CannotDecrypt;

    /**
     * Decrypts the given ciphertext, returning verification results and plaintext.
     * @param ciphertext ciphertext
     * @return ready with result
     *
     * @throws sop.exception.SOPGPException.BadData if the byte array does not contain an encrypted OpenPGP message
     * @throws sop.exception.SOPGPException.MissingArg in case of missing decryption method (password or key required)
     * @throws sop.exception.SOPGPException.CannotDecrypt in case decryption fails for some reason
     */
    default ReadyWithResult<DecryptionResult> ciphertext(byte[] ciphertext)
        throws SOPGPException.BadData, SOPGPException.MissingArg, SOPGPException.CannotDecrypt {
        return ciphertext(new ByteArrayInputStream(ciphertext));
    }
}
