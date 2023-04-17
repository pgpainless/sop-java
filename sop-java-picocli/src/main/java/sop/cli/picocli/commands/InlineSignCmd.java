// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.Ready;
import sop.cli.picocli.SopCLI;
import sop.enums.InlineSignAs;
import sop.exception.SOPGPException;
import sop.operation.InlineSign;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "inline-sign",
        resourceBundle = "msg_inline-sign",
        exitCodeOnInvalidInput = 37)
public class InlineSignCmd extends AbstractSopCmd {

    @CommandLine.Option(names = "--no-armor",
            negatable = true)
    boolean armor = true;

    @CommandLine.Option(names = "--as",
            paramLabel = "{binary|text|clearsigned}")
    InlineSignAs type;

    @CommandLine.Parameters(paramLabel = "KEYS")
    List<String> secretKeyFile = new ArrayList<>();

    @CommandLine.Option(names = "--with-key-password",
            paramLabel = "PASSWORD")
    List<String> withKeyPassword = new ArrayList<>();

    @Override
    public void run() {
        InlineSign inlineSign = throwIfUnsupportedSubcommand(
                SopCLI.getSop().inlineSign(), "inline-sign");

        // Clearsigned messages are inherently armored, so --no-armor makes no sense.
        if (!armor && type == InlineSignAs.clearsigned) {
            String errorMsg = getMsg("sop.error.usage.incompatible_options.clearsigned_no_armor");
            throw new SOPGPException.IncompatibleOptions(errorMsg);
        }

        if (type != null) {
            try {
                inlineSign.mode(type);
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--as");
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            }
        }

        if (secretKeyFile.isEmpty()) {
            String errorMsg = getMsg("sop.error.usage.parameter_required", "KEYS");
            throw new SOPGPException.MissingArg(errorMsg);
        }

        for (String passwordFile : withKeyPassword) {
            try {
                String password = stringFromInputStream(getInput(passwordFile));
                inlineSign.withKeyPassword(password);
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--with-key-password");
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (String keyInput : secretKeyFile) {
            try (InputStream keyIn = getInput(keyInput)) {
                inlineSign.key(keyIn);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SOPGPException.KeyIsProtected e) {
                String errorMsg = getMsg("sop.error.runtime.cannot_unlock_key", keyInput);
                throw new SOPGPException.KeyIsProtected(errorMsg, e);
            } catch (SOPGPException.BadData badData) {
                String errorMsg = getMsg("sop.error.input.not_a_private_key", keyInput);
                throw new SOPGPException.BadData(errorMsg, badData);
            }
        }

        if (!armor) {
            inlineSign.noArmor();
        }

        try {
            Ready ready = inlineSign.data(System.in);
            ready.writeTo(System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
