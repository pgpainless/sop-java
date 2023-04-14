// SPDX-FileCopyrightText: 2023 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop.operation;

import sop.Profile;

import java.util.List;

public interface ListProfiles {

    List<Profile> subcommand(String command);

}
