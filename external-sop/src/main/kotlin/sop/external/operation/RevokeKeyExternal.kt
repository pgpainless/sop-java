// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.InputStream
import java.util.Properties
import sop.Ready
import sop.external.ExternalSOP
import sop.operation.RevokeKey

/** Implementation of the [RevokeKey] operation using an external SOP binary. */
class RevokeKeyExternal(binary: String, environment: Properties) : RevokeKey {

    private val commandList = mutableListOf(binary, "revoke-key")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var argCount = 0

    override fun noArmor(): RevokeKey = apply { commandList.add("--no-armor") }

    override fun withKeyPassword(password: ByteArray): RevokeKey = apply {
        commandList.add("--with-key-password=@ENV:KEY_PASSWORD_$argCount")
        envList.add("KEY_PASSWORD_$argCount=${String(password)}")
        argCount += 1
    }

    override fun keys(keys: InputStream): Ready =
        ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, keys)
}
