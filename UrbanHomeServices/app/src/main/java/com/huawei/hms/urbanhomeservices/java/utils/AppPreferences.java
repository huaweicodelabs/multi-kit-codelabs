/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.urbanhomeservices.java.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Define Constants and App Prefs
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class AppPreferences {

    public static final String NAME = "UHProviderLoginPrefs";
    public static final int MODE = Context.MODE_PRIVATE;
    public static SharedPreferences preferences;
    public static final String IS_LOGIN = "is_login";
    public static final String USERNAME = "username";
    public static final String USER_TYPE = "userType";
    public static final String IS_FIRST_TIMELOGIN = "is_first_time_login";

    /**
     * Init SharePrefs
     *
     * @param context context of app
     */
    public static void init(Context context) {
        preferences = context.getSharedPreferences(NAME, MODE);
        sharedPrefsEdit(preferences);
    }

    /**
     * Store data into SharePrefs
     *
     * @param prefs preference of app
     */
    private static void sharedPrefsEdit(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.commit();
    }

    /**
     * Fetch Login Details from SharedPrefs
     *
     * @return boolean preference boolean
     */
    public static boolean isLogin() {
        return preferences.getBoolean(IS_LOGIN, false);
    }

    /**
     * Set Login Details to SharedPrefs
     *
     * @param loginValue boolean login value
     */
    public static void setIsLogin(boolean loginValue) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(IS_LOGIN, loginValue);
        editor.commit();
    }

    /**
     * Get userName from SharedPrefs
     *
     * @return getUserName user name stored in preference
     */
    public static String getUserName() {
        return preferences.getString(USERNAME, "");
    }

    /**
     * Set user name to SharedPrefs
     *
     * @param userName user name
     */
    public static void setUsername(String userName) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USERNAME, userName);
        editor.commit();
    }

    /**
     * Get user type
     *
     * @return String returns user type.
     */
    public static String getUserType() {
        return preferences.getString(USER_TYPE, "");
    }

    /**
     * Set user type
     *
     * @param userType use type
     */
    public static void setUserType(String userType) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_TYPE, userType);
        editor.commit();
    }

    /**
     * First time login check
     *
     * @return boolean of first time login
     */
    public static boolean isFirstTimeLogin() {
        return preferences.getBoolean(IS_FIRST_TIMELOGIN, true);
    }

    /**
     * First time login check
     *
     * @param firstTimeLogin first time login
     */
    public static void setFirstTimeLogin(boolean firstTimeLogin) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(IS_FIRST_TIMELOGIN, firstTimeLogin);
        editor.commit();
    }

}
