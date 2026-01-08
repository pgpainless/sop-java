// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.*
import sop.cli.picocli.SopCLI
import sop.enums.SignAs
import sop.exception.SOPGPException
import sop.exception.SOPGPException.BadData
import sop.exception.SOPGPException.KeyIsProtected

@Command(
    name = "sign",
    resourceBundle = "msg_detached-sign",
    exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
class SignCmd : AbstractSopCmd() {

    @Option(names = [OPT_NO_ARMOR], negatable = true) var armor: Boolean = true

    @Option(names = [OPT_AS], paramLabel = "{binary|text}") var type: SignAs? = null

    @Parameters(paramLabel = "KEYS") var secretKeyFile: List<String> = listOf()

    @Option(names = [OPT_WITH_KEY_PASSWORD], paramLabel = "PASSWORD")
    var withKeyPassword: List<String> = listOf()

    @Option(names = [OPT_MICALG_OUT], paramLabel = "MICALG") var micAlgOut: String? = null

    override fun run() {
        val detachedSign = throwIfUnsupportedSubcommand(SopCLI.getSop().detachedSign(), "sign")

        throwIfOutputExists(micAlgOut)
        throwIfEmptyParameters(secretKeyFile, "KEYS")

        type?.let { throwIfUnsupportedOption(OPT_AS) { detachedSign.mode(it) } }

        withKeyPassword.forEach { passIn ->
            try {
                throwIfUnsupportedOption(OPT_WITH_KEY_PASSWORD) {
                    val password = stringFromInputStream(getInput(passIn))
                    detachedSign.withKeyPassword(password)
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        secretKeyFile.forEach { keyIn ->
            try {
                getInput(keyIn).use { input -> detachedSign.key(input) }
            } catch (ioe: IOException) {
                throw RuntimeException(ioe)
            } catch (keyIsProtected: KeyIsProtected) {
                val errorMsg = getMsg("sop.error.runtime.cannot_unlock_key", keyIn)
                throw KeyIsProtected(errorMsg, keyIsProtected)
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.not_a_private_key", keyIn)
                throw BadData(errorMsg, badData)
            }
        }

        if (!armor) {
            throwIfUnsupportedOption(OPT_NO_ARMOR) { detachedSign.noArmor() }
        }

        try {
            val ready = detachedSign.data(System.`in`)
            val result = ready.writeTo(System.out)

            if (micAlgOut != null) {
                getOutput(micAlgOut).use { result.micAlg.writeTo(it) }
            }
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e)
        }
    }

    companion object {
        const val OPT_NO_ARMOR = "--no-armor"
        const val OPT_AS = "--as"
        const val OPT_WITH_KEY_PASSWORD = "--with-key-password"
        const val OPT_MICALG_OUT = "--micalg-out"
    }
}
