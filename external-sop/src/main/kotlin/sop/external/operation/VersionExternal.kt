// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.IOException
import java.util.Properties
import sop.external.ExternalSOP
import sop.operation.Version

/** Implementation of the [Version] operation using an external SOP binary. */
class VersionExternal(binary: String, environment: Properties) : Version {

    private val commandList = listOf(binary, "version")
    private val envList = ExternalSOP.propertiesToEnv(environment)

    override fun getName(): String {
        val info = executeForLine(commandList)
        return if (info.contains(" ")) {
            info.substring(0, info.lastIndexOf(" "))
        } else {
            info
        }
    }

    override fun getVersion(): String {
        val info = executeForLine(commandList)
        return if (info.contains(" ")) {
            info.substring(info.lastIndexOf(" ") + 1)
        } else {
            info
        }
    }

    override fun getBackendVersion(): String {
        return executeForLines(commandList.plus("--backend"))
    }

    override fun getExtendedVersion(): String {
        return executeForLines(commandList.plus("--extended"))
    }

    override fun getSopSpecRevisionNumber(): Int {
        val revision = getSopSpecVersion()
        val firstLine =
            if (revision.contains("\n")) {
                revision.substring(0, revision.indexOf("\n"))
            } else {
                revision
            }

        if (!firstLine.contains("-")) {
            return -1
        }
        return Integer.parseInt(firstLine.substring(firstLine.lastIndexOf("-") + 1))
    }

    override fun isSopSpecImplementationIncomplete(): Boolean {
        return getSopSpecVersion().startsWith("~")
    }

    override fun getSopSpecImplementationRemarks(): String? {
        val revision = getSopSpecVersion()
        if (revision.contains("\n")) {
            revision.substring(revision.indexOf("\n")).trim().takeIf { it.isNotBlank() }
        }
        return null
    }

    override fun getSopSpecVersion(): String {
        return executeForLines(commandList.plus("--sop-spec"))
    }

    private fun executeForLine(commandList: List<String>): String {
        return try {
            val process =
                Runtime.getRuntime().exec(commandList.toTypedArray(), envList.toTypedArray())
            val result = process.inputStream.bufferedReader().readLine()
            ExternalSOP.finish(process)
            result.trim()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun executeForLines(commandList: List<String>): String {
        return try {
            val process =
                Runtime.getRuntime().exec(commandList.toTypedArray(), envList.toTypedArray())
            val result = process.inputStream.bufferedReader().readLines().joinToString("\n")
            ExternalSOP.finish(process)
            result.trim()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
