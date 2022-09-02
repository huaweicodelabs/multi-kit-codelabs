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

package com.huawei.discovertourismapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.huawei.discovertourismapp.R;


public class TourismSharedPref {

    private final SharedPreferences mSharedPreferences;
    private static TourismSharedPref mInstance;
    private static String PREF_NAME;


    private TourismSharedPref(Context context) {
        PREF_NAME = context.getString(R.string.app_name);
        mSharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void initializeInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TourismSharedPref(context);
        }
    }

    public static synchronized TourismSharedPref getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(TourismSharedPref.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return mInstance;
    }

    private SharedPreferences.Editor getPreferenceEditor() {
        return mSharedPreferences.edit();
    }

    public boolean putString(String key, String value) {
        return getPreferenceEditor().putString(key, value).commit();
    }

    public boolean putBoolean(String key, boolean value) {
        return getPreferenceEditor().putBoolean(key, value).commit();
    }

    public boolean putLong(String key, Long value) {
        return getPreferenceEditor().putLong(key, value).commit();
    }

    public boolean putDouble(String key, float value) {
        return getPreferenceEditor().putFloat(key, value).commit();
    }


    public String getString(String key, String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mSharedPreferences.getBoolean(key, defValue);
    }

    public Float getFloat(String key, Float defValue) {
        return mSharedPreferences.getFloat(key, defValue);
    }

    public Long getLong(String key, Long defValue) {
        return mSharedPreferences.getLong(key, defValue);
    }

}
