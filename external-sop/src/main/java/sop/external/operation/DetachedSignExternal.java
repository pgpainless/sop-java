package sop.external.operation;

import sop.ReadyWithResult;
import sop.SigningResult;
import sop.enums.SignAs;
import sop.exception.SOPGPException;
import sop.operation.DetachedSign;

import java.io.IOException;
import java.io.InputStream;

public class DetachedSignExternal implements DetachedSign {

    private boolean noArmor;
    private byte[] keyPassword;

    @Override
    public DetachedSign noArmor() {
        this.noArmor = true;
        return this;
    }

    @Override
    public DetachedSign key(InputStream key) throws SOPGPException.KeyCannotSign, SOPGPException.BadData, SOPGPException.UnsupportedAsymmetricAlgo, IOException {
        return null;
    }

    @Override
    public DetachedSign withKeyPassword(byte[] password) throws SOPGPException.UnsupportedOption, SOPGPException.PasswordNotHumanReadable {
        this.keyPassword = password;
        return this;
    }

    @Override
    public DetachedSign mode(SignAs mode) throws SOPGPException.UnsupportedOption {
        return null;
    }

    @Override
    public ReadyWithResult<SigningResult> data(InputStream data) throws IOException, SOPGPException.KeyIsProtected, SOPGPException.ExpectedText {
        return null;
    }
}
