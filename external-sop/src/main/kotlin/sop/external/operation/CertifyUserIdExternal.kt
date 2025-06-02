// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.InputStream
import java.util.*
import sop.Ready
import sop.external.ExternalSOP
import sop.operation.CertifyUserId

class CertifyUserIdExternal(binary: String, environment: Properties) : CertifyUserId {

    private val commandList = mutableListOf(binary, "certify-userid")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var argCount = 0

    private val keys: MutableList<String> = mutableListOf()

    override fun noArmor(): CertifyUserId = apply { commandList.add("--no-armor") }

    override fun userId(userId: String): CertifyUserId = apply {
        commandList.add("--userid")
        commandList.add(userId)
    }

    override fun withKeyPassword(password: ByteArray): CertifyUserId = apply {
        commandList.add("--with-key-password=@ENV:KEY_PASSWORD_$argCount")
        envList.add("KEY_PASSWORD_$argCount=${String(password)}")
        argCount += 1
    }

    override fun noRequireSelfSig(): CertifyUserId = apply {
        commandList.add("--no-require-self-sig")
    }

    override fun keys(keys: InputStream): CertifyUserId = apply {
        this.keys.add("@ENV:KEY_$argCount")
        envList.add("KEY_$argCount=${ExternalSOP.readString(keys)}")
        argCount += 1
    }

    override fun certs(certs: InputStream): Ready =
        ExternalSOP.executeTransformingOperation(
            Runtime.getRuntime(), commandList.plus("--").plus(keys), envList, certs)
}
