// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import java.io.PrintWriter
import picocli.CommandLine.*
import sop.cli.picocli.SopCLI
import sop.enums.EncryptAs
import sop.enums.EncryptFor
import sop.exception.SOPGPException.*

@Command(
    name = "encrypt",
    resourceBundle = "msg_encrypt",
    exitCodeOnInvalidInput = UnsupportedOption.EXIT_CODE)
class EncryptCmd : AbstractSopCmd() {

    @Option(names = [OPT_NO_ARMOR], negatable = true) var armor = true

    @Option(names = [OPT_AS], paramLabel = "{binary|text}") var type: EncryptAs? = null

    @Option(names = [OPT_FOR], paramLabel = "{storage|communications|any}")
    var purpose: EncryptFor? = null

    @Option(names = [OPT_WITH_PASSWORD], paramLabel = "PASSWORD")
    var withPassword: List<String> = listOf()

    @Option(names = [OPT_SIGN_WITH], paramLabel = "KEY") var signWith: List<String> = listOf()

    @Option(names = [OPT_WITH_KEY_PASSWORD], paramLabel = "PASSWORD")
    var withKeyPassword: List<String> = listOf()

    @Option(names = [OPT_PROFILE], paramLabel = "PROFILE") var profile: String? = null

    @Parameters(index = "0..*", paramLabel = "CERTS") var certs: List<String> = listOf()

    @Option(names = [OPT_SESSION_KEY_OUT], paramLabel = "SESSIONKEY")
    var sessionKeyOut: String? = null

    override fun run() {
        val encrypt = throwIfUnsupportedSubcommand(SopCLI.getSop().encrypt(), "encrypt")

        throwIfOutputExists(sessionKeyOut)

        profile?.let {
            try {
                encrypt.profile(it)
            } catch (e: UnsupportedProfile) {
                val errorMsg = getMsg("sop.error.usage.profile_not_supported", "encrypt", it)
                throw UnsupportedProfile(errorMsg, e)
            }
        }

        type?.let { throwIfUnsupportedOption(OPT_AS) { encrypt.mode(it) } }

        purpose?.let { throwIfUnsupportedOption(OPT_FOR) { encrypt.encryptFor(it) } }

        if (withPassword.isEmpty() && certs.isEmpty()) {
            val errorMsg = getMsg("sop.error.usage.password_or_cert_required")
            throw MissingArg(errorMsg)
        }

        for (passwordFileName in withPassword) {
            try {
                throwIfUnsupportedOption(OPT_WITH_PASSWORD) {
                    val password = stringFromInputStream(getInput(passwordFileName))
                    encrypt.withPassword(password)
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        for (passwordFileName in withKeyPassword) {
            try {
                throwIfUnsupportedOption(OPT_WITH_KEY_PASSWORD) {
                    val password = stringFromInputStream(getInput(passwordFileName))
                    encrypt.withKeyPassword(password)
                }
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
            throwIfUnsupportedOption(OPT_NO_ARMOR) { encrypt.noArmor() }
        }

        try {
            val ready = encrypt.plaintext(System.`in`)
            val result = ready.writeTo(System.out)

            if (sessionKeyOut == null) {
                return
            }

            getOutput(sessionKeyOut).use {
                if (!result.sessionKey.isPresent) {
                    val errorMsg = getMsg("sop.error.runtime.no_session_key_extracted")
                    throw UnsupportedOption(String.format(errorMsg, "--session-key-out"))
                }
                val sessionKey = result.sessionKey.get() ?: return
                val writer = PrintWriter(it)
                writer.println(sessionKey)
                writer.flush()
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        const val OPT_NO_ARMOR = "--no-armor"
        const val OPT_AS = "--as"
        const val OPT_FOR = "--for"
        const val OPT_WITH_PASSWORD = "--with-password"
        const val OPT_SIGN_WITH = "--sign-with"
        const val OPT_WITH_KEY_PASSWORD = "--with-key-password"
        const val OPT_PROFILE = "--profile"
        const val OPT_SESSION_KEY_OUT = "--session-key-out"
    }
}
