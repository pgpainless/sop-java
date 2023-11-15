// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.InputStream
import java.util.Properties
import sop.Ready
import sop.enums.InlineSignAs
import sop.external.ExternalSOP
import sop.operation.InlineSign

/** Implementation of the [InlineSign] operation using an external SOP binary. */
class InlineSignExternal(binary: String, environment: Properties) : InlineSign {

    private val commandList = mutableListOf(binary, "inline-sign")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var argCounter = 0

    override fun mode(mode: InlineSignAs): InlineSign = apply { commandList.add("--as=$mode") }

    override fun data(data: InputStream): Ready =
        ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, data)

    override fun noArmor(): InlineSign = apply { commandList.add("--no-armor") }

    override fun key(key: InputStream): InlineSign = apply {
        commandList.add("@ENV:KEY_$argCounter")
        envList.add("KEY_$argCounter=${ExternalSOP.readString(key)}")
        argCounter += 1
    }

    override fun withKeyPassword(password: ByteArray): InlineSign = apply {
        commandList.add("--with-key-password=@ENV:WITH_KEY_PASSWORD_$argCounter")
        envList.add("WITH_KEY_PASSWORD_$argCounter=${String(password)}")
        argCounter += 1
    }
}
