// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import java.io.IOException
import java.io.OutputStream

abstract class Signatures : Ready() {

    /**
     * Write OpenPGP signatures to the provided output stream.
     *
     * @param outputStream signature output stream
     * @throws IOException in case of an IO error
     */
    @Throws(IOException::class) abstract override fun writeTo(outputStream: OutputStream)
}
