// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.exception.SOPGPException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public interface AbstractSign<T> {

    /**
     * Disable ASCII armor encoding.
     *
     * @return builder instance
     */
    T noArmor();

    /**
     * Add one or more signing keys.
     *
     * @param key input stream containing encoded keys
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.KeyCannotSign if the key cannot be used for signing
     * @throws sop.exception.SOPGPException.BadData if the {@link InputStream} does not contain an OpenPGP key
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    T key(InputStream key)
            throws SOPGPException.KeyCannotSign,
            SOPGPException.BadData,
            SOPGPException.UnsupportedAsymmetricAlgo,
            IOException;

    /**
     * Add one or more signing keys.
     *
     * @param key byte array containing encoded keys
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.KeyCannotSign if the key cannot be used for signing
     * @throws sop.exception.SOPGPException.BadData if the byte array does not contain an OpenPGP key
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the key uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    default T key(byte[] key)
            throws SOPGPException.KeyCannotSign,
            SOPGPException.BadData,
            SOPGPException.UnsupportedAsymmetricAlgo,
            IOException {
        return key(new ByteArrayInputStream(key));
    }

    /**
     * Provide the password for the secret key used for signing.
     *
     * @param password password
     * @return builder instance
     * @throws sop.exception.SOPGPException.UnsupportedOption if key passwords are not supported
     * @throws sop.exception.SOPGPException.PasswordNotHumanReadable if the provided passphrase is not human-readable
     */
    default T withKeyPassword(String password)
            throws SOPGPException.UnsupportedOption,
            SOPGPException.PasswordNotHumanReadable {
        return withKeyPassword(password.getBytes(Charset.forName("UTF8")));
    }

    /**
     * Provide the password for the secret key used for signing.
     *
     * @param password password
     * @return builder instance
     * @throws sop.exception.SOPGPException.UnsupportedOption if key passwords are not supported
     * @throws sop.exception.SOPGPException.PasswordNotHumanReadable if the provided passphrase is not human-readable
     */
    T withKeyPassword(byte[] password)
            throws SOPGPException.UnsupportedOption,
            SOPGPException.PasswordNotHumanReadable;

}
