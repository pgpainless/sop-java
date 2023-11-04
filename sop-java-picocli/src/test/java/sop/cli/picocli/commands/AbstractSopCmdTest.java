// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli.commands;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;
import sop.cli.picocli.TestFileUtil;
import sop.exception.SOPGPException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractSopCmdTest {

    private static AbstractSopCmd abstractCmd;
    private static final TestEnvironmentVariableResolver resolver = new TestEnvironmentVariableResolver();

    @BeforeAll
    public static void setup() {
        abstractCmd = new VersionCmd(); // Use Version as representative command
        abstractCmd.setEnvironmentVariableResolver(resolver);
    }

    @Test
    public void setEnvironmentVariableResolver_nullNPE() {
        assertThrows(NullPointerException.class, () -> abstractCmd.setEnvironmentVariableResolver(null));
    }

    @Test
    public void getInput_NullInvalid() {
        assertThrows(NullPointerException.class, () -> abstractCmd.getInput(null));
    }

    @Test
    public void getInput_EmptyInvalid() {
        assertThrows(IllegalArgumentException.class, () -> abstractCmd.getInput(""));
    }

    @Test
    public void getInput_BlankInvalid() {
        assertThrows(IllegalArgumentException.class, () -> abstractCmd.getInput("    "));
    }

    @Test
    public void getInput_envNotSetIllegalArg() {
        String envName = "@ENV:IS_NOT_SET";
        assertThrows(IllegalArgumentException.class, () -> abstractCmd.getInput(envName));
    }

    @Test
    public void getInput_envEmptyIllegalArg() {
        String envName = "@ENV:IS_EMPTY";
        resolver.addEnvironmentVariable("IS_EMPTY", "");
        assertThrows(IllegalArgumentException.class, () -> abstractCmd.getInput(envName));
    }

    @Test
    public void getInput_fromEnv() throws IOException {
        resolver.addEnvironmentVariable("FOO", "BAR");
        InputStream input = abstractCmd.getInput("@ENV:FOO");
        String string = readStringFromInputStream(input);
        assertEquals("BAR", string);
    }

    private static String readStringFromInputStream(InputStream input)
            throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] buf = new byte[512];
        int read;
        while ((read = input.read(buf)) > 0) {
            output.write(buf, 0, read);
        }
        return output.toString();
    }

    @Test
    public void getInput_envClashesWithExistingFile() throws IOException {
        String env = "@ENV:existing.file";
        File tempFile = new File(env);
        if (!tempFile.createNewFile()) {
            throw new TestAbortedException("Cannot create temporary file " + tempFile.getAbsolutePath());
        }
        tempFile.deleteOnExit();

        resolver.addEnvironmentVariable("existing.file", "foo_bar");

        assertThrows(SOPGPException.AmbiguousInput.class, () -> abstractCmd.getInput(env));
    }

    @Test
    public void getInput_fdClashesWithExistingFile() throws IOException {
        String env = "@FD:existing.file";
        File tempFile = new File(env);
        if (!tempFile.createNewFile()) {
            throw new TestAbortedException("Cannot create temporary file " + tempFile.getAbsolutePath());
        }
        tempFile.deleteOnExit();

        resolver.addEnvironmentVariable("existing.file", "foo_bar");

        assertThrows(SOPGPException.AmbiguousInput.class, () -> abstractCmd.getInput(env));
    }

    @Test
    public void getInput_missingFile() {
        String missingFile = "missing.file";
        assertThrows(SOPGPException.MissingInput.class, () -> abstractCmd.getInput(missingFile));
    }

    @Test
    public void getInput_notAFile() throws IOException {
        File directory = TestFileUtil.createTempDir();
        directory.deleteOnExit();

        assertThrows(SOPGPException.MissingInput.class, () -> abstractCmd.getInput(directory.getAbsolutePath()));
    }

    @Test
    public void getOutput_NullIllegalArg() {
        assertThrows(IllegalArgumentException.class, () -> abstractCmd.getOutput(null));
    }

    @Test
    public void getOutput_EmptyIllegalArg() {
        assertThrows(IllegalArgumentException.class, () -> abstractCmd.getOutput(""));
    }

    @Test
    public void getOutput_BlankIllegalArg() {
        assertThrows(IllegalArgumentException.class, () -> abstractCmd.getOutput("  "));
    }

    @Test
    public void getOutput_envUnsupportedSpecialPrefix() {
        assertThrows(SOPGPException.UnsupportedSpecialPrefix.class, () -> abstractCmd.getOutput("@ENV:IS_ILLEGAL"));
    }

    @Test
    public void getOutput_malformedFileDescriptor() {
        assertThrows(IllegalArgumentException.class, () -> abstractCmd.getOutput("@FD:IS_ILLEGAL"));
    }

    @Test
    public void getOutput_fileExists() throws IOException {
        File testFile = TestFileUtil.createTempDir();
        testFile.deleteOnExit();

        assertThrows(SOPGPException.OutputExists.class, () -> abstractCmd.getOutput(testFile.getAbsolutePath()));
    }
}
