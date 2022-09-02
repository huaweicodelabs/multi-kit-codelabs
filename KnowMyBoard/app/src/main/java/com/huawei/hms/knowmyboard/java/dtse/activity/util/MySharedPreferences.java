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

package com.huawei.hms.knowmyboard.dtse.activity.util;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreferences {
    public static final String TAG = "MySharedPreferences";
    private SharedPreferences myPreferences;
    private SharedPreferences.Editor myEditor;
    private static MySharedPreferences mySharedPreferences;


    public MySharedPreferences(Context context) {
        myPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        myEditor = myPreferences.edit();
    }

    public static MySharedPreferences getInstance(Context context) {
        if (mySharedPreferences == null) {
            synchronized (MySharedPreferences.class) {
                if(mySharedPreferences == null) {
                    mySharedPreferences = new MySharedPreferences(context);
                }
            }
        }
        return mySharedPreferences;
    }

    public void putStringValue(String key, String value) {
        myEditor.putString(key, value);
        myEditor.commit();
    }

    public String getStringValue(String key) {
        return myPreferences.getString(key, Constants.USER_NAME);
    }

}
