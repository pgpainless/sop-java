// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.enums.InlineSignAs;
import sop.exception.SOPGPException;

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
    Sign mode(InlineSignAs mode) throws SOPGPException.UnsupportedOption;

}
