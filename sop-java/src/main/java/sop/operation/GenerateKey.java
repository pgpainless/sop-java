// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import java.io.IOException;
import java.io.InputStream;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.util.UTF8Util;

public interface GenerateKey {

    /**
     * Disable ASCII armor encoding.
     *
     * @return builder instance
     */
    GenerateKey noArmor();

    /**
     * Adds a user-id.
     *
     * @param userId user-id
     * @return builder instance
     */
    GenerateKey userId(String userId);

    /**
     * Set a password for the key.
     *
     * @param password password to protect the key
     * @return builder instance
     */
    GenerateKey withKeyPassword(String password);

    /**
     * Set a password for the key.
     *
     * @param password password to protect the key
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.PasswordNotHumanReadable in case of a non-UTF8 password
     */
    default GenerateKey withKeyPassword(byte[] password)
            throws SOPGPException.PasswordNotHumanReadable {
        return withKeyPassword(UTF8Util.decodeUTF8(password));
    }

    /**
     * Generate the OpenPGP key and return it encoded as an {@link InputStream}.
     *
     * @return key
     *
     * @throws sop.exception.SOPGPException.MissingArg if no user-id was provided
     * @throws sop.exception.SOPGPException.UnsupportedAsymmetricAlgo if the generated key uses an unsupported asymmetric algorithm
     * @throws IOException in case of an IO error
     */
    Ready generate() throws SOPGPException.MissingArg, SOPGPException.UnsupportedAsymmetricAlgo, IOException;
}
