// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import sop.ReadyWithResult
import sop.SigningResult
import sop.enums.SignAs
import sop.exception.SOPGPException.*

interface DetachedSign : AbstractSign<DetachedSign> {

    /**
     * Sets the signature mode. Note: This method has to be called before [key] is called.
     *
     * @param mode signature mode
     * @return builder instance
     * @throws UnsupportedOption if this option is not supported
     */
    @Throws(UnsupportedOption::class) fun mode(mode: SignAs): DetachedSign

    /**
     * Signs data.
     *
     * @param data input stream containing data
     * @return ready
     * @throws IOException in case of an IO error
     * @throws KeyIsProtected if at least one signing key cannot be unlocked
     * @throws ExpectedText if text data was expected, but binary data was encountered
     */
    @Throws(IOException::class, KeyIsProtected::class, ExpectedText::class)
    fun data(data: InputStream): ReadyWithResult<SigningResult>

    /**
     * Signs data.
     *
     * @param data byte array containing data
     * @return ready
     * @throws IOException in case of an IO error
     * @throws sop.exception.SOPGPException.KeyIsProtected if at least one signing key cannot be
     *   unlocked
     * @throws sop.exception.SOPGPException.ExpectedText if text data was expected, but binary data
     *   was encountered
     */
    @Throws(IOException::class, KeyIsProtected::class, ExpectedText::class)
    fun data(data: ByteArray): ReadyWithResult<SigningResult> = data(data.inputStream())
}
