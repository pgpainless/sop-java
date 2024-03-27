// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sop.testsuite.assertions.SopExecutionAssertions.assertBadData;
import static sop.testsuite.assertions.SopExecutionAssertions.assertGenericError;
import static sop.testsuite.assertions.SopExecutionAssertions.assertSuccess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sop.Ready;
import sop.SOP;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.ExtractCert;

public class ExtractCertCmdTest {

    ExtractCert extractCert;

    @BeforeEach
    public void mockComponents() throws IOException, SOPGPException.BadData {
        extractCert = mock(ExtractCert.class);
        when(extractCert.key((InputStream) any())).thenReturn(new Ready() {
            @Override
            public void writeTo(OutputStream outputStream) {
            }
        });

        SOP sop = mock(SOP.class);
        when(sop.extractCert()).thenReturn(extractCert);

        SopCLI.setSopInstance(sop);
    }

    @Test
    public void noArmor_notCalledByDefault() {
        assertSuccess(() ->
                SopCLI.execute("extract-cert"));
        verify(extractCert, never()).noArmor();
    }

    @Test
    public void noArmor_passedDown() {
        assertSuccess(() ->
                SopCLI.execute("extract-cert", "--no-armor"));
        verify(extractCert, times(1)).noArmor();
    }

    @Test
    public void key_ioExceptionCausesGenericError() throws IOException, SOPGPException.BadData {
        when(extractCert.key((InputStream) any())).thenReturn(new Ready() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                throw new IOException();
            }
        });
        assertGenericError(() ->
                SopCLI.execute("extract-cert"));
    }

    @Test
    public void key_badDataCausesBadData() throws IOException, SOPGPException.BadData {
        when(extractCert.key((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        assertBadData(() ->
                SopCLI.execute("extract-cert"));
    }
}
