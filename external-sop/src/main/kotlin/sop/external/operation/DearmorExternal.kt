// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.InputStream
import java.util.Properties
import sop.Ready
import sop.external.ExternalSOP
import sop.operation.Dearmor

/** Implementation of the [Dearmor] operation using an external SOP binary. */
class DearmorExternal(binary: String, environment: Properties) : Dearmor {
    private val commandList = listOf(binary, "dearmor")
    private val envList = ExternalSOP.propertiesToEnv(environment)

    override fun data(data: InputStream): Ready =
        ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, data)
}
