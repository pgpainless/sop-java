// SPDX-FileCopyrightText: 2025 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.testsuite.external.operation;

import org.junit.jupiter.api.condition.EnabledIf;
import sop.testsuite.operation.ChangeKeyPasswordTest;

@EnabledIf("sop.testsuite.operation.AbstractSOPTest#hasBackends")
public class ExternalChangeKeyPasswordTest extends ChangeKeyPasswordTest {

}
