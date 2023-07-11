// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;
import sop.exception.SOPGPException;
import sop.util.UTF8Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIf("sop.testsuite.operation.AbstractSOPTest#hasBackends")
public class RevokeKeyTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void revokeUnprotectedKey(SOP sop) throws IOException {
        byte[] secretKey = sop.generateKey().userId("Alice <alice@pgpainless.org>").generate().getBytes();
        byte[] revocation = sop.revokeKey().keys(secretKey).getBytes();

        assertFalse(Arrays.equals(secretKey, revocation));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void revokeProtectedKey(SOP sop) throws IOException {
        byte[] password = "sw0rdf1sh".getBytes(UTF8Util.UTF8);
        byte[] secretKey = sop.generateKey().withKeyPassword(password).userId("Alice <alice@pgpainless.org>").generate().getBytes();
        byte[] revocation = sop.revokeKey().withKeyPassword(password).keys(secretKey).getBytes();

        assertFalse(Arrays.equals(secretKey, revocation));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void revokeProtectedKeyWithMultiplePasswordOptions(SOP sop) throws IOException {
        byte[] password = "sw0rdf1sh".getBytes(UTF8Util.UTF8);
        byte[] wrongPassword = "0r4ng3".getBytes(UTF8Util.UTF8);
        byte[] secretKey = sop.generateKey().withKeyPassword(password).userId("Alice <alice@pgpainless.org>").generate().getBytes();
        byte[] revocation = sop.revokeKey().withKeyPassword(wrongPassword).withKeyPassword(password).keys(secretKey).getBytes();

        assertFalse(Arrays.equals(secretKey, revocation));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void revokeProtectedKeyWithMissingPassphraseFails(SOP sop) throws IOException {
        byte[] password = "sw0rdf1sh".getBytes(UTF8Util.UTF8);
        byte[] secretKey = sop.generateKey().withKeyPassword(password).userId("Alice <alice@pgpainless.org>").generate().getBytes();

        assertThrows(SOPGPException.KeyIsProtected.class, () -> sop.revokeKey().keys(secretKey).getBytes());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void revokeProtectedKeyWithWrongPassphraseFails(SOP sop) throws IOException {
        byte[] password = "sw0rdf1sh".getBytes(UTF8Util.UTF8);
        String wrongPassword = "or4ng3";
        byte[] secretKey = sop.generateKey().withKeyPassword(password).userId("Alice <alice@pgpainless.org>").generate().getBytes();

        assertThrows(SOPGPException.KeyIsProtected.class, () -> sop.revokeKey().withKeyPassword(wrongPassword).keys(secretKey).getBytes());
    }
}
