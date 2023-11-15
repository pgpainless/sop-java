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

    @Option(names = ["--no-armor"], negatable = true) var armor = true

    @Option(names = ["--as"], paramLabel = "{binary|text|clearsigned}")
    var type: InlineSignAs? = null

    @Parameters(paramLabel = "KEYS") var secretKeyFile: List<String> = listOf()

    @Option(names = ["--with-key-password"], paramLabel = "PASSWORD")
    var withKeyPassword: List<String> = listOf()

    override fun run() {
        val inlineSign = throwIfUnsupportedSubcommand(SopCLI.getSop().inlineSign(), "inline-sign")

        if (!armor && type == InlineSignAs.clearsigned) {
            val errorMsg = getMsg("sop.error.usage.incompatible_options.clearsigned_no_armor")
            throw IncompatibleOptions(errorMsg)
        }

        type?.let {
            try {
                inlineSign.mode(it)
            } catch (unsupportedOption: UnsupportedOption) {
                val errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--as")
                throw UnsupportedOption(errorMsg, unsupportedOption)
            }
        }

        if (secretKeyFile.isEmpty()) {
            val errorMsg = getMsg("sop.error.usage.parameter_required", "KEYS")
            throw MissingArg(errorMsg)
        }

        for (passwordFile in withKeyPassword) {
            try {
                val password = stringFromInputStream(getInput(passwordFile))
                inlineSign.withKeyPassword(password)
            } catch (unsupportedOption: UnsupportedOption) {
                val errorMsg =
                    getMsg("sop.error.feature_support.option_not_supported", "--with-key-password")
                throw UnsupportedOption(errorMsg, unsupportedOption)
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
            inlineSign.noArmor()
        }

        try {
            val ready = inlineSign.data(System.`in`)
            ready.writeTo(System.out)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
