// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.InputStream
import java.util.Properties
import sop.Ready
import sop.external.ExternalSOP
import sop.operation.ChangeKeyPassword

/** Implementation of the [ChangeKeyPassword] operation using an external SOP binary. */
class ChangeKeyPasswordExternal(binary: String, environment: Properties) : ChangeKeyPassword {

    private val commandList: MutableList<String> = mutableListOf(binary, "change-key-password")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var keyPasswordCounter = 0

    override fun noArmor(): ChangeKeyPassword = apply { commandList.add("--no-armor") }

    override fun oldKeyPassphrase(oldPassphrase: String): ChangeKeyPassword = apply {
        commandList.add("--old-key-password=@ENV:KEY_PASSWORD_$keyPasswordCounter")
        envList.add("KEY_PASSWORD_$keyPasswordCounter=$oldPassphrase")
        keyPasswordCounter += 1
    }

    override fun newKeyPassphrase(newPassphrase: String): ChangeKeyPassword = apply {
        commandList.add("--new-key-password=@ENV:KEY_PASSWORD_$keyPasswordCounter")
        envList.add("KEY_PASSWORD_$keyPasswordCounter=$newPassphrase")
        keyPasswordCounter += 1
    }

    override fun keys(keys: InputStream): Ready =
        ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, keys)
}
