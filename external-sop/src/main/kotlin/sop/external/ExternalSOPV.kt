// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external

import java.nio.file.Files
import java.util.*
import sop.SOPV
import sop.external.ExternalSOP.TempDirProvider
import sop.external.operation.DetachedVerifyExternal
import sop.external.operation.InlineVerifyExternal
import sop.external.operation.VersionExternal
import sop.operation.DetachedVerify
import sop.operation.InlineVerify
import sop.operation.Version

/**
 * Implementation of the [SOPV] API subset using an external sopv/sop binary.
 *
 * Instantiate an [ExternalSOPV] object for the given binary and the given [TempDirProvider] using
 * empty environment variables.
 *
 * @param binaryName name / path of the sopv binary
 * @param tempDirProvider custom tempDirProvider
 */
class ExternalSOPV(
    private val binaryName: String,
    private val properties: Properties = Properties(),
    private val tempDirProvider: TempDirProvider = defaultTempDirProvider()
) : SOPV {

    override fun version(): Version = VersionExternal(binaryName, properties)

    override fun detachedVerify(): DetachedVerify = DetachedVerifyExternal(binaryName, properties)

    override fun inlineVerify(): InlineVerify =
        InlineVerifyExternal(binaryName, properties, tempDirProvider)

    companion object {

        /**
         * Default implementation of the [TempDirProvider] which stores temporary files in the
         * systems temp dir ([Files.createTempDirectory]).
         *
         * @return default implementation
         */
        @JvmStatic
        fun defaultTempDirProvider(): TempDirProvider {
            return TempDirProvider { Files.createTempDirectory("ext-sopv").toFile() }
        }
    }
}
