// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.ReadyWithResult;
import sop.SigningResult;
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
    Sign noArmor();

    /**
     * Add one or more signing keys.
     *
     * @param key input stream containing encoded keys
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.KeyIsProtected if the key is password protected
     * @throws sop.exception.SOPGPException.BadData if the {@link InputStream} does not contain an OpenPGP key
     * @throws IOException in case of an IO error
     */
    T key(InputStream key) throws SOPGPException.KeyIsProtected, SOPGPException.BadData, IOException;

    /**
     * Add one or more signing keys.
     *
     * @param key byte array containing encoded keys
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.KeyIsProtected if the key is password protected
     * @throws sop.exception.SOPGPException.BadData if the byte array does not contain an OpenPGP key
     * @throws IOException in case of an IO error
     */
    default T key(byte[] key) throws SOPGPException.KeyIsProtected, SOPGPException.BadData, IOException {
        return key(new ByteArrayInputStream(key));
    }

    /**
     * Provide the password for the secret key used for signing.
     *
     * @param password password
     * @return builder instance
     */
    default T withKeyPassword(String password) {
        return withKeyPassword(password.getBytes(Charset.forName("UTF8")));
    }

    /**
     * Provide the password for the secret key used for signing.
     *
     * @param password password
     * @return builder instance
     */
    T withKeyPassword(byte[] password);

    /**
     * Signs data.
     *
     * @param data input stream containing data
     * @return ready
     *
     * @throws IOException in case of an IO error
     * @throws sop.exception.SOPGPException.ExpectedText if text data was expected, but binary data was encountered
     */
    ReadyWithResult<SigningResult> data(InputStream data) throws IOException, SOPGPException.ExpectedText;

    /**
     * Signs data.
     *
     * @param data byte array containing data
     * @return ready
     *
     * @throws IOException in case of an IO error
     * @throws sop.exception.SOPGPException.ExpectedText if text data was expected, but binary data was encountered
     */
    default ReadyWithResult<SigningResult> data(byte[] data) throws IOException, SOPGPException.ExpectedText {
        return data(new ByteArrayInputStream(data));
    }
}
