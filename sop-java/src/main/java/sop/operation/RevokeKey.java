// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.Ready;
import sop.exception.SOPGPException;
import sop.util.UTF8Util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public interface RevokeKey {

    /**
     * Disable ASCII armor encoding.
     *
     * @return builder instance
     */
    RevokeKey noArmor();

    /**
     * Provide the decryption password for the secret key.
     *
     * @param password password
     * @return builder instance
     * @throws SOPGPException.UnsupportedOption if the implementation does not support key passwords
     * @throws SOPGPException.PasswordNotHumanReadable if the password is not human-readable
     */
    default RevokeKey withKeyPassword(String password)
            throws SOPGPException.UnsupportedOption,
            SOPGPException.PasswordNotHumanReadable {
        return withKeyPassword(password.getBytes(UTF8Util.UTF8));
    }

    /**
     * Provide the decryption password for the secret key.
     *
     * @param password password
     * @return builder instance
     * @throws SOPGPException.UnsupportedOption if the implementation does not support key passwords
     * @throws SOPGPException.PasswordNotHumanReadable if the password is not human-readable
     */
    RevokeKey withKeyPassword(byte[] password)
            throws SOPGPException.UnsupportedOption,
            SOPGPException.PasswordNotHumanReadable;

    default Ready keys(byte[] bytes) {
        return keys(new ByteArrayInputStream(bytes));
    }

    Ready keys(InputStream keys);
}
