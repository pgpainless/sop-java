// SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.cli.picocli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class TestFileUtil {

    public static File writeTempStringFile(String string) throws IOException {
        File tempDir = Files.createTempDirectory("tmpDir").toFile();
        tempDir.deleteOnExit();
        tempDir.mkdirs();

        File passwordFile = new File(tempDir, "file");
        passwordFile.createNewFile();

        FileOutputStream fileOut = new FileOutputStream(passwordFile);
        fileOut.write(string.getBytes(StandardCharsets.UTF_8));
        fileOut.close();

        return passwordFile;
    }
}
