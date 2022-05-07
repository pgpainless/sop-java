<!--
SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>

SPDX-License-Identifier: Apache-2.0
-->

# SOP for Java

[![Travis (.com)](https://travis-ci.com/pgpainless/sop-java.svg?branch=master)](https://travis-ci.com/pgpainless/sop-java)
[![Maven Central](https://badgen.net/maven/v/maven-central/org.pgpainless/sop-java)](https://search.maven.org/artifact/org.pgpainless/sop-java)
[![Spec Revision: 3](https://img.shields.io/badge/Spec%20Revision-3-blue)](https://datatracker.ietf.org/doc/draft-dkg-openpgp-stateless-cli/)
[![Coverage Status](https://coveralls.io/repos/github/pgpainless/sop-java/badge.svg?branch=master)](https://coveralls.io/github/pgpainless/sop-java?branch=master)
[![REUSE status](https://api.reuse.software/badge/github.com/pgpainless/sop-java)](https://api.reuse.software/info/github.com/pgpainless/sop-java)

The [Stateless OpenPGP Protocol](https://datatracker.ietf.org/doc/draft-dkg-openpgp-stateless-cli/) specification
defines a generic stateless CLI for dealing with OpenPGP messages.
Its goal is to provide a minimal, yet powerful API for the most common OpenPGP related operations.

## Modules

The repository contains the following modules:

* [sop-java](/sop-java) defines a set of Java interfaces describing the Stateless OpenPGP Protocol.
* [sop-java-picocli](/sop-java-picocli) contains a wrapper application that transforms the `sop-java` API into a command line application
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
