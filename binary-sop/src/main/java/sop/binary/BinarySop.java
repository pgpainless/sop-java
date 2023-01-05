// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.binary;

import sop.SOP;
import sop.binary.operation.BinaryExtractCert;
import sop.binary.operation.BinaryGenerateKey;
import sop.binary.operation.BinaryVersion;
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

public class BinarySop implements SOP {

    private final String binaryName;

    public BinarySop(String binaryName) {
        this.binaryName = binaryName;
    }

    @Override
    public Version version() {
        return new BinaryVersion(binaryName);
    }

    @Override
    public GenerateKey generateKey() {
        return new BinaryGenerateKey(binaryName);
    }

    @Override
    public ExtractCert extractCert() {
        return new BinaryExtractCert(binaryName);
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
