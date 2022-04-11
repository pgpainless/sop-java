// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import sop.Ready;
import sop.enums.EncryptAs;
import sop.exception.SOPGPException;

public interface Encrypt {

    /**
     * Disable ASCII armor encoding.
     *
     * @return builder instance
     */
    Encrypt noArmor();

    /**
     * Sets encryption mode.
     *
     * @param mode mode
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.UnsupportedOption if this option is not supported
     */
    Encrypt mode(EncryptAs mode)
            throws SOPGPException.UnsupportedOption;

    /**
     * Adds the signer key.
     *
     * @param key input stream containing the encoded signer key
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.KeyIsProtected if the key is password protected
     * @throws sop.exception.SOPGPException.KeyCannotSign if the key is not capable of signing
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     * @throws sop.exception.SOPGPException.BadData if the {@link InputStream} does not contain an OpenPGP key
     */
    Encrypt signWith(InputStream key)
            throws SOPGPException.KeyIsProtected,
            SOPGPException.KeyCannotSign,
            SOPGPException.UnsupportedAsymmetricAlgo,
            SOPGPException.BadData;

    /**
     * Adds the signer key.
     *
     * @param key byte array containing the encoded signer key
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.KeyIsProtected if the key is password protected
     * @throws sop.exception.SOPGPException.KeyCannotSign if the key is not capable of signing
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     * @throws sop.exception.SOPGPException.BadData if the byte array does not contain an OpenPGP key
     */
    default Encrypt signWith(byte[] key)
            throws SOPGPException.KeyIsProtected,
            SOPGPException.KeyCannotSign,
            SOPGPException.UnsupportedAsymmetricAlgo,
            SOPGPException.BadData {
        return signWith(new ByteArrayInputStream(key));
    }

    /**
     * Encrypt with the given password.
     *
     * @param password password
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.PasswordNotHumanReadable if the password is not human-readable
     * @throws sop.exception.SOPGPException.UnsupportedOption if this option is not supported
     */
    Encrypt withPassword(String password)
            throws SOPGPException.PasswordNotHumanReadable,
            SOPGPException.UnsupportedOption;

    /**
     * Encrypt with the given cert.
     *
     * @param cert input stream containing the encoded cert.
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.CertCannotEncrypt if the certificate is not encryption capable
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the certificate uses an unsupported asymmetric algorithm
     * @throws sop.exception.SOPGPException.BadData if the {@link InputStream} does not contain an OpenPGP certificate
     */
    Encrypt withCert(InputStream cert)
            throws SOPGPException.CertCannotEncrypt,
            SOPGPException.UnsupportedAsymmetricAlgo,
            SOPGPException.BadData;

    /**
     * Encrypt with the given cert.
     *
     * @param cert byte array containing the encoded cert.
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.CertCannotEncrypt if the certificate is not encryption capable
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the certificate uses an unsupported asymmetric algorithm
     * @throws sop.exception.SOPGPException.BadData if the byte array does not contain an OpenPGP certificate
     */
    default Encrypt withCert(byte[] cert)
            throws SOPGPException.CertCannotEncrypt,
            SOPGPException.UnsupportedAsymmetricAlgo,
            SOPGPException.BadData {
        return withCert(new ByteArrayInputStream(cert));
    }

    /**
     * Encrypt the given data yielding the ciphertext.
     * @param plaintext plaintext
     * @return input stream containing the ciphertext
     *
     * @throws IOException in case of an IO error
     */
    Ready plaintext(InputStream plaintext)
        throws IOException;

    /**
     * Encrypt the given data yielding the ciphertext.
     * @param plaintext plaintext
     * @return input stream containing the ciphertext
     *
     * @throws IOException in case of an IO error
     */
    default Ready plaintext(byte[] plaintext) throws IOException {
        return plaintext(new ByteArrayInputStream(plaintext));
    }
}
