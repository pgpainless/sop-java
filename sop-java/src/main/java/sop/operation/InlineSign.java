// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.Ready;
import sop.enums.InlineSignAs;
import sop.exception.SOPGPException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface InlineSign extends AbstractSign<InlineSign> {

    /**
     * Sets the signature mode.
     * Note: This method has to be called before {@link #key(InputStream)} is called.
     *
     * @param mode signature mode
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.UnsupportedOption if this option is not supported
     */
    InlineSign mode(InlineSignAs mode)
            throws SOPGPException.UnsupportedOption;

    /**
     * Signs data.
     *
     * @param data input stream containing data
     * @return ready
     *
     * @throws IOException in case of an IO error
     * @throws sop.exception.SOPGPException.KeyIsProtected if at least one signing key cannot be unlocked
     * @throws sop.exception.SOPGPException.ExpectedText if text data was expected, but binary data was encountered
     */
    Ready data(InputStream data)
            throws IOException,
            SOPGPException.KeyIsProtected,
            SOPGPException.ExpectedText;

    /**
     * Signs data.
     *
     * @param data byte array containing data
     * @return ready
     *
     * @throws IOException in case of an IO error
     * @throws sop.exception.SOPGPException.KeyIsProtected if at least one signing key cannot be unlocked
     * @throws sop.exception.SOPGPException.ExpectedText if text data was expected, but binary data was encountered
     */
    default Ready data(byte[] data)
            throws IOException,
            SOPGPException.KeyIsProtected,
            SOPGPException.ExpectedText {
        return data(new ByteArrayInputStream(data));
    }
}
