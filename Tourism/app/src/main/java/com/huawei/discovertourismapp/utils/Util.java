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

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.huawei.discovertourismapp.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Util {
    private  ProgressDialog progress;
        /**
         * Show the progress bar
         *
         * @param context of the activity or fragment
         */
        public void showProgressBar(Context context) {
            progress = new ProgressDialog(context);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setMessage("Please Wait...");
            progress.setIndeterminate(true);
            progress.show();
        }

        /**
         * Dismiss progress bar
         */
        public void stopProgressBar() {
            progress.hide();
        }

        /**
         * Check internet connection is available or not
         *
         * @param applicationContext which call this method
         * @return true or false
         */
        public static boolean isOnline(Context applicationContext) {
            ConnectivityManager conMgr = (ConnectivityManager) applicationContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

            if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
                Toast.makeText(applicationContext, applicationContext.getString(R.string.no_internet),
                        Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

        public static String getTimeStamp() {
            long tsLong = System.currentTimeMillis() / 1000;
            return Long.toString(tsLong);
        }

        public static int getRandomNumber() {
            long tsLong = System.currentTimeMillis()%1000;
            return (int) tsLong;
        }
    /**
     * It is recommended to save the apiKey to the server to avoid being obtained by hackers.
     * Please get the api_key from the app you created in appgallery
     * Need to encode api_key before use
     */
    public static String getApiKey(Context context) {
        // get apiKey from AppGallery Connect
        String apiKey = "DAEDAJ7TzufUv60/m880ZUbmPsstrcDsejHe+GZIF01e1omuZdCA3M+CXOj0XfpJKRNVy1TuFxf50sp8EmaOxe4UGUbRs+9w2VLTHQ==";

        // need encodeURI the apiKey
        try {
            String encodedApiKey = URLEncoder.encode(context.getResources().getString(R.string.api_key), "utf-8");
            return encodedApiKey;
        } catch (UnsupportedEncodingException e) {
          //  Log.e(TAG, "encode apikey error");
            return null;
        }
    }
}
