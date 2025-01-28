// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands

import java.io.IOException
import java.io.PrintWriter
import picocli.CommandLine.*
import sop.DecryptionResult
import sop.SessionKey
import sop.SessionKey.Companion.fromString
import sop.cli.picocli.SopCLI
import sop.exception.SOPGPException.*
import sop.operation.Decrypt

@Command(
    name = "decrypt",
    resourceBundle = "msg_decrypt",
    exitCodeOnInvalidInput = UnsupportedOption.EXIT_CODE)
class DecryptCmd : AbstractSopCmd() {

    @Option(names = [OPT_SESSION_KEY_OUT], paramLabel = "SESSIONKEY")
    var sessionKeyOut: String? = null

    @Option(names = [OPT_WITH_SESSION_KEY], paramLabel = "SESSIONKEY")
    var withSessionKey: List<String> = listOf()

    @Option(names = [OPT_WITH_PASSWORD], paramLabel = "PASSWORD")
    var withPassword: List<String> = listOf()

    @Option(names = [OPT_VERIFICATIONS_OUT, "--verify-out"], paramLabel = "VERIFICATIONS")
    var verifyOut: String? = null

    @Option(names = [OPT_VERIFY_WITH], paramLabel = "CERT") var certs: List<String> = listOf()

    @Option(names = [OPT_NOT_BEFORE], paramLabel = "DATE") var notBefore = "-"

    @Option(names = [OPT_NOT_AFTER], paramLabel = "DATE") var notAfter = "now"

    @Parameters(index = "0..*", paramLabel = "KEY") var keys: List<String> = listOf()

    @Option(names = [OPT_WITH_KEY_PASSWORD], paramLabel = "PASSWORD")
    var withKeyPassword: List<String> = listOf()

    override fun run() {
        val decrypt = throwIfUnsupportedSubcommand(SopCLI.getSop().decrypt(), "decrypt")

        throwIfOutputExists(verifyOut)
        throwIfOutputExists(sessionKeyOut)

        setNotAfter(notAfter, decrypt)
        setNotBefore(notBefore, decrypt)
        setWithPasswords(withPassword, decrypt)
        setWithSessionKeys(withSessionKey, decrypt)
        setWithKeyPassword(withKeyPassword, decrypt)
        setVerifyWith(certs, decrypt)
        setDecryptWith(keys, decrypt)

        if (verifyOut != null && certs.isEmpty()) {
            val errorMsg =
                getMsg(
                    "sop.error.usage.option_requires_other_option",
                    OPT_VERIFICATIONS_OUT,
                    OPT_VERIFY_WITH)
            throw IncompleteVerification(errorMsg)
        }

        try {
            val ready = decrypt.ciphertext(System.`in`)
            val result = ready.writeTo(System.out)
            writeSessionKeyOut(result)
            writeVerifyOut(result)
        } catch (badData: BadData) {
            val errorMsg = getMsg("sop.error.input.stdin_not_a_message")
            throw BadData(errorMsg, badData)
        } catch (e: CannotDecrypt) {
            val errorMsg = getMsg("sop.error.runtime.cannot_decrypt_message")
            throw CannotDecrypt(errorMsg, e)
        } catch (ioException: IOException) {
            throw RuntimeException(ioException)
        }
    }

    @Throws(IOException::class)
    private fun writeVerifyOut(result: DecryptionResult) {
        verifyOut?.let {
            getOutput(it).use { out ->
                PrintWriter(out).use { pw ->
                    result.verifications.forEach { verification -> pw.println(verification) }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun writeSessionKeyOut(result: DecryptionResult) {
        sessionKeyOut?.let { fileName ->
            getOutput(fileName).use { out ->
                if (!result.sessionKey.isPresent) {
                    val errorMsg = getMsg("sop.error.runtime.no_session_key_extracted")
                    throw UnsupportedOption(String.format(errorMsg, OPT_SESSION_KEY_OUT))
                }

                PrintWriter(out).use { it.println(result.sessionKey.get()!!) }
            }
        }
    }

    private fun setDecryptWith(keys: List<String>, decrypt: Decrypt) {
        for (key in keys) {
            try {
                getInput(key).use { decrypt.withKey(it) }
            } catch (keyIsProtected: KeyIsProtected) {
                val errorMsg = getMsg("sop.error.runtime.cannot_unlock_key", key)
                throw KeyIsProtected(errorMsg, keyIsProtected)
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.not_a_private_key", key)
                throw BadData(errorMsg, badData)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    private fun setVerifyWith(certs: List<String>, decrypt: Decrypt) {
        for (cert in certs) {
            try {
                getInput(cert).use { certIn -> decrypt.verifyWithCert(certIn) }
            } catch (badData: BadData) {
                val errorMsg = getMsg("sop.error.input.not_a_certificate", cert)
                throw BadData(errorMsg, badData)
            } catch (ioException: IOException) {
                throw RuntimeException(ioException)
            }
        }
    }

    private fun setWithSessionKeys(withSessionKey: List<String>, decrypt: Decrypt) {
        for (sessionKeyFile in withSessionKey) {
            val sessionKeyString: String =
                try {
                    stringFromInputStream(getInput(sessionKeyFile))
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            val sessionKey: SessionKey =
                try {
                    fromString(sessionKeyString)
                } catch (e: IllegalArgumentException) {
                    val errorMsg = getMsg("sop.error.input.malformed_session_key")
                    throw IllegalArgumentException(errorMsg, e)
                }
            try {
                decrypt.withSessionKey(sessionKey)
            } catch (unsupportedOption: UnsupportedOption) {
                val errorMsg =
                    getMsg("sop.error.feature_support.option_not_supported", OPT_WITH_SESSION_KEY)
                throw UnsupportedOption(errorMsg, unsupportedOption)
            }
        }
    }

    private fun setWithPasswords(withPassword: List<String>, decrypt: Decrypt) {
        for (passwordFile in withPassword) {
            try {
                val password = stringFromInputStream(getInput(passwordFile))
                decrypt.withPassword(password)
            } catch (unsupportedOption: UnsupportedOption) {
                val errorMsg =
                    getMsg("sop.error.feature_support.option_not_supported", OPT_WITH_PASSWORD)
                throw UnsupportedOption(errorMsg, unsupportedOption)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    private fun setWithKeyPassword(withKeyPassword: List<String>, decrypt: Decrypt) {
        for (passwordFile in withKeyPassword) {
            try {
                val password = stringFromInputStream(getInput(passwordFile))
                decrypt.withKeyPassword(password)
            } catch (unsupportedOption: UnsupportedOption) {
                val errorMsg =
                    getMsg("sop.error.feature_support.option_not_supported", OPT_WITH_KEY_PASSWORD)
                throw UnsupportedOption(errorMsg, unsupportedOption)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    private fun setNotAfter(notAfter: String, decrypt: Decrypt) {
        val notAfterDate = parseNotAfter(notAfter)
        try {
            decrypt.verifyNotAfter(notAfterDate)
        } catch (unsupportedOption: UnsupportedOption) {
            val errorMsg = getMsg("sop.error.feature_support.option_not_supported", OPT_NOT_AFTER)
            throw UnsupportedOption(errorMsg, unsupportedOption)
        }
    }

    private fun setNotBefore(notBefore: String, decrypt: Decrypt) {
        val notBeforeDate = parseNotBefore(notBefore)
        try {
            decrypt.verifyNotBefore(notBeforeDate)
        } catch (unsupportedOption: UnsupportedOption) {
            val errorMsg = getMsg("sop.error.feature_support.option_not_supported", OPT_NOT_BEFORE)
            throw UnsupportedOption(errorMsg, unsupportedOption)
        }
    }

    companion object {
        const val OPT_SESSION_KEY_OUT = "--session-key-out"
        const val OPT_WITH_SESSION_KEY = "--with-session-key"
        const val OPT_WITH_PASSWORD = "--with-password"
        const val OPT_WITH_KEY_PASSWORD = "--with-key-password"
        const val OPT_VERIFICATIONS_OUT = "--verifications-out"
        const val OPT_VERIFY_WITH = "--verify-with"
        const val OPT_NOT_BEFORE = "--verify-not-before"
        const val OPT_NOT_AFTER = "--verify-not-after"
    }
}
