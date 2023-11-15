// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.util.Properties
import sop.Ready
import sop.external.ExternalSOP
import sop.operation.GenerateKey

/** Implementation of the [GenerateKey] operation using an external SOP binary. */
class GenerateKeyExternal(binary: String, environment: Properties) : GenerateKey {

    private val commandList = mutableListOf(binary, "generate-key")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var argCounter = 0

    override fun noArmor(): GenerateKey = apply { commandList.add("--no-armor") }

    override fun userId(userId: String): GenerateKey = apply { commandList.add(userId) }

    override fun withKeyPassword(password: String): GenerateKey = apply {
        commandList.add("--with-key-password=@ENV:KEY_PASSWORD_$argCounter")
        envList.add("KEY_PASSWORD_$argCounter=$password")
        argCounter += 1
    }

    override fun profile(profile: String): GenerateKey = apply {
        commandList.add("--profile=$profile")
    }

    override fun signingOnly(): GenerateKey = apply { commandList.add("--signing-only") }

    override fun generate(): Ready =
        ExternalSOP.executeProducingOperation(Runtime.getRuntime(), commandList, envList)
}
