// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sop.ReadyWithResult;
import sop.SOP;
import sop.Signatures;
import sop.cli.picocli.SopCLI;
import sop.cli.picocli.TestFileUtil;
import sop.exception.SOPGPException;
import sop.operation.InlineDetach;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sop.testsuite.assertions.SopExecutionAssertions.assertMissingArg;
import static sop.testsuite.assertions.SopExecutionAssertions.assertSuccess;

public class InlineDetachCmdTest {

    InlineDetach inlineDetach;

    @BeforeEach
    public void mockComponents() {
        inlineDetach = mock(InlineDetach.class);

        SOP sop = mock(SOP.class);
        when(sop.inlineDetach()).thenReturn(inlineDetach);
        SopCLI.setSopInstance(sop);
    }

    @Test
    public void testMissingSignaturesOutResultsInMissingArg() {
        assertMissingArg(() ->
                SopCLI.execute("inline-detach"));
    }

    @Test
    public void testNoArmorIsCalled() throws IOException {
        // Create temp dir and allocate non-existing tempfile for sigout
        File tempDir = TestFileUtil.createTempDir();
        File tempFile = new File(tempDir, "sigs.out");
        tempFile.deleteOnExit();

        // mock inline-detach
        when(inlineDetach.message((InputStream) any()))
                .thenReturn(new ReadyWithResult<Signatures>() {
                    @Override
                    public Signatures writeTo(OutputStream outputStream) throws SOPGPException.NoSignature {
                        return new Signatures() {
                            @Override
                            public void writeTo(OutputStream signatureOutputStream) throws IOException {
                                signatureOutputStream.write("Signatures!\n".getBytes(StandardCharsets.UTF_8));
                            }
                        };
                    }
                });

        assertSuccess(() ->
                SopCLI.execute("inline-detach", "--signatures-out", tempFile.getAbsolutePath(), "--no-armor"));
        verify(inlineDetach, times(1)).noArmor();
        verify(inlineDetach, times(1)).message((InputStream) any());
    }
}
