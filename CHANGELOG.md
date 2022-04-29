<!--
SPDX-FileCopyrightText: 2022 Paul Schaub <vanitasvitae@fsfe.org>

SPDX-License-Identifier: Apache-2.0
-->

# Changelog

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
