// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.external.operation;

import org.junit.jupiter.api.condition.EnabledIf;
import sop.testsuite.operation.DecryptWithSessionKeyTest;

@EnabledIf("sop.testsuite.operation.AbstractSOPTest#hasBackends")
public class ExternalDecryptWithSessionKeyTest extends DecryptWithSessionKeyTest {

}
