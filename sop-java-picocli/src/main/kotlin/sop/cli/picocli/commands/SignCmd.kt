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

    @Option(names = ["--no-armor"], negatable = true) var armor: Boolean = true

    @Option(names = ["--as"], paramLabel = "{binary|text}") var type: SignAs? = null

    @Parameters(paramLabel = "KEYS") var secretKeyFile: List<String> = listOf()

    @Option(names = ["--with-key-password"], paramLabel = "PASSWORD")
    var withKeyPassword: List<String> = listOf()

    @Option(names = ["--micalg-out"], paramLabel = "MICALG") var micAlgOut: String? = null

    override fun run() {
        val detachedSign = throwIfUnsupportedSubcommand(SopCLI.getSop().detachedSign(), "sign")

        throwIfOutputExists(micAlgOut)
        throwIfEmptyParameters(secretKeyFile, "KEYS")

        try {
            type?.let { detachedSign.mode(it) }
        } catch (unsupported: SOPGPException.UnsupportedOption) {
            val errorMsg =
                getMsg("sop.error.feature_support.option_not_supported", "--with-key-password")
            throw SOPGPException.UnsupportedOption(errorMsg, unsupported)
        } catch (ioe: IOException) {
            throw RuntimeException(ioe)
        }

        withKeyPassword.forEach { passIn ->
            try {
                val password = stringFromInputStream(getInput(passIn))
                detachedSign.withKeyPassword(password)
            } catch (unsupported: SOPGPException.UnsupportedOption) {
                val errorMsg =
                    getMsg("sop.error.feature_support.option_not_supported", "--with-key-password")
                throw SOPGPException.UnsupportedOption(errorMsg, unsupported)
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
            detachedSign.noArmor()
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
}
