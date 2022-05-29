// SPDX-FileCopyrightText: 2020 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import picocli.CommandLine;
import sop.DecryptionResult;
import sop.ReadyWithResult;
import sop.SessionKey;
import sop.Verification;
import sop.cli.picocli.DateParser;
import sop.cli.picocli.FileUtil;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.Decrypt;
import sop.util.HexUtil;

@CommandLine.Command(name = "decrypt",
        description = "Decrypt a message from standard input",
        exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
public class DecryptCmd extends AbstractSopCmd {

    private static final String OPT_WITH_SESSION_KEY = "--with-session-key";
    private static final String OPT_WITH_PASSWORD = "--with-password";
    private static final String OPT_NOT_BEFORE = "--not-before";
    private static final String OPT_NOT_AFTER = "--not-after";
    private static final String OPT_SESSION_KEY_OUT = "--session-key-out";
    private static final String OPT_VERIFY_OUT = "--verify-out";
    private static final String OPT_VERIFY_WITH = "--verify-with";
    private static final String OPT_WITH_KEY_PASSWORD = "--with-key-password";


    @CommandLine.Option(
            names = {OPT_SESSION_KEY_OUT},
            description = "Can be used to learn the session key on successful decryption",
            paramLabel = "SESSIONKEY")
    File sessionKeyOut;

    @CommandLine.Option(
            names = {OPT_WITH_SESSION_KEY},
            description = "Provide a session key file. Enables decryption of the \"CIPHERTEXT\" using the session key directly against the \"SEIPD\" packet",
            paramLabel = "SESSIONKEY")
    List<String> withSessionKey = new ArrayList<>();

    @CommandLine.Option(
            names = {OPT_WITH_PASSWORD},
            description = "Provide a password file. Enables decryption based on any \"SKESK\" packets in the \"CIPHERTEXT\"",
            paramLabel = "PASSWORD")
    List<String> withPassword = new ArrayList<>();

    @CommandLine.Option(names = {OPT_VERIFY_OUT},
            description = "Produces signature verification status to the designated file",
            paramLabel = "VERIFICATIONS")
    File verifyOut;

    @CommandLine.Option(names = {OPT_VERIFY_WITH},
            description = "Certificates whose signatures would be acceptable for signatures over this message",
            paramLabel = "CERT")
    List<File> certs = new ArrayList<>();

    @CommandLine.Option(names = {OPT_NOT_BEFORE},
            description = "ISO-8601 formatted UTC date (eg. '2020-11-23T16:35Z)\n" +
                    "Reject signatures with a creation date not in range.\n" +
                    "Defaults to beginning of time (\"-\").",
            paramLabel = "DATE")
    String notBefore = "-";

    @CommandLine.Option(names = {OPT_NOT_AFTER},
            description = "ISO-8601 formatted UTC date (eg. '2020-11-23T16:35Z)\n" +
                    "Reject signatures with a creation date not in range.\n" +
                    "Defaults to current system time (\"now\").\n" +
                    "Accepts special value \"-\" for end of time.",
            paramLabel = "DATE")
    String notAfter = "now";

    @CommandLine.Parameters(index = "0..*",
            description = "Secret keys to attempt decryption with",
            paramLabel = "KEY")
    List<File> keys = new ArrayList<>();

    @CommandLine.Option(names = {OPT_WITH_KEY_PASSWORD},
    description = "Provide indirect file type pointing at passphrase(s) for secret key(s)",
    paramLabel = "PASSWORD")
    List<String> withKeyPassword = new ArrayList<>();

    @Override
    public void run() {
        Decrypt decrypt = throwIfUnsupportedSubcommand(
                SopCLI.getSop().decrypt(), "decrypt");

        throwIfOutputExists(verifyOut, OPT_VERIFY_OUT);
        throwIfOutputExists(sessionKeyOut, OPT_SESSION_KEY_OUT);

        setNotAfter(notAfter, decrypt);
        setNotBefore(notBefore, decrypt);
        setWithPasswords(withPassword, decrypt);
        setWithSessionKeys(withSessionKey, decrypt);
        setWithKeyPassword(withKeyPassword, decrypt);
        setVerifyWith(certs, decrypt);
        setDecryptWith(keys, decrypt);

        if (verifyOut != null && certs.isEmpty()) {
            String errorMessage = "Option %s is requested, but no option %s was provided.";
            throw new SOPGPException.IncompleteVerification(String.format(errorMessage, OPT_VERIFY_OUT, OPT_VERIFY_WITH));
        }

        try {
            ReadyWithResult<DecryptionResult> ready = decrypt.ciphertext(System.in);
            DecryptionResult result = ready.writeTo(System.out);
            writeSessionKeyOut(result);
            writeVerifyOut(result);
        } catch (SOPGPException.BadData badData) {
            throw new SOPGPException.BadData("No valid OpenPGP message found on Standard Input.", badData);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    private void writeVerifyOut(DecryptionResult result) throws IOException {
        if (verifyOut != null) {
            FileUtil.createNewFileOrThrow(verifyOut);
            try (FileOutputStream outputStream = new FileOutputStream(verifyOut)) {
                PrintWriter writer = new PrintWriter(outputStream);
                for (Verification verification : result.getVerifications()) {
                    // CHECKSTYLE:OFF
                    writer.println(verification.toString());
                    // CHECKSTYLE:ON
                }
                writer.flush();
            }
        }
    }

    private void writeSessionKeyOut(DecryptionResult result) throws IOException {
        if (sessionKeyOut != null) {
            FileUtil.createNewFileOrThrow(sessionKeyOut);

            try (FileOutputStream outputStream = new FileOutputStream(sessionKeyOut)) {
                if (!result.getSessionKey().isPresent()) {
                    String errorMsg = "Session key not extracted. Possibly the feature %s is not supported.";
                    throw new SOPGPException.UnsupportedOption(String.format(errorMsg, OPT_SESSION_KEY_OUT));
                } else {
                    SessionKey sessionKey = result.getSessionKey().get();
                    outputStream.write(sessionKey.getAlgorithm());
                    outputStream.write(sessionKey.getKey());
                }
            }
        }
    }

    private void setDecryptWith(List<File> keys, Decrypt decrypt) {
        for (File key : keys) {
            try (FileInputStream keyIn = new FileInputStream(key)) {
                decrypt.withKey(keyIn);
            } catch (SOPGPException.KeyIsProtected keyIsProtected) {
                throw new SOPGPException.KeyIsProtected("Key in file " + key.getAbsolutePath() + " is password protected.", keyIsProtected);
            } catch (SOPGPException.BadData badData) {
                throw new SOPGPException.BadData("File " + key.getAbsolutePath() + " does not contain a private key.", badData);
            } catch (FileNotFoundException e) {
                throw new SOPGPException.MissingInput(String.format(ERROR_FILE_NOT_EXIST, key.getAbsolutePath()), e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setVerifyWith(List<File> certs, Decrypt decrypt) {
        for (File cert : certs) {
            try (FileInputStream certIn = new FileInputStream(cert)) {
                decrypt.verifyWithCert(certIn);
            } catch (FileNotFoundException e) {
                throw new SOPGPException.MissingInput(String.format(ERROR_FILE_NOT_EXIST, cert.getAbsolutePath()), e);
            } catch (SOPGPException.BadData badData) {
                throw new SOPGPException.BadData("File " + cert.getAbsolutePath() + " does not contain a valid certificate.", badData);
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }
        }
    }

    private void setWithSessionKeys(List<String> withSessionKey, Decrypt decrypt) {
        Pattern sessionKeyPattern = Pattern.compile("^\\d+:[0-9A-F]+$");
        for (String sessionKeyFile : withSessionKey) {
            String sessionKey;
            try {
                sessionKey = FileUtil.stringFromInputStream(FileUtil.getFileInputStream(sessionKeyFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!sessionKeyPattern.matcher(sessionKey).matches()) {
                throw new IllegalArgumentException("Session keys are expected in the format 'ALGONUM:HEXKEY'.");
            }
            String[] split = sessionKey.split(":");
            byte algorithm = (byte) Integer.parseInt(split[0]);
            byte[] key = HexUtil.hexToBytes(split[1]);

            try {
                decrypt.withSessionKey(new SessionKey(algorithm, key));
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                throw new SOPGPException.UnsupportedOption(String.format(ERROR_UNSUPPORTED_OPTION, OPT_WITH_SESSION_KEY), unsupportedOption);
            }
        }
    }

    private void setWithPasswords(List<String> withPassword, Decrypt decrypt) {
        for (String passwordFile : withPassword) {
            try {
                String password = FileUtil.stringFromInputStream(FileUtil.getFileInputStream(passwordFile));
                decrypt.withPassword(password);
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                throw new SOPGPException.UnsupportedOption(String.format(ERROR_UNSUPPORTED_OPTION, OPT_WITH_PASSWORD), unsupportedOption);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setWithKeyPassword(List<String> withKeyPassword, Decrypt decrypt) {
        for (String passwordFile : withKeyPassword) {
            try {
                String password = FileUtil.stringFromInputStream(FileUtil.getFileInputStream(passwordFile));
                decrypt.withKeyPassword(password);
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                throw new SOPGPException.UnsupportedOption(String.format(ERROR_UNSUPPORTED_OPTION, OPT_WITH_KEY_PASSWORD), unsupportedOption);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setNotAfter(String notAfter, Decrypt decrypt) {
        Date notAfterDate = DateParser.parseNotAfter(notAfter);
        try {
            decrypt.verifyNotAfter(notAfterDate);
        } catch (SOPGPException.UnsupportedOption unsupportedOption) {
            throw new SOPGPException.UnsupportedOption(String.format(ERROR_UNSUPPORTED_OPTION, OPT_NOT_AFTER), unsupportedOption);
        }
    }

    private void setNotBefore(String notBefore, Decrypt decrypt) {
        Date notBeforeDate = DateParser.parseNotBefore(notBefore);
        try {
            decrypt.verifyNotBefore(notBeforeDate);
        } catch (SOPGPException.UnsupportedOption unsupportedOption) {
            throw new SOPGPException.UnsupportedOption(String.format(ERROR_UNSUPPORTED_OPTION, OPT_NOT_BEFORE), unsupportedOption);
        }
    }
}
