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
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIf("sop.testsuite.operation.AbstractSOPTest#hasBackends")
public class ChangeKeyPasswordTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return AbstractSOPTest.provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void changePasswordFromUnprotectedToProtected(SOP sop) throws IOException {
        byte[] unprotectedKey = sop.generateKey().generate().getBytes();
        byte[] password = "sw0rdf1sh".getBytes(UTF8Util.UTF8);
        byte[] protectedKey = sop.changeKeyPassword().newKeyPassphrase(password).keys(unprotectedKey).getBytes();

        sop.sign().withKeyPassword(password).key(protectedKey).data("Test123".getBytes(StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void changePasswordFromUnprotectedToUnprotected(SOP sop) throws IOException {
        byte[] unprotectedKey = sop.generateKey().noArmor().generate().getBytes();
        byte[] stillUnprotectedKey = sop.changeKeyPassword().noArmor().keys(unprotectedKey).getBytes();

        assertArrayEquals(unprotectedKey, stillUnprotectedKey);
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void changePasswordFromProtectedToUnprotected(SOP sop) throws IOException {
        byte[] password = "sw0rdf1sh".getBytes(UTF8Util.UTF8);
        byte[] protectedKey = sop.generateKey().withKeyPassword(password).generate().getBytes();
        byte[] unprotectedKey = sop.changeKeyPassword()
                .oldKeyPassphrase(password)
                .keys(protectedKey).getBytes();

        sop.sign().key(unprotectedKey).data("Test123".getBytes(StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void changePasswordFromProtectedToDifferentProtected(SOP sop) throws IOException {
        byte[] oldPassword = "sw0rdf1sh".getBytes(UTF8Util.UTF8);
        byte[] newPassword = "0r4ng3".getBytes(UTF8Util.UTF8);
        byte[] protectedKey = sop.generateKey().withKeyPassword(oldPassword).generate().getBytes();
        byte[] reprotectedKey = sop.changeKeyPassword()
                .oldKeyPassphrase(oldPassword)
                .newKeyPassphrase(newPassword)
                .keys(protectedKey).getBytes();

        sop.sign().key(reprotectedKey).withKeyPassword(newPassword).data("Test123".getBytes(StandardCharsets.UTF_8));
    }


    @ParameterizedTest
    @MethodSource("provideInstances")
    public void changePasswordWithWrongOldPasswordFails(SOP sop) throws IOException {
        byte[] oldPassword = "sw0rdf1sh".getBytes(UTF8Util.UTF8);
        byte[] newPassword = "monkey123".getBytes(UTF8Util.UTF8);
        byte[] wrongPassword = "0r4ng3".getBytes(UTF8Util.UTF8);

        byte[] protectedKey = sop.generateKey().withKeyPassword(oldPassword).generate().getBytes();
        assertThrows(SOPGPException.KeyIsProtected.class, () -> sop.changeKeyPassword()
                .oldKeyPassphrase(wrongPassword)
                .newKeyPassphrase(newPassword)
                .keys(protectedKey).getBytes());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void nonUtf8PasswordsFail(SOP sop) {
        assertThrows(SOPGPException.PasswordNotHumanReadable.class, () ->
                sop.changeKeyPassword().oldKeyPassphrase(new byte[] {(byte) 0xff, (byte) 0xfe}));
        assertThrows(SOPGPException.PasswordNotHumanReadable.class, () ->
                sop.changeKeyPassword().newKeyPassphrase(new byte[] {(byte) 0xff, (byte) 0xfe}));

    }
}
