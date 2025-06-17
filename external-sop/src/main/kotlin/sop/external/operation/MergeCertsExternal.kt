// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.InputStream
import java.util.*
import sop.Ready
import sop.external.ExternalSOP
import sop.operation.MergeCerts

class MergeCertsExternal(binary: String, environment: Properties) : MergeCerts {

    private val commandList = mutableListOf(binary, "merge-certs")
    private val envList = ExternalSOP.propertiesToEnv(environment).toMutableList()

    private var argCount = 0

    override fun noArmor(): MergeCerts = apply { commandList.add("--no-armor") }

    override fun updates(updateCerts: InputStream): MergeCerts = apply {
        commandList.add("@ENV:CERT_$argCount")
        envList.add("CERT_$argCount=${ExternalSOP.readString(updateCerts)}")
        argCount += 1
    }

    override fun baseCertificates(certs: InputStream): Ready =
        ExternalSOP.executeTransformingOperation(Runtime.getRuntime(), commandList, envList, certs)
}
