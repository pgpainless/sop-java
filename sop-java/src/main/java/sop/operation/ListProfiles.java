// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import java.util.List;

public interface ListProfiles {

    List<String> ofCommand(String command);

    List<String> global();

}
