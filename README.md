<!--
SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>

SPDX-License-Identifier: Apache-2.0
-->

# SOP for Java

The [Stateless OpenPGP Protocol](https://datatracker.ietf.org/doc/html/draft-dkg-openpgp-stateless-cli-03) specification
defines a generic stateless CLI for dealing with OpenPGP messages.
Its goal is to provide a minimal, yet powerful API for the most common OpenPGP related operations.

`sop-java` defines a set of Java interfaces describing said API.

`sop-java-picocli` contains a wrapper application that transforms the `sop-java` API into a command line application
compatible with the SOP-CLI specification.

## Known Implementations
(Please expand!)

| Project                                                                               | Description                                   |
|---------------------------------------------------------------------------------------|-----------------------------------------------|
| [pgpainless-sop](https://github.com/pgpainless/pgpainless/tree/master/pgpainless-sop) | Implementation of `sop-java` using PGPainless |

### Implementations in other languages
| Project                                         | Language |
|-------------------------------------------------|----------|
| [sop-rs](https://sequoia-pgp.gitlab.io/sop-rs/) | Rust     |
| [SOP for python](https://pypi.org/project/sop/) | Python   |