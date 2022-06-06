// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.MicAlg;
import sop.ReadyWithResult;
import sop.SigningResult;
import sop.cli.picocli.SopCLI;
import sop.enums.SignAs;
import sop.exception.SOPGPException;
import sop.operation.DetachedSign;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "sign",
        resourceBundle = "sop",
        exitCodeOnInvalidInput = 37)
public class SignCmd extends AbstractSopCmd {

    @CommandLine.Option(names = "--no-armor",
            descriptionKey = "sop.sign.usage.option.armor",
            negatable = true)
    boolean armor = true;

    @CommandLine.Option(names = "--as",
            descriptionKey = "sop.sign.usage.option.as",
            paramLabel = "{binary|text}")
    SignAs type;

    @CommandLine.Parameters(descriptionKey = "sop.sign.usage.parameter.keys",
            paramLabel = "KEYS")
    List<String> secretKeyFile = new ArrayList<>();

    @CommandLine.Option(names = "--with-key-password",
            descriptionKey = "sop.sign.usage.option.with_key_password",
            paramLabel = "PASSWORD")
    List<String> withKeyPassword = new ArrayList<>();

    @CommandLine.Option(names = "--micalg-out",
            descriptionKey = "sop.sign.usage.option.micalg_out",
            paramLabel = "MICALG")
    String micAlgOut;

    @Override
    public void run() {
        DetachedSign detachedSign = throwIfUnsupportedSubcommand(
                SopCLI.getSop().detachedSign(), "sign");

        throwIfOutputExists(micAlgOut);
        throwIfEmptyParameters(secretKeyFile, "KEYS");

        if (type != null) {
            try {
                detachedSign.mode(type);
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--as");
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            }
        }

        for (String passwordFile : withKeyPassword) {
            try {
                String password = stringFromInputStream(getInput(passwordFile));
                detachedSign.withKeyPassword(password);
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--with-key-password");
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (String keyInput : secretKeyFile) {
            try (InputStream keyIn = getInput(keyInput)) {
                detachedSign.key(keyIn);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SOPGPException.KeyIsProtected keyIsProtected) {
                String errorMsg = getMsg("sop.error.runtime.cannot_unlock_key", keyInput);
                throw new SOPGPException.KeyIsProtected(errorMsg, keyIsProtected);
            } catch (SOPGPException.BadData badData) {
                String errorMsg = getMsg("sop.error.input.not_a_private_key", keyInput);
                throw new SOPGPException.BadData(errorMsg, badData);
            }
        }

        if (!armor) {
            detachedSign.noArmor();
        }

        try {
            ReadyWithResult<SigningResult> ready = detachedSign.data(System.in);
            SigningResult result = ready.writeTo(System.out);

            MicAlg micAlg = result.getMicAlg();
            if (micAlgOut != null) {
                // Write micalg out
                OutputStream outputStream = getOutput(micAlgOut);
                micAlg.writeTo(outputStream);
                outputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
