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

//@file:Suppress("DEPRECATION")
@file:Suppress("TooManyFunctions","ReturnCount")
package com.huawei.hms.urbanhomeservices.kotlin.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.facebook.login.LoginManager
import com.google.gson.Gson
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectAuthCredential
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.kotlin.activities.SplashActivity
import com.huawei.hms.urbanhomeservices.kotlin.model.LoginModel
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * This class contains common function used in the application.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

object Utils {
    private val TAG = Utils::class.qualifiedName
    private val sharedPrefFile = "urbanhomeservice"
    private val preferenceKey = "userData"
    private val preferenceCountryKey = "country"
    private val sharedPrefCountryFile = "country"
    private val preferenceUserAddressKey = "userAddress"
    private val sharedPrefsUserAddressFile = "userAddress"
    var curentLatitude: Double = 0.0
    var currentLongitude: Double = 0.0
    var imageType: String? = null
    var isProfileFragment = false

    /**
     * Check for network connection and return boolean value accordingly
     *
     * @return Boolean check connected with network or not
     */
    fun isConnected(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }

    /**
     * It is recommended to save the apiKey to the server to avoid being obtained by hackers.
     * Please get the api_key from the app you created in appgallery
     * Need to encode api_key before use
     *
     * @return String returns API key
     */
    fun getApiKey(): String? {
        val apiKey = AppConstants.API_KEY
        return try {
            URLEncoder.encode(apiKey, AppConstants.ENCODE_FORMAT)
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, "encode apikey error")
            null
        }
    }

    /**
     * Displays short time toast message
     *
     * @param context application context
     * @param result to show to user as a toast
     *
     */
    fun showToast(context: Context?, result: String) {
        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
    }

    /**
     * Helps in hiding the keyboard when focus in the fragment view
     */
    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    /**
     * Helps in hiding the keyboard when focus in the activity view
     */
    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
                getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * This method helps to get user details after facebook login and stores data in
     * sharedPreferences and loginmodel
     *
     * @param context application context
     * @param user JSON object of user
     */
    fun facebookLogin(context: Context, user: JSONObject) {
        user.let {
            val firstName = user.getString(AppConstants.FACEBOOK_FIELD_FIRST_NAME)
            val lastName = user.getString(AppConstants.FACEBOOK_FIELD_LAST_NAME)
            val email = user.getString(AppConstants.FACEBOOK_FIELD_EMAIL)
            val id = user.getString(AppConstants.FACEBOOK_FIELD_ID)
            val imageUrl = getFbImageUrl(id)
            val loginModel =
                    LoginModel("$firstName $lastName", email, "", imageUrl)
            storeData(
                    context,
                    loginModel
            )
        }
    }

    /**
     * Stores user data in SharedPreferences
     *
     * @param context application context
     * @param loginModel login bean class
     *
     */
    private fun storeData(context: Context, loginModel: LoginModel) {
        val sharedPreferences: SharedPreferences =
                context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        val userData: String = Gson().toJson(loginModel)
        editor.putString(preferenceKey, userData)
        editor.apply()
        editor.commit()
    }

    /**
     * Get user data from SharedPreferences
     *
     * @param context application context
     */
    fun getSharePrefData(context: Context): LoginModel {
        val sharedPreferences: SharedPreferences =
                context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        return Gson().fromJson(
                sharedPreferences.getString(preferenceKey, ""),
                LoginModel::class.java
        )
    }

    /**
     * Stores country name in SharedPreferences
     *
     * @param context application context
     * @param countryName name of country
     */
    fun storeCountryName(context: Context, countryName: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                sharedPrefCountryFile, Context.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(preferenceCountryKey, countryName)
        editor.apply()
        editor.commit()
    }

    /**
     * Get country name from SharedPreferences
     *
     * @param context application context
     */
    fun getSharePrefCountry(context: Context): String? {
        val sharedPreferences: SharedPreferences =
                context.getSharedPreferences(sharedPrefCountryFile, Context.MODE_PRIVATE)
        return sharedPreferences.getString(preferenceCountryKey, "")
    }

    /**
     * Stores user address in SharedPreferences
     *
     * @param context application context
     * @param userAddress address of user
     */
    fun storeUserAddress(context: Context, userAddress: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                sharedPrefsUserAddressFile, Context.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(preferenceUserAddressKey, userAddress)
        editor.apply()
        editor.commit()
    }

    /**
     * Get user address from SharedPreference
     *
     * @param context application context
     * @return String returns users address
     */
    fun getUserAddress(context: Context): String? {
        val sharedPreferences: SharedPreferences =
                context.getSharedPreferences(sharedPrefsUserAddressFile, Context.MODE_PRIVATE)
        return sharedPreferences.getString(preferenceUserAddressKey, "")
    }

    /**
     * This method helps in fetching user details from hms account kit and
     * stores details in login model and SharedPreferences.
     *
     * @param context application context
     * @param user object of Huawei auth id
     */
    fun huaweiLogin(context: Context, user: AuthHuaweiId) {
        val displayName: String = user.displayName?.let { user.displayName } ?: run { "" }
        val avatarUri: String = user.avatarUriString?.let { user.avatarUriString } ?: run { "" }
        val loginModel = LoginModel(displayName, user.email, "", avatarUri)
        storeData(
                context,
                loginModel
        )
    }

    /**
     * Sets current lat and lng to instance variable
     *
     * @param currentLat current latitude
     * @param currentLng current longitude
     */
    fun latLng(currentLat: Double, currentLng: Double) {
        curentLatitude = currentLat
        currentLongitude = currentLng
    }

    /**
     * Login out alert dialog box
     *
     * @param context context of activity
     */
    fun logoutDialog(context: Activity) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.dialogTitle)
        builder.setMessage(R.string.dialogMessage)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(context.getString(R.string.text_yes)) { _, _ ->
            logout(context)
        }
        builder.setNeutralButton(context.getString(R.string.text_no)) { _, _ ->
            showLongToast(context, context.getString(R.string.msg_logout_cancelled))
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    /**
     * Method used for logout.
     *
     * @param context activity context
     */
    private fun logout(context: Activity) {
        val providerId = getLoginProviderId()
        if (providerId == AGConnectAuthCredential.Facebook_Provider.toString()) {
            AppPreferences.isLogin = false
            LoginManager.getInstance().logOut() // logout from facebook account
            showToast(context, context.getString(R.string.msg_logout_success))
            val intent = Intent(context, SplashActivity::class.java)
            context.startActivity(intent)
            context.finish()
        } else if (null != AGConnectAuth.getInstance().currentUser) {
            AppPreferences.isLogin = false
            AGConnectAuth.getInstance().signOut()
            showToast(context, context.getString(R.string.msg_logout_success))
            val intent = Intent(context, SplashActivity::class.java)
            context.startActivity(intent)
            context.finish()
        }

    }

    /**
     * Function to fetch login provider
     *
     * @return String provide Id
     */
    fun getLoginProviderId(): String? {
        var providerId = "-1"
        if (AGConnectAuth.getInstance().currentUser != null) {
            val user = AGConnectAuth.getInstance().currentUser
            providerId = user.providerId
        }
        return providerId
    }

    /**
     * Displays long toast.
     *
     * @param context application context
     * @param result to show as a toast message
     */
    fun showLongToast(context: Context?, result: String) {
        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
    }

    /**
     * To get Facebook image
     *
     * @param id FB id
     * @return String FB profile URL
     *
     */
    private fun getFbImageUrl(id: String): String {
        return "https://graph.facebook.com/$id/picture?type=normal"
    }
}
