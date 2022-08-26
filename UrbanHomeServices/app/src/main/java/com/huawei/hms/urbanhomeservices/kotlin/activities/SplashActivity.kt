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
@file:Suppress("MaxLineLength","NewLineAtEndOfFile")
package com.huawei.hms.urbanhomeservices.kotlin.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.ActivitySplashBinding
import com.huawei.hms.urbanhomeservices.databinding.ManageServiceActivityBinding
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.LoginInfo
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppPreferences
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils


/**
 * This activity basically helps to select user type :
 * 1 : Consumer type
 * 2 : Service provider type.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */
class SplashActivity : AppCompatActivity() {
    private val tag = "SplashActivity"
    private var loginInfo: LoginInfo? = null

    private lateinit var splashBinding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashBinding = ActivitySplashBinding.inflate(layoutInflater)
        val view = splashBinding.root
        setContentView(view)
        locationPermission()
        checkUserLoginType()
        loginInfo = LoginInfo()
        val items: Array<String> = resources.getStringArray(R.array.select_user_type)
        val adapter: ArrayAdapter<String> = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, items
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        splashBinding.spnLogin.adapter = adapter
        splashBinding.btnnext.setOnClickListener {
            splashBinding.spnLogin.selectedItemId.toString()
            if (splashBinding.spnLogin.selectedItemId.toString() == AppConstants.INTIAL_VALUE.toString()) {
                Utils.showToast(this@SplashActivity, getString(R.string.select_type))
                return@setOnClickListener
            }
            checkLogin(splashBinding.spnLogin)
        }
    }

    /**
     * Check if the user is already signed in based on user type and navigate to particular activity
     */
    private fun checkUserLoginType() {
        if (AppPreferences.isLogin && AppPreferences.userType.equals(resources.getString(R.string.select_consumer_type))) {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        } else if (AppPreferences.isLogin && AppPreferences.userType.equals(resources.getString(R.string.select_provider_type))) {
            val intent = Intent(
                    this@SplashActivity,
                    AddServiceActivity::class.java
            )
            intent.putExtra(
                    AppConstants.LOGIN_USER_TYPE,
                    resources.getString(R.string.select_provider_type)
            )
            startActivity(intent)
            finish()
        }
    }
    @Suppress("ComplexMethod")
    /**
     * Select the user type from spinner
     *
     * @param spnLogin user type drop down.
     */
    private fun checkLogin(spnLogin: Spinner) {
        if (spnLogin.selectedItemId.toString() == AppConstants.INITIAL_VALUE_ONE.toString()) {
            if (AppPreferences.isLogin && AppPreferences.userType.equals(resources.getString(R.string.select_consumer_type))) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                AppPreferences.apply {
                    isLogin = true
                    userType = resources.getString(R.string.select_consumer_type)
                }
                intent.apply {
                    putExtra(AppConstants.LOGIN_USER_TYPE, spnLogin.selectedItem.toString())
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                intent.putExtra(AppConstants.LOGIN_USER_TYPE, spnLogin.selectedItem.toString())
                startActivity(intent)
            }
        } else if (spnLogin.selectedItemId.toString() == AppConstants.INITIAL_VALUE_TWO.toString()) {
            if (AppPreferences.isLogin && AppPreferences.userType.equals(resources.getString(R.string.select_provider_type))) {
                val intent = Intent(this@SplashActivity, AddServiceActivity::class.java)
                AppPreferences.apply {
                    isLogin = true
                    userType = resources.getString(R.string.select_provider_type)

                }
                intent.apply {
                    putExtra(AppConstants.LOGIN_USER_TYPE, spnLogin.selectedItem.toString())
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                intent.putExtra(AppConstants.LOGIN_USER_TYPE, spnLogin.selectedItem.toString())
                startActivity(intent)
            }
        }
    }

    /**
     * Asking for location permission
     * which is used to fetch current location on home Fragment
     */
    private fun locationPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )
                requestPermissions(strings, AppConstants.INITIAL_VALUE_ONE)
            }
        } else {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(AppConstants.LOGIN_LOCATION_PERMISSION) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        AppConstants.LOGIN_LOCATION_PERMISSION
                )
                requestPermissions(strings, AppConstants.INITIAL_VALUE_TWO)
            }
        }
    }
    @Suppress("ComplexCondition")
    /**
     * Verifies location permission is granted or not
     */
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstants.INITIAL_VALUE_ONE) {
            if (grantResults.size > AppConstants.INITIAL_VALUE_ONE &&
                    grantResults[AppConstants.INTIAL_VALUE] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[AppConstants.INITIAL_VALUE_ONE] == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(tag, "onRequestPermissionsResult: apply LOCATION PERMISSION successful")
            } else {
                Log.i(tag, "onRequestPermissionsResult: apply LOCATION PERMISSION  failed")
            }
        }
        if (requestCode == AppConstants.INITIAL_VALUE_TWO) {
            if (grantResults.size > AppConstants.INITIAL_VALUE_TWO
                    && grantResults[AppConstants.INITIAL_VALUE_TWO] == PackageManager.PERMISSION_GRANTED
                    && grantResults[AppConstants.INTIAL_VALUE] == PackageManager.PERMISSION_GRANTED
                    && grantResults[AppConstants.INITIAL_VALUE_ONE] == PackageManager.PERMISSION_GRANTED) {
                Log.i(
                        tag,
                        "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION successful"
                )
            } else {
                Log.i(tag, "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION  failed")
            }
        }
    }
}
