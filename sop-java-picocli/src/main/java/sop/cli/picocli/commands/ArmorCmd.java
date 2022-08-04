// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import picocli.CommandLine;
import sop.Ready;
import sop.cli.picocli.SopCLI;
import sop.enums.ArmorLabel;
import sop.exception.SOPGPException;
import sop.operation.Armor;

import java.io.IOException;

@CommandLine.Command(name = "armor",
        resourceBundle = "msg_armor",
        exitCodeOnInvalidInput = SOPGPException.UnsupportedOption.EXIT_CODE)
public class ArmorCmd extends AbstractSopCmd {

    @CommandLine.Option(names = {"--label"},
            paramLabel = "{auto|sig|key|cert|message}")
    ArmorLabel label;

    @Override
    public void run() {
        Armor armor = throwIfUnsupportedSubcommand(
                SopCLI.getSop().armor(),
        "armor");

        if (label != null) {
            try {
                armor.label(label);
            } catch (SOPGPException.UnsupportedOption unsupportedOption) {
                String errorMsg = getMsg("sop.error.feature_support.option_not_supported", "--label");
                throw new SOPGPException.UnsupportedOption(errorMsg, unsupportedOption);
            }
        }

        try {
            Ready ready = armor.data(System.in);
            ready.writeTo(System.out);
        } catch (SOPGPException.BadData badData) {
            String errorMsg = getMsg("sop.error.input.stdin_not_openpgp_data");
            throw new SOPGPException.BadData(errorMsg, badData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
