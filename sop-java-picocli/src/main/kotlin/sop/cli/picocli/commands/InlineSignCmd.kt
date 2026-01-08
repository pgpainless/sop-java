// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.*
import sop.cli.picocli.SopCLI
import sop.enums.InlineSignAs
import sop.exception.SOPGPException.*

@Command(
    name = "inline-sign",
    resourceBundle = "msg_inline-sign",
    exitCodeOnInvalidInput = UnsupportedOption.EXIT_CODE)
class InlineSignCmd : AbstractSopCmd() {

    @Option(names = [OPT_NO_ARMOR], negatable = true) var armor = true

    @Option(names = [OPT_AS], paramLabel = "{binary|text|clearsigned}")
    var type: InlineSignAs? = null

    @Parameters(paramLabel = "KEYS") var secretKeyFile: List<String> = listOf()

    @Option(names = [OPT_WITH_KEY_PASSWORD], paramLabel = "PASSWORD")
    var withKeyPassword: List<String> = listOf()

    override fun run() {
        val inlineSign = throwIfUnsupportedSubcommand(SopCLI.getSop().inlineSign(), "inline-sign")

        if (!armor && type == InlineSignAs.clearsigned) {
            val errorMsg = getMsg("sop.error.usage.incompatible_options.clearsigned_no_armor")
            throw IncompatibleOptions(errorMsg)
        }

        type?.let { throwIfUnsupportedOption(OPT_AS) { inlineSign.mode(it) } }

        if (secretKeyFile.isEmpty()) {
            val errorMsg = getMsg("sop.error.usage.parameter_required", "KEYS")
            throw MissingArg(errorMsg)
        }

        for (passwordFile in withKeyPassword) {
            try {
                throwIfUnsupportedOption(OPT_WITH_KEY_PASSWORD) {
                    val password = stringFromInputStream(getInput(passwordFile))
                    inlineSign.withKeyPassword(password)
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        for (keyInput in secretKeyFile) {
            try {
                getInput(keyInput).use { keyIn -> inlineSign.key(keyIn) }
            } catch (e: IOException) {
                throw RuntimeException(e)
            } catch (e: KeyIsProtected) {
                val errorMsg = getMsg("sop.error.runtime.cannot_unlock_key", keyInput)
                throw KeyIsProtected(errorMsg, e)
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.not_a_private_key", keyInput)
                throw BadData(errorMsg, badData)
            }
        }

        if (!armor) {
            throwIfUnsupportedOption(OPT_NO_ARMOR) { inlineSign.noArmor() }
        }

        try {
            val ready = inlineSign.data(System.`in`)
            ready.writeTo(System.out)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        const val OPT_NO_ARMOR = "--no-armor"
        const val OPT_AS = "--as"
        const val OPT_WITH_KEY_PASSWORD = "--with-key-password"
    }
}
