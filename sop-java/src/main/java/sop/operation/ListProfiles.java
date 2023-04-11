// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import java.util.List;

public abstract class ListProfiles {

    public ListProfiles() {

    }

    public abstract List<String> ofCommand(String command);

    public abstract List<String> global();

}
