// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.enums.SignAs;
import sop.exception.SOPGPException;

import java.io.InputStream;

public interface Sign extends AbstractSign<Sign> {

    /**
     * Sets the signature mode.
     * Note: This method has to be called before {@link #key(InputStream)} is called.
     *
     * @param mode signature mode
     * @return builder instance
     *
     * @throws sop.exception.SOPGPException.UnsupportedOption if this option is not supported
     */
    Sign mode(SignAs mode) throws SOPGPException.UnsupportedOption;

}
