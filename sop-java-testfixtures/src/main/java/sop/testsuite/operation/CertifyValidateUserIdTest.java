// SPDX-FileCopyrightText: 2025 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.operation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sop.SOP;
import sop.exception.SOPGPException;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("sop.testsuite.operation.AbstractSOPTest#hasBackends")
public class CertifyValidateUserIdTest extends AbstractSOPTest {

    static Stream<Arguments> provideInstances() {
        return AbstractSOPTest.provideBackends();
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void certifyUserId(SOP sop) throws IOException {
        byte[] aliceKey = assumeSupported(sop::generateKey)
                .withKeyPassword("sw0rdf1sh")
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] aliceCert = assumeSupported(sop::extractCert)
                .key(aliceKey)
                .getBytes();

        byte[] bobKey = assumeSupported(sop::generateKey)
                .userId("Bob <bob@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] bobCert = assumeSupported(sop::extractCert)
                .key(bobKey)
                .getBytes();

        // Alice has her own user-id self-certified
        assertTrue(assumeSupported(sop::validateUserId)
                        .authorities(aliceCert)
                        .userId("Alice <alice@pgpainless.org>")
                        .subjects(aliceCert),
                "Alice accepts her own self-certified user-id");

        // Alice has not yet certified Bobs user-id
        assertThrows(SOPGPException.CertUserIdNoMatch.class, () ->
                        assumeSupported(sop::validateUserId)
                                .authorities(aliceCert)
                                .userId("Bob <bob@pgpainless.org>")
                                .subjects(bobCert),
                "Alice has not yet certified Bobs user-id");

        byte[] bobCertifiedByAlice = assumeSupported(sop::certifyUserId)
                .userId("Bob <bob@pgpainless.org>")
                .withKeyPassword("sw0rdf1sh")
                .keys(aliceKey)
                .certs(bobCert)
                .getBytes();

        assertTrue(assumeSupported(sop::validateUserId)
                        .userId("Bob <bob@pgpainless.org>")
                        .authorities(aliceCert)
                        .subjects(bobCertifiedByAlice),
                "Alice accepts Bobs user-id after she certified it");
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    @Disabled
    public void certifyUserIdUnarmored(SOP sop) throws IOException {
        byte[] aliceKey = assumeSupported(sop::generateKey)
                .noArmor()
                .withKeyPassword("sw0rdf1sh")
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] aliceCert = assumeSupported(sop::extractCert)
                .noArmor()
                .key(aliceKey)
                .getBytes();

        byte[] bobKey = assumeSupported(sop::generateKey)
                .noArmor()
                .userId("Bob <bob@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] bobCert = assumeSupported(sop::extractCert)
                .noArmor()
                .key(bobKey)
                .getBytes();

        byte[] bobCertifiedByAlice = assumeSupported(sop::certifyUserId)
                .noArmor()
                .userId("Bob <bob@pgpainless.org>")
                .withKeyPassword("sw0rdf1sh")
                .keys(aliceKey)
                .certs(bobCert)
                .getBytes();

        assertTrue(assumeSupported(sop::validateUserId)
                        .userId("Bob <bob@pgpainless.org>")
                        .authorities(aliceCert)
                        .subjects(bobCertifiedByAlice),
                "Alice accepts Bobs user-id after she certified it");
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void addPetName(SOP sop) throws IOException {
        byte[] aliceKey = assumeSupported(sop::generateKey)
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] aliceCert = assumeSupported(sop::extractCert)
                .key(aliceKey)
                .getBytes();

        byte[] bobKey = assumeSupported(sop::generateKey)
                .userId("Bob <bob@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] bobCert = assumeSupported(sop::extractCert)
                .key(bobKey)
                .getBytes();

        assertThrows(SOPGPException.CertUserIdNoMatch.class, () ->
                        assumeSupported(sop::certifyUserId)
                                .userId("Bobby")
                                .keys(aliceKey)
                                .certs(bobCert)
                                .getBytes(),
                "Alice cannot create a pet-name for Bob without the --no-require-self-sig flag");

        byte[] bobWithPetName = assumeSupported(sop::certifyUserId)
                .userId("Bobby")
                .noRequireSelfSig()
                .keys(aliceKey)
                .certs(bobCert)
                .getBytes();

        assertTrue(assumeSupported(sop::validateUserId)
                        .userId("Bobby")
                        .authorities(aliceCert)
                        .subjects(bobWithPetName),
                "Alice accepts the pet-name she gave to Bob");

        assertThrows(SOPGPException.CertUserIdNoMatch.class, () ->
                        assumeSupported(sop::validateUserId)
                                .userId("Bobby")
                                .authorities(bobWithPetName)
                                .subjects(bobWithPetName),
                "Bob does not accept the pet-name Alice gave him");
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void certifyWithRevokedKey(SOP sop) throws IOException {
        byte[] aliceKey = assumeSupported(sop::generateKey)
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] aliceRevokedCert = assumeSupported(sop::revokeKey)
                .keys(aliceKey)
                .getBytes();
        byte[] aliceRevokedKey = assumeSupported(sop::updateKey)
                .mergeCerts(aliceRevokedCert)
                .key(aliceKey)
                .getBytes();

        byte[] bobKey = assumeSupported(sop::generateKey)
                .userId("Bob <bob@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] bobCert = assumeSupported(sop::extractCert)
                .key(bobKey)
                .getBytes();

        assertThrows(SOPGPException.KeyCannotCertify.class, () ->
                assumeSupported(sop::certifyUserId)
                        .userId("Bob <bob@pgpainless.org>")
                        .keys(aliceRevokedKey)
                        .certs(bobCert)
                        .getBytes());
    }

    @ParameterizedTest
    @MethodSource("provideInstances")
    public void certifyValidateAddrSpecOnly(SOP sop) throws IOException {
        byte[] aliceKey = assumeSupported(sop::generateKey)
                .userId("Alice <alice@pgpainless.org>")
                .generate()
                .getBytes();
        byte[] aliceCert = assumeSupported(sop::extractCert)
                .key(aliceKey)
                .getBytes();

        assertThrows(SOPGPException.CertUserIdNoMatch.class, () ->
                assumeSupported(sop::validateUserId)
                        .authorities(aliceCert)
                        // addrSpecOnly is false, so only match exact user-ids
                        .userId("alice@pgpainless.org")
                        .subjects(aliceCert));

        assertTrue(assumeSupported(sop::validateUserId)
                .addrSpecOnly() // match the addrSpecOnly
                .authorities(aliceCert)
                .userId("alice@pgpainless.org")
                .subjects(aliceCert));
    }
}
