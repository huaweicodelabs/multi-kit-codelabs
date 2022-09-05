/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
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
package com.huawei.schooldairy.userutils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Shared Preference methods which is used in this whole application
 * @author: Huawei
 * @since: 25-05-2021
 */
public class PrefUtil {

    public static final String MyPREFERENCES = "SchoolDiaryPref";
    private static Context context;
    private static PrefUtil prefUtil;

    public static PrefUtil getInstance(Context context) {
        if (prefUtil == null)
            prefUtil = new PrefUtil(context);
        return prefUtil;
    }

    PrefUtil(Context context) {
        this.context = context;
    }

    public boolean sharedPreferenceExist(String key) {
        SharedPreferences prefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (!prefs.contains(key)) {
            return true;
        } else {
            return false;
        }
    }

    public void setInt(String key, int value) {
        SharedPreferences prefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key) {
        SharedPreferences prefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        return prefs.getInt(key, -1);
    }

    public void setStr(String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getStr(String key) {
        SharedPreferences prefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public void setBool(String key, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBool(String key) {
        SharedPreferences prefs = context.getSharedPreferences(MyPREFERENCES, 0);
        return prefs.getBoolean(key, false);
    }

}
