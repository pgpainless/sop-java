<!--
SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>

SPDX-License-Identifier: Apache-2.0
-->

# SOP for Java

[![status-badge](https://ci.codeberg.org/api/badges/PGPainless/sop-java/status.svg)](https://ci.codeberg.org/PGPainless/sop-java)
[![Spec Revision: 4](https://img.shields.io/badge/Spec%20Revision-4-blue)](https://datatracker.ietf.org/doc/draft-dkg-openpgp-stateless-cli/04/)
[![Coverage Status](https://coveralls.io/repos/github/pgpainless/sop-java/badge.svg?branch=main)](https://coveralls.io/github/pgpainless/sop-java?branch=main)
[![REUSE status](https://api.reuse.software/badge/github.com/pgpainless/sop-java)](https://api.reuse.software/info/github.com/pgpainless/sop-java)

The [Stateless OpenPGP Protocol](https://datatracker.ietf.org/doc/draft-dkg-openpgp-stateless-cli/) specification
defines a generic stateless CLI for dealing with OpenPGP messages.
Its goal is to provide a minimal, yet powerful API for the most common OpenPGP related operations.

[![Packaging status](https://repology.org/badge/vertical-allrepos/sop-java.svg)](https://repology.org/project/pgpainless/versions)
[![Maven Central](https://badgen.net/maven/v/maven-central/org.pgpainless/sop-java)](https://search.maven.org/artifact/org.pgpainless/sop-java)

## Modules

The repository contains the following modules:

* [sop-java](/sop-java) defines a set of Java interfaces describing the Stateless OpenPGP Protocol.
* [sop-java-picocli](/sop-java-picocli) contains a wrapper application that transforms the `sop-java` API into a command line application
compatible with the SOP-CLI specification.

## Known Implementations
(Please expand!)

| Project                                                                             | Description                                                                              |
|-------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|
| [pgpainless-sop](https://github.com/pgpainless/pgpainless/tree/main/pgpainless-sop) | Implementation of `sop-java` using PGPainless                                            |
| [external-sop](https://github.com/pgpainless/sop-java/tree/main/external-sop)       | Implementation of `sop-java` that allows binding to external SOP binaries such as `sqop` |

### Implementations in other languages
| Project                                         | Language |
|-------------------------------------------------|----------|
| [sop-rs](https://sequoia-pgp.gitlab.io/sop-rs/) | Rust     |
| [SOP for python](https://pypi.org/project/sop/) | Python   |
