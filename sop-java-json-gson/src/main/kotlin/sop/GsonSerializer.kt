// SPDX-FileCopyrightText: 2025 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import com.google.gson.Gson

class GsonSerializer(
    private val gson: Gson = Gson()
) : Verification.JSONSerializer {

    override fun serialize(json: Verification.JSON): String {
        return gson.toJson(json)
    }
}