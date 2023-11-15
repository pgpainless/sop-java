// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.InputStream
import java.util.Properties
import sop.Ready
import sop.enums.ArmorLabel
import sop.exception.SOPGPException
import sop.external.ExternalSOP
import sop.operation.Armor

/** Implementation of the [Armor] operation using an external SOP binary. */
class ArmorExternal(binary: String, environment: Properties) : Armor {

    private val commandList: MutableList<String> = mutableListOf(binary, "armor")
    private val envList: List<String> = ExternalSOP.propertiesToEnv(environment)

    override fun label(label: ArmorLabel): Armor = apply { commandList.add("--label=$label") }

    @Throws(SOPGPException.BadData::class)
    override fun data(data: InputStream): Ready =
        ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, data)
}
