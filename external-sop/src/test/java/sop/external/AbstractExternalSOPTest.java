// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sop.SOP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class AbstractExternalSOPTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExternalSOPTest.class);

    private final SOP sop;

    public AbstractExternalSOPTest() {
        String backend = readSopBackendFromProperties();
        assumeTrue(backend != null);
        Properties environment = readBackendEnvironment();
        sop = new ExternalSOP(backend, environment);
    }

    /**
     * Return the SOP backend.
     *
     * @return SOP backend
     */
    public SOP getSop() {
        return sop;
    }

    /**
     * Return <pre>true</pre> iff the specified SOP backend binary is available and accessible.
     *
     * @return true if external SOP backend is usable
     */
    public static boolean isExternalSopInstalled() {
        String binary = readSopBackendFromProperties();
        if (binary == null) {
            return false;
        }
        return new File(binary).exists();
    }

    /**
     * Relational enum.
     */
    public enum Is {
        /**
         * Less than.
         */
        le("<"),
        /**
         * Less or equal than.
         */
        leq("<="),
        /**
         * Equal.
         */
        eq("=="),
        /**
         * Not equal.
         */
        neq("!="),
        /**
         * Greater or equal than.
         */
        geq(">="),
        /**
         * Greater than.
         */
        ge(">"),
        ;

        private final String display;

        Is(String display) {
            this.display = display;
        }

        public String toDisplay() {
            return display;
        }
    }

    /**
     * Ignore a test if the tested binary version matches a version criterion.
     * Example:
     * If the installed version of example-sop is 0.1.3, <pre>ignoreIf("example-sop", Is.le, "0.1.4")</pre> will
     * make the test be ignored.
     * <pre>ignoreIf("example-sop", Is.eq, "0.1.3")</pre> will skip the test as well.
     * <pre>ignoreIf("another-sop", Is.gt, "0.0.0")</pre> will not skip the test, since the binary name does not match.
     *
     * @param name name of the binary
     * @param is relation of the version
     * @param version the reference version
     */
    public void ignoreIf(String name, Is is, String version) {
        String actualName = getSop().version().getName();
        String actualVersion = getSop().version().getVersion();

        if (!name.matches(actualName)) {
            // Name mismatch, do not ignore
            return;
        }

        ComparableVersion reference = new ComparableVersion(version);
        ComparableVersion actual = new ComparableVersion(actualVersion);

        int res = actual.compareTo(reference);
        String msg = "Skip since installed " + name + " " + actual + " " + is.toDisplay() + " " + reference;
        switch (is) {
            case le:
                assumeFalse(res < 0, msg);
                break;
            case leq:
                assumeFalse(res <= 0, msg);
            case eq:
                assumeFalse(res == 0, msg);
                break;
            case neq:
                assumeFalse(res != 0, msg);
                break;
            case geq:
                assumeFalse(res >= 0, msg);
                break;
            case ge:
                assumeFalse(res > 0, msg);
                break;
        }
    }

    private static String readSopBackendFromProperties() {
        Properties properties = new Properties();
        try {
            InputStream resourceIn = AbstractExternalSOPTest.class.getResourceAsStream("backend.local.properties");
            if (resourceIn == null) {
                LOGGER.info("Could not find backend.local.properties file. Try backend.properties instead.");
                resourceIn = AbstractExternalSOPTest.class.getResourceAsStream("backend.properties");
            }
            if (resourceIn == null) {
                throw new FileNotFoundException("Could not find backend.properties file.");
            }

            properties.load(resourceIn);
            return properties.getProperty("sop.backend");
        } catch (IOException e) {
            return null;
        }
    }

    protected static Properties readBackendEnvironment() {
        Properties properties = new Properties();
        try {
            InputStream resourceIn = AbstractExternalSOPTest.class.getResourceAsStream("backend.env");
            if (resourceIn == null) {
                LOGGER.info("Could not read backend.env file.");
            } else {
                properties.load(resourceIn);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
