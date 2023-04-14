// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.Ready;
import sop.cli.picocli.SopCLI;
import sop.enums.EncryptAs;
import sop.exception.SOPGPException;
import sop.operation.Encrypt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "encrypt",
        resourceBundle = "msg_encrypt",
        exitCodeOnInvalidInput = 37)
public class EncryptCmd extends AbstractSopCmd {

    @CommandLine.Option(names = "--no-armor",
            negatable = true)
    boolean armor = true;

    @CommandLine.Option(names = {"--as"},
            paramLabel = "{binary|text}")
    EncryptAs type;

    @CommandLine.Option(names = "--with-password",
            paramLabel = "PASSWORD")
    List<String> withPassword = new ArrayList<>();

    @CommandLine.Option(names = "--sign-with",
            paramLabel = "KEY")
    List<String> signWith = new ArrayList<>();

    @CommandLine.Option(names = "--with-key-password",
            paramLabel = "PASSWORD")
    List<String> withKeyPassword = new ArrayList<>();

    @CommandLine.Option(names = "--profile",
            paramLabel = "PROFILE")
    String profile;

    @CommandLine.Parameters(index = "0..*",
            paramLabel = "CERTS")
    List<String> certs = new ArrayList<>();

    @Override
    public void run() {
        Encrypt encrypt = throwIfUnsupportedSubcommand(
                SopCLI.getSop().encrypt(), "encrypt");

        if (profile != null) {
            try {
                encrypt.profile(profile);
            } catch (SOPGPException.UnsupportedProfile e) {
                String errorMsg = getMsg("sop.error.usage.profile_not_supported", "encrypt", profile);
                throw new SOPGPException.UnsupportedProfile(errorMsg, e);
            }
        }

        if (type != null) {
            try {
                encrypt.mode(type);
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--as");
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            }
        }

        if (withPassword.isEmpty() && certs.isEmpty()) {
            String errorMsg = getMsg("sop.error.usage.password_or_cert_required");
            throw new SOPGPException.MissingArg(errorMsg);
        }

        for (String passwordFileName : withPassword) {
            try {
                String password = stringFromInputStream(getInput(passwordFileName));
                encrypt.withPassword(password);
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {

                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--with-password");
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (String passwordFileName : withKeyPassword) {
            try {
                String password = stringFromInputStream(getInput(passwordFileName));
                encrypt.withKeyPassword(password);
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {

                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--with-key-password");
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (String keyInput : signWith) {
            try (InputStream keyIn = getInput(keyInput)) {
                encrypt.signWith(keyIn);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SOPGPException.KeyIsProtected keyIsProtected) {
                String errorMsg = getMsg("sop.error.runtime.cannot_unlock_key", keyInput);
                throw new SOPGPException.KeyIsProtected(errorMsg, keyIsProtected);
            } catch (SOPGPException.UnsupportedAsymmetricAlgo unsupportedAsymmetricAlgo) {
                String errorMsg = getMsg("sop.error.runtime.key_uses_unsupported_asymmetric_algorithm", keyInput);
                throw new SOPGPException.UnsupportedAsymmetricAlgo(errorMsg, unsupportedAsymmetricAlgo);
            } catch (SOPGPException.KeyCannotSign keyCannotSign) {
                String errorMsg = getMsg("sop.error.runtime.key_cannot_sign", keyInput);
                throw new SOPGPException.KeyCannotSign(errorMsg, keyCannotSign);
            } catch (SOPGPException.BadData badData) {
                String errorMsg = getMsg("sop.error.input.not_a_private_key", keyInput);
                throw new SOPGPException.BadData(errorMsg, badData);
            }
        }

        for (String certInput : certs) {
            try (InputStream certIn = getInput(certInput)) {
                encrypt.withCert(certIn);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SOPGPException.UnsupportedAsymmetricAlgo unsupportedAsymmetricAlgo) {
                String errorMsg = getMsg("sop.error.runtime.cert_uses_unsupported_asymmetric_algorithm", certInput);
                throw new SOPGPException.UnsupportedAsymmetricAlgo(errorMsg, unsupportedAsymmetricAlgo);
            } catch (SOPGPException.CertCannotEncrypt certCannotEncrypt) {
                String errorMsg = getMsg("sop.error.runtime.cert_cannot_encrypt", certInput);
                throw new SOPGPException.CertCannotEncrypt(errorMsg, certCannotEncrypt);
            } catch (SOPGPException.BadData badData) {
                String errorMsg = getMsg("sop.error.input.not_a_certificate", certInput);
                throw new SOPGPException.BadData(errorMsg, badData);
            }
        }

        if (!armor) {
            encrypt.noArmor();
        }

        try {
            Ready ready = encrypt.plaintext(System.in);
            ready.writeTo(System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
