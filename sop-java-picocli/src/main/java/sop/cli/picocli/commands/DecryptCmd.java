// SPDX-FileCopyrightText: 2020 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.DecryptionResult;
import sop.ReadyWithResult;
import sop.SessionKey;
import sop.Verification;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.Decrypt;
import sop.util.HexUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@CommandLine.Command(name = "decrypt",
        resourceBundle = "msg_decrypt",
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
            paramLabel = "SESSIONKEY")
    String sessionKeyOut;

    @CommandLine.Option(
            names = {OPT_WITH_SESSION_KEY},
            paramLabel = "SESSIONKEY")
    List<String> withSessionKey = new ArrayList<>();

    @CommandLine.Option(
            names = {OPT_WITH_PASSWORD},
            paramLabel = "PASSWORD")
    List<String> withPassword = new ArrayList<>();

    @CommandLine.Option(names = {OPT_VERIFY_OUT},
            paramLabel = "VERIFICATIONS")
    String verifyOut;

    @CommandLine.Option(names = {OPT_VERIFY_WITH},
            paramLabel = "CERT")
    List<String> certs = new ArrayList<>();

    @CommandLine.Option(names = {OPT_NOT_BEFORE},
            paramLabel = "DATE")
    String notBefore = "-";

    @CommandLine.Option(names = {OPT_NOT_AFTER},
            paramLabel = "DATE")
    String notAfter = "now";

    @CommandLine.Parameters(index = "0..*",
            paramLabel = "KEY")
    List<String> keys = new ArrayList<>();

    @CommandLine.Option(names = {OPT_WITH_KEY_PASSWORD},
            paramLabel = "PASSWORD")
    List<String> withKeyPassword = new ArrayList<>();

    @Override
    public void run() {
        Decrypt decrypt = throwIfUnsupportedSubcommand(
                SopCLI.getSop().decrypt(), "decrypt");

        throwIfOutputExists(verifyOut);
        throwIfOutputExists(sessionKeyOut);

        setNotAfter(notAfter, decrypt);
        setNotBefore(notBefore, decrypt);
        setWithPasswords(withPassword, decrypt);
        setWithSessionKeys(withSessionKey, decrypt);
        setWithKeyPassword(withKeyPassword, decrypt);
        setVerifyWith(certs, decrypt);
        setDecryptWith(keys, decrypt);

        if (verifyOut != null && certs.isEmpty()) {
            String errorMsg = getMsg("sop.error.usage.option_requires_other_option", OPT_VERIFY_OUT, OPT_VERIFY_WITH);
            throw new SOPGPException.IncompleteVerification(errorMsg);
        }

        try {
            ReadyWithResult<DecryptionResult> ready = decrypt.ciphertext(System.in);
            DecryptionResult result = ready.writeTo(System.out);
            writeSessionKeyOut(result);
            writeVerifyOut(result);
        } catch (SOPGPException.BadData badData) {
            String errorMsg = getMsg("sop.error.input.stdin_not_a_message");
            throw new SOPGPException.BadData(errorMsg, badData);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    private void writeVerifyOut(DecryptionResult result) throws IOException {
        if (verifyOut != null) {
            try (OutputStream fileOut = getOutput(verifyOut)) {
                PrintWriter writer = new PrintWriter(fileOut);
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
            try (OutputStream outputStream = getOutput(sessionKeyOut)) {
                if (!result.getSessionKey().isPresent()) {
                    String errorMsg = getMsg("sop.error.runtime.no_session_key_extracted");
                    throw new SOPGPException.UnsupportedOption(String.format(errorMsg, OPT_SESSION_KEY_OUT));
                } else {
                    SessionKey sessionKey = result.getSessionKey().get();
                    outputStream.write(sessionKey.getAlgorithm());
                    outputStream.write(sessionKey.getKey());
                }
            }
        }
    }

    private void setDecryptWith(List<String> keys, Decrypt decrypt) {
        for (String key : keys) {
            try (InputStream keyIn = getInput(key)) {
                decrypt.withKey(keyIn);
            } catch (SOPGPException.KeyIsProtected keyIsProtected) {
                String errorMsg = getMsg("sop.error.runtime.cannot_unlock_key", key);
                throw new SOPGPException.KeyIsProtected(errorMsg, keyIsProtected);
            } catch (SOPGPException.BadData badData) {
                String errorMsg = getMsg("sop.error.input.not_a_private_key", key);
                throw new SOPGPException.BadData(errorMsg, badData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setVerifyWith(List<String> certs, Decrypt decrypt) {
        for (String cert : certs) {
            try (InputStream certIn = getInput(cert)) {
                decrypt.verifyWithCert(certIn);
            } catch (SOPGPException.BadData badData) {
                String errorMsg = getMsg("sop.error.input.not_a_certificate", cert);
                throw new SOPGPException.BadData(errorMsg, badData);
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
                sessionKey = stringFromInputStream(getInput(sessionKeyFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!sessionKeyPattern.matcher(sessionKey).matches()) {
                String errorMsg = getMsg("sop.error.input.malformed_session_key");
                throw new IllegalArgumentException(errorMsg);
            }
            String[] split = sessionKey.split(":");
            byte algorithm = (byte) Integer.parseInt(split[0]);
            byte[] key = HexUtil.hexToBytes(split[1]);

            try {
                decrypt.withSessionKey(new SessionKey(algorithm, key));
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", OPT_WITH_SESSION_KEY);
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            }
        }
    }

    private void setWithPasswords(List<String> withPassword, Decrypt decrypt) {
        for (String passwordFile : withPassword) {
            try {
                String password = stringFromInputStream(getInput(passwordFile));
                decrypt.withPassword(password);
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {

                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", OPT_WITH_PASSWORD);
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setWithKeyPassword(List<String> withKeyPassword, Decrypt decrypt) {
        for (String passwordFile : withKeyPassword) {
            try {
                String password = stringFromInputStream(getInput(passwordFile));
                decrypt.withKeyPassword(password);
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {

                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", OPT_WITH_KEY_PASSWORD);
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setNotAfter(String notAfter, Decrypt decrypt) {
        Date notAfterDate = parseNotAfter(notAfter);
        try {
            decrypt.verifyNotAfter(notAfterDate);
        } catch (SOPGPException.UnsupportedOption unsupportedOption) {
            String errorMsg = getMsg("sop.error.feature_support.option_not_supported", OPT_NOT_AFTER);
            throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
        }
    }

    private void setNotBefore(String notBefore, Decrypt decrypt) {
        Date notBeforeDate = parseNotBefore(notBefore);
        try {
            decrypt.verifyNotBefore(notBeforeDate);
        } catch (SOPGPException.UnsupportedOption unsupportedOption) {
            String errorMsg = getMsg("sop.error.feature_support.option_not_supported", OPT_NOT_BEFORE);
            throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
        }
    }
}
