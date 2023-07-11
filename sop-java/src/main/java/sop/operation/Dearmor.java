// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.util.UTF8Util;

public interface Dearmor {

    /**
     * Dearmor armored OpenPGP data.
     *
     * @param data armored OpenPGP data
     * @return input stream of unarmored data
     *
     * @throws SOPGPException.BadData in case of non-OpenPGP data
     * @throws IOException in case of an IO error
     */
    Ready data(InputStream data)
            throws SOPGPException.BadData,
            IOException;

    /**
     * Dearmor armored OpenPGP data.
     *
     * @param data armored OpenPGP data
     * @return input stream of unarmored data
     *
     * @throws SOPGPException.BadData in case of non-OpenPGP data
     * @throws IOException in case of an IO error
     */
    default Ready data(byte[] data)
            throws SOPGPException.BadData,
            IOException {
        return data(new ByteArrayInputStream(data));
    }

    /**
     * Dearmor amored OpenPGP data.
     *
     * @param data armored OpenPGP data
     * @return input stream of unarmored data
     *
     * @throws SOPGPException.BadData in case of non-OpenPGP data
     * @throws IOException in case of an IO error
     */
    default Ready data(String data)
            throws SOPGPException.BadData,
            IOException {
        return data(data.getBytes(UTF8Util.UTF8));
    }
}
