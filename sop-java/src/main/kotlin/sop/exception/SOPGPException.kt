// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.exception

abstract class SOPGPException : RuntimeException {

    constructor() : super()

    constructor(message: String) : super(message)

    constructor(cause: Throwable) : super(cause)

    constructor(message: String, cause: Throwable) : super(message, cause)

    abstract fun getExitCode(): Int

    /** No acceptable signatures found (sop verify, inline-verify). */
    class NoSignature : SOPGPException {
        @JvmOverloads
        constructor(message: String = "No verifiable signature found.") : super(message)

        constructor(errorMsg: String, e: NoSignature) : super(errorMsg, e)

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 3
        }
    }

    /** Asymmetric algorithm unsupported (sop encrypt, sign, inline-sign). */
    class UnsupportedAsymmetricAlgo(message: String, e: Throwable) : SOPGPException(message, e) {
        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 13
        }
    }

    /** Certificate not encryption capable (e,g, expired, revoked, unacceptable usage). */
    class CertCannotEncrypt : SOPGPException {
        constructor(message: String, cause: Throwable) : super(message, cause)

        constructor(message: String) : super(message)

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 17
        }
    }

    /** Missing required argument. */
    class MissingArg : SOPGPException {
        constructor()

        constructor(message: String) : super(message)

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 19
        }
    }

    /** Incomplete verification instructions (sop decrypt). */
    class IncompleteVerification(message: String) : SOPGPException(message) {
        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 23
        }
    }

    /** Unable to decrypt (sop decrypt). */
    class CannotDecrypt : SOPGPException {
        constructor()

        constructor(errorMsg: String, e: Throwable) : super(errorMsg, e)

        constructor(message: String) : super(message)

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 29
        }
    }

    /** Non-UTF-8 or otherwise unreliable password (sop encrypt). */
    class PasswordNotHumanReadable : SOPGPException {
        constructor() : super()

        constructor(message: String) : super(message)

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 31
        }
    }

    /** Unsupported option. */
    class UnsupportedOption : SOPGPException {
        constructor(message: String) : super(message)

        constructor(message: String, cause: Throwable) : super(message, cause)

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 37
        }
    }

    /** Invalid data type (no secret key where KEYS expected, etc.). */
    class BadData : SOPGPException {
        constructor(message: String) : super(message)

        constructor(throwable: Throwable) : super(throwable)

        constructor(message: String, throwable: Throwable) : super(message, throwable)

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 41
        }
    }

    /** Non-Text input where text expected. */
    class ExpectedText : SOPGPException {
        constructor() : super()

        constructor(message: String) : super(message)

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 53
        }
    }

    /** Output file already exists. */
    class OutputExists(message: String) : SOPGPException(message) {
        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 59
        }
    }

    /** Input file does not exist. */
    class MissingInput : SOPGPException {
        constructor(message: String, cause: Throwable) : super(message, cause)

        constructor(errorMsg: String) : super(errorMsg)

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 61
        }
    }

    /** A KEYS input is protected (locked) with a password and sop failed to unlock it. */
    class KeyIsProtected : SOPGPException {
        constructor() : super()

        constructor(message: String) : super(message)

        constructor(message: String, cause: Throwable) : super(message, cause)

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 67
        }
    }

    /** Unsupported subcommand. */
    class UnsupportedSubcommand(message: String) : SOPGPException(message) {
        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 69
        }
    }

    /**
     * An indirect parameter is a special designator (it starts with @), but sop does not know how
     * to handle the prefix.
     */
    class UnsupportedSpecialPrefix(errorMsg: String) : SOPGPException(errorMsg) {
        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 71
        }
    }

    /**
     * Exception that gets thrown if a special designator (starting with @) is given, but the
     * filesystem contains a file matching the designator.
     *
     * E.g. <pre>@ENV:FOO</pre> is given, but <pre>./@ENV:FOO</pre> exists on the filesystem.
     */
    class AmbiguousInput(message: String) : SOPGPException(message) {
        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 73
        }
    }

    /** Key not signature-capable (e.g. expired, revoked, unacceptable usage flags). */
    class KeyCannotSign : SOPGPException {
        constructor() : super()

        constructor(message: String) : super(message)

        constructor(s: String, throwable: Throwable) : super(s, throwable)

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 79
        }
    }

    /** User provided incompatible options (e.g. "--as=clearsigned --no-armor"). */
    class IncompatibleOptions : SOPGPException {
        constructor() : super()

        constructor(errorMsg: String) : super(errorMsg)

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 83
        }
    }

    /**
     * The user provided a subcommand with an unsupported profile ("--profile=XYZ"), or the user
     * tried to list profiles of a subcommand that does not support profiles at all.
     */
    class UnsupportedProfile : SOPGPException {
        /**
         * Return the subcommand name.
         *
         * @return subcommand
         */
        val subcommand: String

        /**
         * Return the profile name. May return `null`.
         *
         * @return profile name
         */
        val profile: String?

        /**
         * Create an exception signalling a subcommand that does not support any profiles.
         *
         * @param subcommand subcommand
         */
        constructor(
            subcommand: String
        ) : super("Subcommand '$subcommand' does not support any profiles.") {
            this.subcommand = subcommand
            profile = null
        }

        /**
         * Create an exception signalling a subcommand does not support a specific profile.
         *
         * @param subcommand subcommand
         * @param profile unsupported profile
         */
        constructor(
            subcommand: String,
            profile: String
        ) : super("Subcommand '$subcommand' does not support profile '$profile'.") {
            this.subcommand = subcommand
            this.profile = profile
        }

        /**
         * Wrap an exception into another instance with a possibly translated error message.
         *
         * @param errorMsg error message
         * @param e exception
         */
        constructor(errorMsg: String, e: UnsupportedProfile) : super(errorMsg, e) {
            subcommand = e.subcommand
            profile = e.profile
        }

        override fun getExitCode(): Int = EXIT_CODE

        companion object {
            const val EXIT_CODE = 89
        }
    }
}
