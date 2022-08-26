/*
 *
 *  * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.huawei.hms.urbanhomeservices.kotlin.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

/**
 * Define Constants and App Prefs
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

object AppPreferences {
    private const val NAME = "UHProviderLoginPrefs"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences
    private val IS_LOGIN = Pair("is_login", false)
    private val USERNAME = Pair("username", "")
    private val USER_TYPE: Pair<String, String> = Pair("userType", "")
    private val IS_FIRST_TIMELOGIN = Pair("is_first_time_login", true)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(
                NAME,
                MODE
        )
    }

    /**
     * Store data into SharePrefs
     */
    @SuppressLint("ApplySharedPref")
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply {
            commit()
        }
    }

    /**
     * Fetch Login Details from SharedPrefs
     */
    var isLogin: Boolean
        get() = preferences.getBoolean(
                IS_LOGIN.first, IS_LOGIN.second
        )
        set(value) = preferences.edit {
            it.putBoolean(IS_LOGIN.first, value)
        }

    /**
     * Fetch User name from SharedPrefs
     */
    var username: String
        get() = preferences.getString(
                USERNAME.first, USERNAME.second
        ) ?: ""
        set(value) = preferences.edit {
            it.putString(USERNAME.first, value)
        }

    /**
     * Fetch User type from SharedPrefs
     */
    var userType: String?
        get() = preferences.getString(
                USER_TYPE.first, USER_TYPE.second
        )
        set(value) = preferences.edit {
            it.putString(USER_TYPE.first, value)
        }

    /**
     * Check Login status from SharedPrefs
     */
    var isFirstTimeLogin: Boolean
        get() = preferences.getBoolean(
                IS_FIRST_TIMELOGIN.first, IS_FIRST_TIMELOGIN.second
        )
        set(value) = preferences.edit {
            it.putBoolean(
                    IS_FIRST_TIMELOGIN.first, value
            )
        }
}
