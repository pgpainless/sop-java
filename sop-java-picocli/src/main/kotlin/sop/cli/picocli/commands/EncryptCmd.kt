// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import picocli.CommandLine.*
import sop.cli.picocli.SopCLI
import sop.enums.EncryptAs
import sop.exception.SOPGPException.*

@Command(
    name = "encrypt",
    resourceBundle = "msg_encrypt",
    exitCodeOnInvalidInput = UnsupportedOption.EXIT_CODE)
class EncryptCmd : AbstractSopCmd() {

    @Option(names = ["--no-armor"], negatable = true) var armor = true

    @Option(names = ["--as"], paramLabel = "{binary|text}") var type: EncryptAs? = null

    @Option(names = ["--with-password"], paramLabel = "PASSWORD")
    var withPassword: List<String> = listOf()

    @Option(names = ["--sign-with"], paramLabel = "KEY") var signWith: List<String> = listOf()

    @Option(names = ["--with-key-password"], paramLabel = "PASSWORD")
    var withKeyPassword: List<String> = listOf()

    @Option(names = ["--profile"], paramLabel = "PROFILE") var profile: String? = null

    @Parameters(index = "0..*", paramLabel = "CERTS") var certs: List<String> = listOf()

    override fun run() {
        val encrypt = throwIfUnsupportedSubcommand(SopCLI.getSop().encrypt(), "encrypt")

        profile?.let {
            try {
                encrypt.profile(it)
            } catch (e: UnsupportedProfile) {
                val errorMsg = getMsg("sop.error.usage.profile_not_supported", "encrypt", it)
                throw UnsupportedProfile(errorMsg, e)
            }
        }

        type?.let {
            try {
                encrypt.mode(it)
            } catch (e: UnsupportedOption) {
                val errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--as")
                throw UnsupportedOption(errorMsg, e)
            }
        }

        if (withPassword.isEmpty() && certs.isEmpty()) {
            val errorMsg = getMsg("sop.error.usage.password_or_cert_required")
            throw MissingArg(errorMsg)
        }

        for (passwordFileName in withPassword) {
            try {
                val password = stringFromInputStream(getInput(passwordFileName))
                encrypt.withPassword(password)
            } catch (unsupportedOption: UnsupportedOption) {
                val errorMsg =
                    getMsg("sop.error.feature_support.option_not_supported", "--with-password")
                throw UnsupportedOption(errorMsg, unsupportedOption)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        for (passwordFileName in withKeyPassword) {
            try {
                val password = stringFromInputStream(getInput(passwordFileName))
                encrypt.withKeyPassword(password)
            } catch (unsupportedOption: UnsupportedOption) {
                val errorMsg =
                    getMsg("sop.error.feature_support.option_not_supported", "--with-key-password")
                throw UnsupportedOption(errorMsg, unsupportedOption)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        for (keyInput in signWith) {
            try {
                getInput(keyInput).use { keyIn -> encrypt.signWith(keyIn) }
            } catch (e: IOException) {
                throw RuntimeException(e)
            } catch (keyIsProtected: KeyIsProtected) {
                val errorMsg = getMsg("sop.error.runtime.cannot_unlock_key", keyInput)
                throw KeyIsProtected(errorMsg, keyIsProtected)
            } catch (unsupportedAsymmetricAlgo: UnsupportedAsymmetricAlgo) {
                val errorMsg =
                    getMsg("sop.error.runtime.key_uses_unsupported_asymmetric_algorithm", keyInput)
                throw UnsupportedAsymmetricAlgo(errorMsg, unsupportedAsymmetricAlgo)
            } catch (keyCannotSign: KeyCannotSign) {
                val errorMsg = getMsg("sop.error.runtime.key_cannot_sign", keyInput)
                throw KeyCannotSign(errorMsg, keyCannotSign)
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.not_a_private_key", keyInput)
                throw BadData(errorMsg, badData)
            }
        }

        for (certInput in certs) {
            try {
                getInput(certInput).use { certIn -> encrypt.withCert(certIn) }
            } catch (e: IOException) {
                throw RuntimeException(e)
            } catch (unsupportedAsymmetricAlgo: UnsupportedAsymmetricAlgo) {
                val errorMsg =
                    getMsg(
                        "sop.error.runtime.cert_uses_unsupported_asymmetric_algorithm", certInput)
                throw UnsupportedAsymmetricAlgo(errorMsg, unsupportedAsymmetricAlgo)
            } catch (certCannotEncrypt: CertCannotEncrypt) {
                val errorMsg = getMsg("sop.error.runtime.cert_cannot_encrypt", certInput)
                throw CertCannotEncrypt(errorMsg, certCannotEncrypt)
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.not_a_certificate", certInput)
                throw BadData(errorMsg, badData)
            }
        }

        if (!armor) {
            encrypt.noArmor()
        }

        try {
            val ready = encrypt.plaintext(System.`in`)
            ready.writeTo(System.out)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
