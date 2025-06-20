<!--
SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>

SPDX-License-Identifier: Apache-2.0
-->

# Changelog

## 14.0.0
- Update implementation to [SOP Specification revision 14](https://www.ietf.org/archive/id/draft-dkg-openpgp-stateless-cli-14.html), 
  including changes from revisions `11`, `12`, `13`, `14`.
  - Implement newly introduced operations
    - `update-key` 'fixes' everything wrong with a key
    - `merge-certs` merges a certificate with other copies
    - `certify-userid` create signatures over user-ids on certificates
    - `validate-userid` validate signatures over user-ids
  - Add new exceptions
    - `UnspecificFailure` maps generic application errors
    - `KeyCannotCertify` signals that a key cannot be used for third-party certifications
    - `NoHardwareKeyFound` signals that a key backed by a hardware device cannot be found
    - `HardwareKeyFailure` signals a hardware device failure
    - `PrimaryKeyBad` signals an unusable or bad primary key
    - `CertUserIdNoMatch` signals that a user-id cannot be found/validated on a certificate
  - `Verification`: Add support for JSON description extensions
- Remove `animalsniffer` from build dependencies
- Bump `logback` to `1.5.13`

## 10.1.1
- Prepare jar files for use in native images, e.g. using GraalVM by generating and including
  configuration files for reflection, resources and dynamic proxies.
- gradle: Make use of jvmToolchain functionality
- gradle: Improve reproducibility
- gradle: Bump animalsniffer to `2.0.0`

## 10.1.0
- `sop-java`:
  - Remove `label()` option from `armor()` subcommand
  - Move test-fixtures artifact built with the `testFixtures` plugin into
    its own module `sop-java-testfixtures`, which can be consumed by maven builds.
- `sop-java-picocli`:
  - Properly map `MissingParameterException` to `MissingArg` exit code
  - As a workaround for native builds using graalvm:
    - Do not re-set message bundles dynamically (fails in native builds)
    - Prevent an unmatched argument error

## 10.0.3
- CLI `change-key-password`: Fix indirect parameter passing for new and old passwords (thanks to @dkg for the report)
- Backport: `revoke-key`: Allow for multiple password options

## 10.0.2
- Downgrade `logback-core` to `1.2.13`

## 10.0.1
- Remove `label()` option from `Armor` operation
- Fix exit code for 'Missing required option/parameter' error
- Fix `revoke-key`: Allow for multiple invocations of `--with-key-password` option
- Fix `EncryptExternal` use of `--sign-with` parameter
- Fix `NullPointerException` in `DecryptExternal` when reading lines
- Fix `DecryptExternal` use of `verifications-out`
- Test suite: Ignore tests if `UnsupportedOption` is thrown
- Bump `logback-core` to `1.4.14`

## 10.0.0
- Update implementation to [SOP Specification revision 10](https://www.ietf.org/archive/id/draft-dkg-openpgp-stateless-cli-10.html).
  - Throw `BadData` when passing KEYS where CERTS are expected
  - Introduce `sopv` interface subset with revision `1.0`
  - Add `sop version --sopv`

## 8.0.2
- CLI `change-key-password`: Fix indirect parameter passing for new and old passwords (thanks to @dkg for the report)
- Backport: `revoke-key`: Allow for multiple password options

## 8.0.1
- `decrypt`: Do not throw `NoSignature` exception (exit code 3) if `--verify-with` is provided, but `VERIFICATIONS` is empty.

## 8.0.0
- Rewrote `sop-java` in Kotlin
- Rewrote `sop-java-picocli` in Kotlin
- Rewrote `external-sop` in Kotlin
- Update implementation to [SOP Specification revision 08](https://www.ietf.org/archive/id/draft-dkg-openpgp-stateless-cli-08.html).
  - Add `--no-armor` option to `revoke-key` and `change-key-password` subcommands
  - `armor`: Deprecate `--label` option in `sop-java` and remove in `sop-java-picocli`
  - `encrypt`: Add `--session-key-out` option
- Slight API changes:
  - `sop.encrypt().plaintext()` now returns a `ReadyWithResult<EncryptionResult>` instead of `Ready`.
  - `EncryptionResult` is a new result type, that provides access to the session key of an encrypted message
  - Change `ArmorLabel` values into lowercase
  - Change `EncryptAs` values into lowercase
  - Change `SignAs` values into lowercase

## 7.0.2
- CLI `change-key-password`: Fix indirect parameter passing for new and old passwords (thanks to @dkg for the report)
- Backport: revoke-key command: Allow for multiple '--with-key-password' options

## 7.0.1
- `decrypt`: Do not throw `NoSignature` exception (exit code 3) if `--verify-with` is provided, but `VERIFICATIONS` is empty.

## 7.0.0
- Update implementation to [SOP Specification revision 07](https://www.ietf.org/archive/id/draft-dkg-openpgp-stateless-cli-07.html).
  - Add support for new `revoke-key` subcommand
  - Add support for new `change-key-password` subcommand
  - Add support for new `--signing-only` option of `generate-key` subcommand
- Add `dearmor.data(String)` utility method
- Fix typos in, and improve i18n of CLI help pages

## 6.1.0
- `listProfiles()`: Add shortcut methods `generateKey()` and `encrypt()`
- Add DSL for testing `Verification` results
- `Verification`
  - Return `Optional<SignatureMode>` for `getSignatureMode()`
  - Return `Optional<String>` for `getDescription()`
- `Profile`
  - Add support for profiles without description
  - Return `Optional<String>` for `getDescription()`
  - Add `parse(String)` method for parsing profile lines
- `sop-java`: Add dependency on `com.google.code.findbugs:jsr305` for `@Nullable`, `@Nonnull` annotations
- `UTCUtil`: `parseUTCDate()` is now `@Nonnull` and throws a `ParseException` for invalid inputs
- `UTF8Util`: `decodeUTF8()` now throws `CharacterCodingException` instead of `SOPGPException.PasswordNotHumanReadable`
- `external-sop`: Properly map error codes to new exception types (ported from `5.0.1`):
  - `UNSUPPORTED_PROFILE`
  - `INCOMPATIBLE_OPTIONS`

## 5.0.1
- `external-sop`: Properly map error codes to new exception types:
  - `UNSUPPORTED_PROFILE`
  - `INCOMPATIBLE_OPTIONS`

## 6.0.0
- Update implementation to [SOP Specification revision 06](https://www.ietf.org/archive/id/draft-dkg-openpgp-stateless-cli-06.html).
  - Add option `--profile=XYZ` to `encrypt` subcommand
  - Add option `--sop-spec` to `version` subcommand
  - `Version`: Add different getters for specification-related values

## 5.0.0
- Update implementation to [SOP Specification revision 05](https://www.ietf.org/archive/id/draft-dkg-openpgp-stateless-cli-05.html).
  - Add the concept of profiles
  - Add `list-profiles` subcommand
  - Add option `--profile=XYZ` to `generate-key` subcommand
  - `Verification` objects can now optionally indicate the type of the signature (`mode:text` or `mode:binary`)
  - `Verification` objects can now contain an optional description of the signature
  - `inline-sign` now throws an error if incompatible options `--as=clearsigned` and `--no-armor` are used

## 4.1.1
- Restructure test suite to allow simultaneous testing of multiple backends
- Fix IOException in `sop sign` due to premature stream closing
- Allow for downstream implementations of `sop-java` to reuse the test suite
  - Check out Javadoc of `sop-java/src/testFixtures/java/sop/testsuite/SOPInstanceFactory` for details

## 4.1.0
- Add module `external-sop`
  - This module implements the `sop-java` interfaces and allows the use of an external SOP binary
- `decrypt`: Rename `--not-before`, `--not-after` to `--verify-not-before`, `--verify-not-after`
- `decrypt`: Throw `NoSignature` error if no verifiable signature found, but signature verification is requested using `--verify-with`.
- `inline-sign`: Fix parameter label of `--as=clearsigned`
- `ArmorLabel`, `EncryptAs`, `SignAs`: make `toString()` return lowercase

## 4.0.7
- Make i18n string for `--stacktrace` option translatable
- Make manpages generation reproducible
- `dearmor`: Transform `IOException` into `BadData`

## 4.0.6
- Add support for file descriptors on unix / linux systems

## 4.0.5
- `inline-sign`: Make possible values of `--as` option lowercase
- `inline-sign`: Rename value `cleartextsigned` of option `--as` to `clearsigned`

## 4.0.4
- Not found

## 4.0.3
- `decrypt`: Rename option `--verify-out` to `--verifications-out`, but keep `--verify-out` as alias
- Fix: `decrypt`: Flush output stream in order to prevent empty file as result of `--session-key-out`
- Fix: Properly format session key for `--session-key-out`
- Be less finicky about input session key formats
  - Allow upper- and lowercase hexadecimal keys
  - Allow trailing whitespace

## 4.0.2
- Fix: `verify`: Do not include detached signature in list of certificates
- Fix: `inline-verify`: Also include the first argument in list of certificates
- Hide stacktraces by default and add `--stacktrace` option to print them
- Properly throw `CannotDecrypt` exception when message could not be decrypted

## 4.0.1
- Use shared resources for i18n
  - Fix strings not being resolved properly when downstream renames `sop` command

## 4.0.0
- Switch to new versioning format to indicate implemented SOP version
- Implement SOP specification version 04
  - Add `--with-key-password` to `sop generate-key`
  - Add `--with-key-password` to `sop sign`
  - Add `--with-key-password` to `sop encrypt`
  - Add `--with-key-password` to `sop decrypt`
  - Rename `sop detach-inband-signature-and-message` to `sop inline-detach`
  - `sop inline-detach`: Add support for inline-signed messages
  - Implement `sop inline-sign`
  - Implement `sop inline-verify`
- Rename `Sign` to `DetachedSign`
- Rename `Verify` to `DetachedVerify`
- `SignAs`: Remove `Mime` option
- `sop-java-picocli`: Implement i18n and add German translation

## 1.2.3
- Bump Mockito version to `4.5.1`

## 1.2.2
- Add SOP parent command name and description

## 1.2.1
- Bump dependencies
  - `com.ginsberg:junit5-system-exit` from `1.1.1` to `1.1.2`
  - `org.mockito:mockito-core` from `4.2.0` to `4.3.1`
  - `info.picocli:picocli` from `4.6.2` to `4.6.3`
- Add hidden `generate-completion` subcommand
- Document exit codes

## 1.2.0
- `encrypt`, `decrypt`: Interpret arguments of `--with-password` and `--with-session-key` as indirect data types (e.g. file references instead of strings)

## 1.1.0
- Initial release from new repository
- Implement SOP specification version 3
