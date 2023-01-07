// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.external;

import sop.SOP;
import sop.external.operation.ExtractCertExternal;
import sop.external.operation.GenerateKeyExternal;
import sop.external.operation.VersionExternal;
import sop.operation.Armor;
import sop.operation.Dearmor;
import sop.operation.Decrypt;
import sop.operation.DetachedSign;
import sop.operation.DetachedVerify;
import sop.operation.Encrypt;
import sop.operation.ExtractCert;
import sop.operation.GenerateKey;
import sop.operation.InlineDetach;
import sop.operation.InlineSign;
import sop.operation.InlineVerify;
import sop.operation.Version;

public class ExternalSOP implements SOP {

    private final String binaryName;

    public ExternalSOP(String binaryName) {
        this.binaryName = binaryName;
    }

    @Override
    public Version version() {
        return new VersionExternal(binaryName);
    }

    @Override
    public GenerateKey generateKey() {
        return new GenerateKeyExternal(binaryName);
    }

    @Override
    public ExtractCert extractCert() {
        return new ExtractCertExternal(binaryName);
    }

    @Override
    public DetachedSign detachedSign() {
        return null;
    }

    @Override
    public InlineSign inlineSign() {
        return null;
    }

    @Override
    public DetachedVerify detachedVerify() {
        return null;
    }

    @Override
    public InlineVerify inlineVerify() {
        return null;
    }

    @Override
    public InlineDetach inlineDetach() {
        return null;
    }

    @Override
    public Encrypt encrypt() {
        return null;
    }

    @Override
    public Decrypt decrypt() {
        return null;
    }

    @Override
    public Armor armor() {
        return null;
    }

    @Override
    public Dearmor dearmor() {
        return null;
    }
}
