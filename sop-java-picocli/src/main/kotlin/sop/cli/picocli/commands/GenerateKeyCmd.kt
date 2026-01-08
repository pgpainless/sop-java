// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.*
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException.UnsupportedOption
import sop.exception.SOPGPException.UnsupportedProfile

@Command(
    name = "generate-key",
    resourceBundle = "msg_generate-key",
    exitCodeOnInvalidInput = UnsupportedOption.EXIT_CODE)
class GenerateKeyCmd : AbstractSopCmd() {

    @Option(names = [OPT_NO_ARMOR], negatable = true) var armor = true

    @Parameters(paramLabel = "USERID") var userId: List<String> = listOf()

    @Option(names = [OPT_WITH_KEY_PASSWORD], paramLabel = "PASSWORD")
    var withKeyPassword: String? = null

    @Option(names = [OPT_PROFILE], paramLabel = "PROFILE") var profile: String? = null

    @Option(names = [OPT_SIGNING_ONLY]) var signingOnly: Boolean = false

    override fun run() {
        val generateKey =
            throwIfUnsupportedSubcommand(SopCLI.getSop().generateKey(), "generate-key")

        profile?.let {
            try {
                generateKey.profile(it)
            } catch (e: UnsupportedProfile) {
                val errorMsg = getMsg("sop.error.usage.profile_not_supported", "generate-key", it)
                throw UnsupportedProfile(errorMsg, e)
            }
        }

        if (signingOnly) {
            throwIfUnsupportedOption(OPT_SIGNING_ONLY) { generateKey.signingOnly() }
        }

        for (userId in userId) {
            generateKey.userId(userId)
        }

        if (!armor) {
            throwIfUnsupportedOption(OPT_NO_ARMOR) { generateKey.noArmor() }
        }

        withKeyPassword?.let {
            try {
                throwIfUnsupportedOption(OPT_WITH_KEY_PASSWORD) {
                    val password = stringFromInputStream(getInput(it))
                    generateKey.withKeyPassword(password)
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        try {
            val ready = generateKey.generate()
            ready.writeTo(System.out)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        const val OPT_NO_ARMOR = "--no-armor"
        const val OPT_WITH_KEY_PASSWORD = "--with-key-password"
        const val OPT_PROFILE = "--profile"
        const val OPT_SIGNING_ONLY = "--signing-only"
    }
}
