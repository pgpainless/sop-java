<!--
SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>

SPDX-License-Identifier: Apache-2.0
-->

# Changelog

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
