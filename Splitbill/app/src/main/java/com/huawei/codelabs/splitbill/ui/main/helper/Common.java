/* Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.huawei.codelabs.splitbill.ui.main.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.huawei.codelabs.splitbill.ui.main.activities.AuthActivity;
import com.huawei.codelabs.splitbill.ui.main.activities.MainActivity;

import static android.content.Context.MODE_PRIVATE;

public class Common {
    public static Common common;
    public static String MY_PREFS_NAME = "SplitBillApp";

    public static void setFirstTimeUserLoggedIn(Context context, boolean firstimeuserloginflag) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean("firstimeuserlogin", firstimeuserloginflag);
        editor.apply();
    }

    public static boolean getFirstTimeUserLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        boolean firstimeuserlogin = prefs.getBoolean("firstimeuserlogin", false);//"No name defined" is the default value.
        return firstimeuserlogin;

    }

    public static void showToast(String msg, Activity activity) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    public static Common getCommonInstace() {
        return new Common();
    }

    public void hideBottom(Activity activity, int state) {
        ((MainActivity) activity).mainActivityBinding.navView.setVisibility(state);
    }

    public void goToAuthInActivity(Activity activity) {
        Intent intent = new Intent(activity, AuthActivity.class);

        activity.startActivity(intent);
    }

}
