// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static sop.testsuite.assertions.SopExecutionAssertions.assertGenericError;
import static sop.testsuite.assertions.SopExecutionAssertions.assertUnsupportedSubcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import sop.SOP;
import sop.exception.SOPGPException;
import sop.operation.Armor;
import sop.operation.CertifyUserId;
import sop.operation.ChangeKeyPassword;
import sop.operation.Dearmor;
import sop.operation.Decrypt;
import sop.operation.InlineDetach;
import sop.operation.Encrypt;
import sop.operation.ExtractCert;
import sop.operation.GenerateKey;
import sop.operation.InlineSign;
import sop.operation.InlineVerify;
import sop.operation.DetachedSign;
import sop.operation.DetachedVerify;
import sop.operation.ListProfiles;
import sop.operation.MergeCerts;
import sop.operation.RevokeKey;
import sop.operation.UpdateKey;
import sop.operation.ValidateUserId;
import sop.operation.Version;

public class SOPTest {

    @Test
    public void assertExitOnInvalidSubcommand() {
        SOP sop = mock(SOP.class);
        SopCLI.setSopInstance(sop);

        assertUnsupportedSubcommand(() -> SopCLI.execute("invalid"));
    }

    @Test
    public void assertThrowsIfNoSOPBackendSet() {
        SopCLI.setSopInstance(null);
        // At this point, no SOP backend is set, so an InvalidStateException triggers error code 1
        assertGenericError(() -> SopCLI.execute("armor"));
    }

    @Test
    public void UnsupportedSubcommandsTest() {
        SOP nullCommandSOP = new SOP() {
            @Override
            public ValidateUserId validateUserId() {
                return null;
            }

            @Override
            public CertifyUserId certifyUserId() {
                return null;
            }

            @Override
            public MergeCerts mergeCerts() {
                return null;
            }

            @Override
            public UpdateKey updateKey() {
                return null;
            }

            @Override
            public Version version() {
                return null;
            }

            @Override
            public GenerateKey generateKey() {
                return null;
            }

            @Override
            public ExtractCert extractCert() {
                return null;
            }

            @Override
            public DetachedSign detachedSign() {
                return null;
            }

            @Override
            public DetachedVerify detachedVerify() {
                return null;
            }

            @Override
            public Encrypt encrypt() {
                return null;
            }

            @Override
            public Decrypt decrypt() {
                return null;
            }

            @Override
            public Armor armor() {
                return null;
            }

            @Override
            public Dearmor dearmor() {
                return null;
            }

            @Override
            public ListProfiles listProfiles() {
                return null;
            }

            @Override
            public RevokeKey revokeKey() {
                return null;
            }

            @Override
            public ChangeKeyPassword changeKeyPassword() {
                return null;
            }

            @Override
            public InlineDetach inlineDetach() {
                return null;
            }

            @Override
            public InlineSign inlineSign() {
                return null;
            }

            @Override
            public InlineVerify inlineVerify() {
                return null;
            }
        };
        SopCLI.setSopInstance(nullCommandSOP);

        List<String[]> commands = new ArrayList<>();
        commands.add(new String[] {"armor"});
        commands.add(new String[] {"dearmor"});
        commands.add(new String[] {"decrypt"});
        commands.add(new String[] {"inline-detach", "--signatures-out", "sigs.asc"});
        commands.add(new String[] {"encrypt"});
        commands.add(new String[] {"extract-cert"});
        commands.add(new String[] {"generate-key"});
        commands.add(new String[] {"sign"});
        commands.add(new String[] {"verify", "signature.asc", "cert.asc"});
        commands.add(new String[] {"version"});
        commands.add(new String[] {"list-profiles", "generate-key"});
        commands.add(new String[] {"certify-userid", "--userid", "Alice <alice@pgpainless.org>", "--", "alice.pgp"});
        commands.add(new String[] {"validate-userid", "Alice <alice@pgpainless.org>", "bob.pgp", "--", "alice.pgp"});
        commands.add(new String[] {"update-key"});
        commands.add(new String[] {"merge-certs"});

        for (String[] command : commands) {
            int exit = SopCLI.execute(command);
            assertEquals(SOPGPException.UnsupportedSubcommand.EXIT_CODE, exit,
                    "Unexpected exit code for non-implemented command " + Arrays.toString(command) + ": " + exit);
        }
    }
}
