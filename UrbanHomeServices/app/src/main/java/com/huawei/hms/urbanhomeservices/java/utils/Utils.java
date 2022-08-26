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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.gson.Gson;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.activities.SplashActivity;
import com.huawei.hms.urbanhomeservices.java.model.LoginModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This class contains common function used in the application.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class Utils {
    
    public static final String TAG = Utils.class.getName();
    public static final String SHARED_PREF_FILE = "urbanhomeservice";
    public static final String PREF_KEY_USER_DATA = "userData";
    public static final String PREF_COUNTRY_KEY = "country";
    public static final String PREF_COUNTRY_FILE = "country";
    public static final String PREF_USER_ADDRESS_KEY = "userAddress";
    public static final String PREF_USER_ADDR_FILE = "userAddress";
    public static double CURRENT_LAT = 0.0;
    public static double CURRENT_LON = 0.0;
    public static String IMAGE_TYPE = null;
    public static boolean IS_PROFILE_FRAGMENT = false;
    public static LoginModel LOGIN_MODEL;

    /**
     * Check for network connection and return boolean value accordingly
     *
     * @param context app context
     * @return boolean network is connected or not
     */
    public static boolean isConnected(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true;
                } else {
                    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
                }
            }
        } else {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    /**
     * This method helps to get user details after facebook login and stores data in
     * sharedPreferences and loginmodel
     *
     * @return String api key of app
     */
    public static String getApiKey() {
        String apiKey = AppConstants.API_KEY;
        try {
            return URLEncoder.encode(apiKey, AppConstants.ENCODE_FORMAT);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "encode apikey error");
            return "";
        }
    }

    /**
     * Displays short time toast message
     *
     * @param context application context
     * @param result result to show as a toast message
     *
     */
    public static void showToast(Context context, String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays short time toast message
     *
     * @param context application context
     * @param result result to show as a toast
     */
    public static void showLongToast(Context context, String result) {
        Toast.makeText(context, result, Toast.LENGTH_LONG).show();
    }

    /**
     * Helps in hiding the keyboard when focus in the activity view
     *
     * @param activity activity context
     */
    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View currentFocusedView = activity.getCurrentFocus();
            if (currentFocusedView != null) {
                inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stores country name in SharedPreferences
     *
     * @param context application context
     * @param countryName country name
     *                    
     */
    public static void storeCountryName(Context context, String countryName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_COUNTRY_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_COUNTRY_KEY, countryName);
        editor.apply();
        editor.commit();
    }

    /**
     * Get country name from SharedPreferences
     *
     * @param context application context
     * @return String country name
     */
    public static String getSharePrefCountry(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREF_COUNTRY_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PREF_COUNTRY_KEY, "");
    }

    /**
     * Stores user address in SharedPreferences
     *
     * @param context application context
     * @param userAddress address of user
     */
    public static void storeUserAddress(Context context, String userAddress) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PREF_USER_ADDR_FILE, Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_USER_ADDRESS_KEY, userAddress);
        editor.apply();
        editor.commit();
    }

    /**
     * Get user address from SharedPreferences
     *
     * @param context application context
     * @return String address of user
     */
    public static String getUserAddress(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREF_USER_ADDR_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PREF_USER_ADDRESS_KEY, "");
    }

    /**
     * Sets current lat and lng to instance variable
     *
     * @param currentLat current latitude
     * @param currentLng current longitude
     */
    public static void latLng(Double currentLat, Double currentLng) {
        CURRENT_LAT = currentLat;
        CURRENT_LON = currentLng;
    }

    /**
     * Method used for logout.
     *
     * @param context activity context
     */
    private static void logout(Activity context) {
        String providerId = getLoginProviderId();
        if (providerId.equals(String.valueOf(AGConnectAuthCredential.Facebook_Provider))) {
            AppPreferences.setIsLogin(false);
            LoginManager.getInstance().logOut();
            // logout from facebook account
            showToast(context, context.getString(R.string.msg_logout_success));
            Intent intent = new Intent(context, SplashActivity.class);
            context.startActivity(intent);
            context.finish();
        } else if (null != AGConnectAuth.getInstance().getCurrentUser()) {
            AppPreferences.setIsLogin(false);
            AGConnectAuth.getInstance().signOut();
            showToast(context, context.getString(R.string.msg_logout_success));
            Intent intent = new Intent(context, SplashActivity.class);
            context.startActivity(intent);
            context.finish();
        }
    }

    /**
     * Function to fetch login provider
     *
     * @return string returns provider id
     */
    public static String getLoginProviderId() {
        String providerId = "-1";
        if (AGConnectAuth.getInstance().getCurrentUser() != null) {
            AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
            providerId = user.getProviderId();
        }
        return providerId;
    }

    /**
     * Login out alert dialog box
     *
     * @param context activity context
     *                
     */
    public static void logoutDialog(Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialogTitle);
        builder.setMessage(R.string.dialogMessage);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                logout(context);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * This method helps in fetching user details from hms account kit and
     * stores details in login model and SharedPreferences.
     *
     * @param context application context
     * @param user authenticated huawei user
     *
     */
    public static void huaweiLogin(Context context, AuthHuaweiId user) {
        String displayName = user.getDisplayName();
        String avatarUri = user.getAvatarUriString();
        LOGIN_MODEL = new LoginModel(displayName, user.getEmail(), "", avatarUri);
        storeData(
                context,
                LOGIN_MODEL
        );
    }

    /**
     * Stores user data in SharedPreferences
     *
     * @param context application context
     * @param loginModel bean class
     *                   
     */
    public static void storeData(Context context, LoginModel loginModel) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String userData = new Gson().toJson(loginModel);
        editor.putString(PREF_KEY_USER_DATA, userData);
        editor.apply();
        editor.commit();
    }

    /**
     * Get user data from SharedPreferences
     *
     * @param context application context
     * @return LoginModel returns login model preference data
     *
     */
    public static LoginModel getSharePrefData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
        return new Gson().fromJson(
                sharedPreferences.getString(PREF_KEY_USER_DATA, ""),
                LoginModel.class
        );
    }

    /**
     * This method helps to get user details after facebook login and stores data in
     * sharedPreferences and login model
     *
     * @param context application context
     * @param user users json object
     * @throws JSONException throws jason exception
     */
    public static void facebookLogin(Context context, JSONObject user) throws JSONException {
        String name = user.getString(AppConstants.FACEBOOK_FIELD_FIRST_NAME);
        String email = user.getString(AppConstants.FACEBOOK_FIELD_EMAIL);
        String id = user.getString(AppConstants.FACEBOOK_FIELD_ID);
        String imageUrl = getFbImageUrl(id);
        LOGIN_MODEL = new LoginModel(name, email, "", imageUrl);
        storeData(context, LOGIN_MODEL);
    }

    /**
     * To get Facebook image
     *
     * @param id  id of facebook
     * @return String fb image url
     */
    private static String getFbImageUrl(String id) {
        return "https://graph.facebook.com/$id/picture?type=normal";
    }

    /**
     * to get the current time stamp.
     *
     * @return int provide the current timestamp
     */
    public static int getTimeStamp() {
        long tsLong = System.currentTimeMillis() / 1000;
        return Integer.parseInt(Long.toString(tsLong));
    }

}
