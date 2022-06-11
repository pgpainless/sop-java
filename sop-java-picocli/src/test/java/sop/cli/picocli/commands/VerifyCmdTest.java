// SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import sop.SOP;
import sop.Verification;
import sop.cli.picocli.SopCLI;
import sop.exception.SOPGPException;
import sop.operation.DetachedVerify;
import sop.util.UTCUtil;

public class VerifyCmdTest {

    DetachedVerify detachedVerify;
    File signature;
    File cert;

    PrintStream originalSout;

    @BeforeEach
    public void prepare() throws SOPGPException.UnsupportedOption, SOPGPException.BadData, SOPGPException.NoSignature, IOException {
        originalSout = System.out;

        detachedVerify = mock(DetachedVerify.class);
        when(detachedVerify.notBefore(any())).thenReturn(detachedVerify);
        when(detachedVerify.notAfter(any())).thenReturn(detachedVerify);
        when(detachedVerify.cert((InputStream) any())).thenReturn(detachedVerify);
        when(detachedVerify.signatures((InputStream) any())).thenReturn(detachedVerify);
        when(detachedVerify.data((InputStream) any())).thenReturn(
                Collections.singletonList(
                        new Verification(
                                UTCUtil.parseUTCDate("2019-10-29T18:36:45Z"),
                                "EB85BB5FA33A75E15E944E63F231550C4F47E38E",
                                "EB85BB5FA33A75E15E944E63F231550C4F47E38E")
                )
        );

        SOP sop = mock(SOP.class);
        when(sop.detachedVerify()).thenReturn(detachedVerify);

        SopCLI.setSopInstance(sop);

        signature = File.createTempFile("signature-", ".asc");
        cert = File.createTempFile("cert-", ".asc");
    }

    @AfterEach
    public void restoreSout() {
        System.setOut(originalSout);
    }

    @Test
    public void notAfter_passedDown() throws SOPGPException.UnsupportedOption {
        Date date = UTCUtil.parseUTCDate("2019-10-29T18:36:45Z");
        SopCLI.main(new String[] {"verify", "--not-after", "2019-10-29T18:36:45Z", signature.getAbsolutePath(), cert.getAbsolutePath()});
        verify(detachedVerify, times(1)).notAfter(date);
    }

    @Test
    public void notAfter_now() throws SOPGPException.UnsupportedOption {
        Date now = new Date();
        SopCLI.main(new String[] {"verify", "--not-after", "now", signature.getAbsolutePath(), cert.getAbsolutePath()});
        verify(detachedVerify, times(1)).notAfter(dateMatcher(now));
    }

    @Test
    public void notAfter_dashCountsAsEndOfTime() throws SOPGPException.UnsupportedOption {
        SopCLI.main(new String[] {"verify", "--not-after", "-", signature.getAbsolutePath(), cert.getAbsolutePath()});
        verify(detachedVerify, times(1)).notAfter(AbstractSopCmd.END_OF_TIME);
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.UnsupportedOption.EXIT_CODE)
    public void notAfter_unsupportedOptionCausesExit37() throws SOPGPException.UnsupportedOption {
        when(detachedVerify.notAfter(any())).thenThrow(new SOPGPException.UnsupportedOption("Setting upper signature date boundary not supported."));
        SopCLI.main(new String[] {"verify", "--not-after", "2019-10-29T18:36:45Z", signature.getAbsolutePath(), cert.getAbsolutePath()});
    }

    @Test
    public void notBefore_passedDown() throws SOPGPException.UnsupportedOption {
        Date date = UTCUtil.parseUTCDate("2019-10-29T18:36:45Z");
        SopCLI.main(new String[] {"verify", "--not-before", "2019-10-29T18:36:45Z", signature.getAbsolutePath(), cert.getAbsolutePath()});
        verify(detachedVerify, times(1)).notBefore(date);
    }

    @Test
    public void notBefore_now() throws SOPGPException.UnsupportedOption {
        Date now = new Date();
        SopCLI.main(new String[] {"verify", "--not-before", "now", signature.getAbsolutePath(), cert.getAbsolutePath()});
        verify(detachedVerify, times(1)).notBefore(dateMatcher(now));
    }

    @Test
    public void notBefore_dashCountsAsBeginningOfTime() throws SOPGPException.UnsupportedOption {
        SopCLI.main(new String[] {"verify", "--not-before", "-", signature.getAbsolutePath(), cert.getAbsolutePath()});
        verify(detachedVerify, times(1)).notBefore(AbstractSopCmd.BEGINNING_OF_TIME);
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.UnsupportedOption.EXIT_CODE)
    public void notBefore_unsupportedOptionCausesExit37() throws SOPGPException.UnsupportedOption {
        when(detachedVerify.notBefore(any())).thenThrow(new SOPGPException.UnsupportedOption("Setting lower signature date boundary not supported."));
        SopCLI.main(new String[] {"verify", "--not-before", "2019-10-29T18:36:45Z", signature.getAbsolutePath(), cert.getAbsolutePath()});
    }

    @Test
    public void notBeforeAndNotAfterAreCalledWithDefaultValues() throws SOPGPException.UnsupportedOption {
        SopCLI.main(new String[] {"verify", signature.getAbsolutePath(), cert.getAbsolutePath()});
        verify(detachedVerify, times(1)).notAfter(dateMatcher(new Date()));
        verify(detachedVerify, times(1)).notBefore(AbstractSopCmd.BEGINNING_OF_TIME);
    }

    private static Date dateMatcher(Date date) {
        return ArgumentMatchers.argThat(argument -> Math.abs(argument.getTime() - date.getTime()) < 1000);
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.MissingInput.EXIT_CODE)
    public void cert_fileNotFoundCausesExit61() {
        SopCLI.main(new String[] {"verify", signature.getAbsolutePath(), "invalid.asc"});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.BadData.EXIT_CODE)
    public void cert_badDataCausesExit41() throws SOPGPException.BadData, IOException {
        when(detachedVerify.cert((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        SopCLI.main(new String[] {"verify", signature.getAbsolutePath(), cert.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.MissingInput.EXIT_CODE)
    public void signature_fileNotFoundCausesExit61() {
        SopCLI.main(new String[] {"verify", "invalid.sig", cert.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.BadData.EXIT_CODE)
    public void signature_badDataCausesExit41() throws SOPGPException.BadData, IOException {
        when(detachedVerify.signatures((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        SopCLI.main(new String[] {"verify", signature.getAbsolutePath(), cert.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.NoSignature.EXIT_CODE)
    public void data_noSignaturesCausesExit3() throws SOPGPException.NoSignature, IOException, SOPGPException.BadData {
        when(detachedVerify.data((InputStream) any())).thenThrow(new SOPGPException.NoSignature());
        SopCLI.main(new String[] {"verify", signature.getAbsolutePath(), cert.getAbsolutePath()});
    }

    @Test
    @ExpectSystemExitWithStatus(SOPGPException.BadData.EXIT_CODE)
    public void data_badDataCausesExit41() throws SOPGPException.NoSignature, IOException, SOPGPException.BadData {
        when(detachedVerify.data((InputStream) any())).thenThrow(new SOPGPException.BadData(new IOException()));
        SopCLI.main(new String[] {"verify", signature.getAbsolutePath(), cert.getAbsolutePath()});
    }

    @Test
    public void resultIsPrintedProperly() throws SOPGPException.NoSignature, IOException, SOPGPException.BadData {
        when(detachedVerify.data((InputStream) any())).thenReturn(Arrays.asList(
                new Verification(UTCUtil.parseUTCDate("2019-10-29T18:36:45Z"),
                        "EB85BB5FA33A75E15E944E63F231550C4F47E38E",
                        "EB85BB5FA33A75E15E944E63F231550C4F47E38E"),
                new Verification(UTCUtil.parseUTCDate("2019-10-24T23:48:29Z"),
                        "C90E6D36200A1B922A1509E77618196529AE5FF8",
                        "C4BC2DDB38CCE96485EBE9C2F20691179038E5C6")
        ));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        SopCLI.main(new String[] {"verify", signature.getAbsolutePath(), cert.getAbsolutePath()});

        System.setOut(originalSout);

        String expected = "2019-10-29T18:36:45Z EB85BB5FA33A75E15E944E63F231550C4F47E38E EB85BB5FA33A75E15E944E63F231550C4F47E38E\n" +
                "2019-10-24T23:48:29Z C90E6D36200A1B922A1509E77618196529AE5FF8 C4BC2DDB38CCE96485EBE9C2F20691179038E5C6\n";

        assertEquals(expected, out.toString());
    }
}
