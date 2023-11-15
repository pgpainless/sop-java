// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.InputStream
import java.util.Properties
import sop.Ready
import sop.external.ExternalSOP
import sop.operation.ExtractCert

/** Implementation of the [ExtractCert] operation using an external SOP binary. */
class ExtractCertExternal(binary: String, environment: Properties) : ExtractCert {

    private val commandList = mutableListOf(binary, "extract-cert")
    private val envList = ExternalSOP.propertiesToEnv(environment)

    override fun noArmor(): ExtractCert = apply { commandList.add("--no-armor") }

    override fun key(keyInputStream: InputStream): Ready =
        ExternalSOP.executeTransformingOperation(
            Runtime.getRuntime(), commandList, envList, keyInputStream)
}
