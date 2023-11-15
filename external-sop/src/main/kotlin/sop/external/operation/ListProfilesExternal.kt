// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external.operation

import java.io.IOException
import java.util.Properties
import sop.Profile
import sop.external.ExternalSOP
import sop.operation.ListProfiles

/** Implementation of the [ListProfiles] operation using an external SOP binary. */
class ListProfilesExternal(binary: String, environment: Properties) : ListProfiles {

    private val commandList = mutableListOf(binary, "list-profiles")
    private val envList = ExternalSOP.propertiesToEnv(environment)

    override fun subcommand(command: String): List<Profile> {
        return try {
            String(
                    ExternalSOP.executeProducingOperation(
                            Runtime.getRuntime(), commandList.plus(command), envList)
                        .bytes)
                .let { toProfiles(it) }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        @JvmStatic
        private fun toProfiles(output: String): List<Profile> =
            output.split("\n").filter { it.isNotBlank() }.map { Profile.parse(it) }
    }
}
