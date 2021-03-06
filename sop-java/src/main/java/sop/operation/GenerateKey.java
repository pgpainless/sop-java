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
     *
     * @throws sop.exception.SOPGPException.UnsupportedOption if key passwords are not supported
     * @throws sop.exception.SOPGPException.PasswordNotHumanReadable if the password is not human-readable
     */
    GenerateKey withKeyPassword(String password)
            throws SOPGPException.PasswordNotHumanReadable,
            SOPGPException.UnsupportedOption;

    /**
     * Set a password for the key.
     *
     * @param password password to protect the key
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.PasswordNotHumanReadable if the password is not human-readable
     * @throws sop.exception.SOPGPException.UnsupportedOption if key passwords are not supported
     */
    default GenerateKey withKeyPassword(byte[] password)
            throws SOPGPException.PasswordNotHumanReadable,
            SOPGPException.UnsupportedOption {
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
    Ready generate()
            throws SOPGPException.MissingArg,
            SOPGPException.UnsupportedAsymmetricAlgo,
            IOException;
}
