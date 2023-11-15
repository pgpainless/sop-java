// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.util

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * [OutputStream] that buffers data being written into it, until its underlying output stream is
 * being replaced. At that point, first all the buffered data is being written to the underlying
 * stream, followed by any successive data that may get written to the [ProxyOutputStream]. This
 * class is useful if we need to provide an [OutputStream] at one point in time when the final
 * target output stream is not yet known.
 */
class ProxyOutputStream : OutputStream() {
    private val buffer = ByteArrayOutputStream()
    private var swapped: OutputStream? = null

    @Synchronized
    fun replaceOutputStream(underlying: OutputStream) {
        this.swapped = underlying
        swapped!!.write(buffer.toByteArray())
    }

    @Synchronized
    @Throws(IOException::class)
    override fun write(b: ByteArray) {
        if (swapped == null) {
            buffer.write(b)
        } else {
            swapped!!.write(b)
        }
    }

    @Synchronized
    @Throws(IOException::class)
    override fun write(b: ByteArray, off: Int, len: Int) {
        if (swapped == null) {
            buffer.write(b, off, len)
        } else {
            swapped!!.write(b, off, len)
        }
    }

    @Synchronized
    @Throws(IOException::class)
    override fun flush() {
        buffer.flush()
        if (swapped != null) {
            swapped!!.flush()
        }
    }

    @Synchronized
    @Throws(IOException::class)
    override fun close() {
        buffer.close()
        if (swapped != null) {
            swapped!!.close()
        }
    }

    @Synchronized
    @Throws(IOException::class)
    override fun write(i: Int) {
        if (swapped == null) {
            buffer.write(i)
        } else {
            swapped!!.write(i)
        }
    }
}
