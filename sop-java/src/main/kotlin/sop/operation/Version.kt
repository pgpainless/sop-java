// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation

import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.jvm.Throws
import sop.exception.SOPGPException

interface Version {

    /**
     * Return the implementations name. e.g. `SOP`,
     *
     * @return implementation name
     */
    fun getName(): String

    /**
     * Return the implementations short version string. e.g. `1.0`
     *
     * @return version string
     */
    fun getVersion(): String

    /**
     * Return version information about the used OpenPGP backend. e.g. `Bouncycastle 1.70`
     *
     * @return backend version string
     */
    fun getBackendVersion(): String

    /**
     * Return an extended version string containing multiple lines of version information. The first
     * line MUST match the information produced by [getName] and [getVersion], but the rest of the
     * text has no defined structure. Example:
     * ```
     * "SOP 1.0
     * Awesome PGP!
     * Using Bouncycastle 1.70
     * LibFoo 1.2.2
     * See https://pgp.example.org/sop/ for more information"
     * ```
     *
     * @return extended version string
     */
    fun getExtendedVersion(): String

    /**
     * Return the revision of the SOP specification that this implementation is implementing, for
     * example, `draft-dkg-openpgp-stateless-cli-06`. If the implementation targets a specific draft
     * but the implementer knows the implementation is incomplete, it should prefix the draft title
     * with a `~` (TILDE, U+007E), for example: `~draft-dkg-openpgp-stateless-cli-06`. The
     * implementation MAY emit additional text about its relationship to the targeted draft on the
     * lines following the versioned title.
     *
     * @return implemented SOP spec version
     */
    fun getSopSpecVersion(): String {
        return buildString {
            if (isSopSpecImplementationIncomplete()) append('~')
            append(getSopSpecRevisionName())
            if (getSopSpecImplementationRemarks() != null) {
                append('\n')
                append('\n')
                append(getSopSpecImplementationRemarks())
            }
        }
    }

    /**
     * Return the version number of the latest targeted SOP spec revision.
     *
     * @return SOP spec revision number
     */
    fun getSopSpecRevisionNumber(): Int

    /**
     * Return the name of the latest targeted revision of the SOP spec.
     *
     * @return SOP spec revision string
     */
    fun getSopSpecRevisionName(): String = buildString {
        append("draft-dkg-openpgp-stateless-cli-")
        append(String.format("%02d", getSopSpecRevisionNumber()))
    }

    /**
     * Return <pre>true</pre>, if this implementation of the SOP spec is known to be incomplete or
     * defective.
     *
     * @return true if incomplete, false otherwise
     */
    fun isSopSpecImplementationIncomplete(): Boolean

    /**
     * Return free-form text containing remarks about the completeness of the SOP implementation. If
     * there are no remarks, this method returns <pre>null</pre>.
     *
     * @return remarks or null
     */
    fun getSopSpecImplementationRemarks(): String?

    /**
     * Return the single-line SEMVER version of the sopv interface subset it provides complete
     * coverage of. If the implementation does not provide complete coverage for any sopv interface,
     * this method throws an [SOPGPException.UnsupportedOption] instead.
     */
    @Throws(SOPGPException.UnsupportedOption::class) fun getSopVVersion(): String

    /** Return the current version of the SOP-Java library. */
    fun getSopJavaVersion(): String? {
        return try {
            val resourceIn: InputStream =
                Version::class.java.getResourceAsStream("/sop-java-version.properties")
                    ?: throw IOException("File sop-java-version.properties not found.")
            val properties = Properties().apply { load(resourceIn) }
            properties.getProperty("sop-java-version")
        } catch (e: IOException) {
            "DEVELOPMENT"
        }
    }
}
