// SPDX-FileCopyrightText: 2024 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException.*

@Command(
    name = "update-key",
    resourceBundle = "msg_update-key",
    exitCodeOnInvalidInput = UnsupportedOption.EXIT_CODE)
class UpdateKeyCmd : AbstractSopCmd() {

    @Option(names = [OPT_NO_ARMOR], negatable = true) var armor = true

    @Option(names = [OPT_SIGNING_ONLY]) var signingOnly = false

    @Option(names = [OPT_NO_ADDED_CAPABILITIES]) var noAddedCapabilities = false

    @Option(names = [OPT_REVOKE_DEPRECATED_KEYS]) var revokeDeprecatedKeys = false

    @Option(names = [OPT_WITH_KEY_PASSWORD], paramLabel = "PASSWORD")
    var withKeyPassword: List<String> = listOf()

    @Option(names = [OPT_MERGE_CERTS], paramLabel = "CERTS") var mergeCerts: List<String> = listOf()

    override fun run() {
        val updateKey = throwIfUnsupportedSubcommand(SopCLI.getSop().updateKey(), "update-key")

        if (!armor) {
            throwIfUnsupportedOption(OPT_NO_ARMOR) { updateKey.noArmor() }
        }

        if (signingOnly) {
            throwIfUnsupportedOption(OPT_SIGNING_ONLY) { updateKey.signingOnly() }
        }

        if (noAddedCapabilities) {
            throwIfUnsupportedOption(OPT_NO_ADDED_CAPABILITIES) { updateKey.noAddedCapabilities() }
        }

        if (revokeDeprecatedKeys) {
            throwIfUnsupportedOption(OPT_REVOKE_DEPRECATED_KEYS) {
                updateKey.revokeDeprecatedKeys()
            }
        }

        for (passwordFileName in withKeyPassword) {
            try {
                throwIfUnsupportedOption(OPT_WITH_KEY_PASSWORD) {
                    val password = stringFromInputStream(getInput(passwordFileName))
                    updateKey.withKeyPassword(password)
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        for (certInput in mergeCerts) {
            try {
                getInput(certInput).use { updateKey.mergeCerts(it) }
            } catch (e: IOException) {
                throw RuntimeException(e)
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.not_a_certificate", certInput)
                throw BadData(errorMsg, badData)
            }
        }

        try {
            val ready = updateKey.key(System.`in`)
            ready.writeTo(System.out)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (badData: BadData) {
            val errorMsg = getMsg("sop.error.input.not_a_private_key", "STDIN")
            throw BadData(errorMsg, badData)
        }
    }

    companion object {
        const val OPT_NO_ARMOR = "--no-armor"
        const val OPT_SIGNING_ONLY = "--signing-only"
        const val OPT_NO_ADDED_CAPABILITIES = "--no-added-capabilities"
        const val OPT_REVOKE_DEPRECATED_KEYS = "--revoke-deprecated-keys"
        const val OPT_WITH_KEY_PASSWORD = "--with-key-password"
        const val OPT_MERGE_CERTS = "--merge-certs"
    }
}
