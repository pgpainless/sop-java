// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.InputStream
import java.util.*
import sop.Ready
import sop.external.ExternalSOP
import sop.operation.UpdateKey

class UpdateKeyExternal(binary: String, environment: Properties) : UpdateKey {

    private val commandList = mutableListOf(binary, "update-key")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var argCount = 0

    override fun noArmor(): UpdateKey = apply { commandList.add("--no-armor") }

    override fun signingOnly(): UpdateKey = apply { commandList.add("--signing-only") }

    override fun noNewMechanisms(): UpdateKey = apply { commandList.add("--no-new-mechanisms") }

    override fun withKeyPassword(password: ByteArray): UpdateKey = apply {
        commandList.add("--with-key-password=@ENV:KEY_PASSWORD_$argCount")
        envList.add("KEY_PASSWORD_$argCount=${String(password)}")
        argCount += 1
    }

    override fun mergeCerts(certs: InputStream): UpdateKey = apply {
        commandList.add("--merge-certs")
        commandList.add("@ENV:CERT_$argCount")
        envList.add("CERT_$argCount=${ExternalSOP.readString(certs)}")
        argCount += 1
    }

    override fun key(key: InputStream): Ready =
        ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, key)
}
