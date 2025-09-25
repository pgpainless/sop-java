// SPDX-FileCopyrightText: 2025 Paul Schaub <vanitasvitae@fsfe.org>
//
// SPDX-License-Identifier: Apache-2.0

package sop

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.text.ParseException

class GsonParser(private val gson: Gson = Gson()) : Verification.JSONParser {

    override fun parse(string: String): Verification.JSON {
        try {
            return gson.fromJson(string, object : TypeToken<Verification.JSON>() {}.type)
        } catch (e: JsonSyntaxException) {
            throw ParseException(e.message, 0)
        }
    }
}
