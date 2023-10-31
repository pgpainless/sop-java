// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.util

/**
 * Backport of java.util.Optional for older Android versions.
 *
 * @param <T> item type
 */
data class Optional<T>(val item: T? = null) {

    val isPresent: Boolean = item != null
    val isEmpty: Boolean = item == null

    fun get() = item

    companion object {
        @JvmStatic fun <T> of(item: T) = Optional(item!!)

        @JvmStatic fun <T> ofNullable(item: T?) = Optional(item)

        @JvmStatic fun <T> ofEmpty() = Optional(null as T?)
    }
}
