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

    @Option(names = ["--no-armor"], negatable = true) var armor = true

    @Parameters(paramLabel = "USERID") var userId: List<String> = listOf()

    @Option(names = ["--with-key-password"], paramLabel = "PASSWORD")
    var withKeyPassword: String? = null

    @Option(names = ["--profile"], paramLabel = "PROFILE") var profile: String? = null

    @Option(names = ["--signing-only"]) var signingOnly: Boolean = false

    override fun run() {
        val generateKey =
            throwIfUnsupportedSubcommand(SopCLI.getSop().generateKey(), "generate-key")

        profile?.let {
            try {
                generateKey.profile(it)
            } catch (e: UnsupportedProfile) {
                val errorMsg =
                    getMsg("sop.error.usage.profile_not_supported", "generate-key", profile!!)
                throw UnsupportedProfile(errorMsg, e)
            }
        }

        if (signingOnly) {
            generateKey.signingOnly()
        }

        for (userId in userId) {
            generateKey.userId(userId)
        }

        if (!armor) {
            generateKey.noArmor()
        }

        withKeyPassword?.let {
            try {
                val password = stringFromInputStream(getInput(it))
                generateKey.withKeyPassword(password)
            } catch (e: UnsupportedOption) {
                val errorMsg =
                    getMsg("sop.error.feature_support.option_not_supported", "--with-key-password")
                throw UnsupportedOption(errorMsg, e)
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
}
