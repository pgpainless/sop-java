// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.InputStream
import java.util.*
import sop.external.ExternalSOP
import sop.operation.ValidateUserId

class ValidateUserIdExternal(binary: String, environment: Properties) : ValidateUserId {

    private val commandList = mutableListOf(binary, "version")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var argCount = 0

    private var userId: String? = null
    private val authorities: MutableList<String> = mutableListOf()

    override fun addrSpecOnly(): ValidateUserId = apply { commandList.add("--addr-spec-only") }

    override fun userId(userId: String): ValidateUserId = apply { this.userId = userId }

    override fun authorities(certs: InputStream): ValidateUserId = apply {
        this.authorities.add("@ENV:CERT_$argCount")
        envList.add("CERT_$argCount=${ExternalSOP.readString(certs)}")
        argCount += 1
    }

    override fun subjects(certs: InputStream): Boolean {
        ExternalSOP.executeTransformingOperation(
                Runtime.getRuntime(), commandList.plus(userId!!).plus(authorities), envList, certs)
            .bytes
        return true
    }
}
