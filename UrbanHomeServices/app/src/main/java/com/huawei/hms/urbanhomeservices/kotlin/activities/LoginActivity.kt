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
@file:Suppress("TooManyFunctions","MaxLineLength","NewLineAtEndOfFile","MagicNumber","WildcardImport")
package com.huawei.hms.urbanhomeservices.kotlin.activities

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.FacebookAuthProvider
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hmf.tasks.Task
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.api.entity.common.CommonConstant
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.ActivityLoginBinding
import com.huawei.hms.urbanhomeservices.databinding.ActivityMainKBinding
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.CloudDBZoneWrapper
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.LoginInfo
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppPreferences
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils.huaweiLogin

import org.json.JSONException

/**
 * This activity helps in logging based on two conditions :
 * 1 : Huawei SignIn
 * 2 : AuthService SignIn
 * Also Sends user data to CloudDB
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

class LoginActivity : AppCompatActivity(), View.OnClickListener,
        CloudDBZoneWrapper.UiCallBack<LoginInfo> {
    private val tag = "LoginActivity"
    private val callbackManager = CallbackManager.Factory.create()
    private lateinit var service: HuaweiIdAuthService
    private lateinit var loginInfo: LoginInfo
    private lateinit var agConnectAuth: AGConnectAuth
    private val scopes = listOf(Scope(AppConstants.LOGIN_EMAIL_SCOPE))
    private var uId: String? = null
    private lateinit var mCloudDBZoneWrapper: CloudDBZoneWrapper<LoginInfo>
    private lateinit var signWithFacebookBtn: LoginButton
    private var profileType: String? = null
    private var huaweiAccount: AuthHuaweiId? = null

    private lateinit var loginBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        val view = loginBinding.root
        setContentView(view)
        profileType = intent.getStringExtra(AppConstants.LOGIN_USER_TYPE)
        loginInfo = LoginInfo()
        mCloudDBZoneWrapper = CloudDBZoneWrapper()
        mCloudDBZoneWrapper.setCloudObject(loginInfo)
        mCloudDBZoneWrapper.createObjectType()
        signWithFacebookBtn = findViewById(R.id.signWithFacebookBtn)
        signWithFacebookBtn.setPermissions(
                listOf(
                        AppConstants.LOGIN_EMAIL_SCOPE,
                        AppConstants.LOGIN_FACEBOOK_PROFILE
                )
        )
        initAuthService()
        agConnectAuth = AGConnectAuth.getInstance()
        locationPermission()
        loginBinding.hwidSignin.setOnClickListener(this)
        signWithFacebookBtn.setOnClickListener(this)
    }

    /**
     * Initialize the AuthService
     * Add Scope list for creating AuthService
     */
    private fun initAuthService() {
        val huaweiIdAuthParamsHelper =
                HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
        val scopeList: MutableList<Scope> = ArrayList()
        scopeList.add(Scope(CommonConstant.SCOPE.ACCOUNT_BASEPROFILE))
        scopeList.add(Scope(CommonConstant.SCOPE.SCOPE_ACCOUNT_EMAIL))
        scopeList.add(Scope(CommonConstant.SCOPE.SCOPE_MOBILE_NUMBER))
        scopeList.add(Scope(CommonConstant.SCOPE.SCOPE_ACCOUNT_PROFILE))
        huaweiIdAuthParamsHelper.setScopeList(scopeList)
        val authParams = huaweiIdAuthParamsHelper.setAccessToken().setMobileNumber().createParams()
        service = HuaweiIdAuthManager.getService(this@LoginActivity, authParams)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.hwid_signin -> {
                startAuthService()
            }
            R.id.signWithFacebookBtn -> {
                signWithFacebookBtn.registerCallback(callbackManager, object :
                        FacebookCallback<LoginResult?> {
                    override fun onSuccess(loginResult: LoginResult?) {
                        loginResult?.accessToken?.let {
                            loadFacebookUserProfile(it)
                        }
                        val credential =
                                FacebookAuthProvider.credentialWithToken(loginResult?.accessToken?.token)
                        AGConnectAuth.getInstance().signIn(credential)
                                .addOnSuccessListener { signInResult -> // onSuccess
                                    val user = signInResult.user
                                    uId = user.uid
                                    Utils.showToast(this@LoginActivity, getString(R.string.msg_login_success))
                                }
                                .addOnFailureListener {
                                    Utils.showToast(this@LoginActivity, getString(R.string.msg_login_error))
                                }
                    }

                    override fun onCancel() {
                        Utils.showToast(this@LoginActivity, getString(R.string.msg_login_cancelled))
                    }

                    override fun onError(error: FacebookException) {
                        Utils.showToast(this@LoginActivity, getString(R.string.msg_login_error))
                    }
                })
            }
        }
    }

    /**
     * Start AuthService and request the scope parameters
     */
    private fun startAuthService() {
        HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setUid()
                .setProfile()
                .setMobileNumber()
                .setEmail()
                .setIdToken()
                .setAccessToken()
                .setAuthorizationCode()
                .setScopeList(scopes)
                .createParams()
        startActivityForResult(service.signInIntent, AppConstants.LOGIN_AUTH_CODE)
    }
    @SuppressWarnings("checkstyle:magicnumber")
    /**
     * Add data into cloud DB login table
     */
    private fun processAddAction(data: AuthHuaweiId) {
        val userIdRandomRange = (0..100000).random()
        loginInfo.userId = userIdRandomRange
        loginInfo.deviceToken = ""
        if (data.avatarUri != null) {
            loginInfo.photoUri = data.avatarUri.toString()
        } else {
            loginInfo.photoUri = ""
            loginInfo.userEmail = data.email
        }
    }

    /**
     * Check authentication is successful or not.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK)
            when (requestCode) {
                AppConstants.LOGIN_AUTH_CODE -> {
                    AGConnectAuth.getInstance().signOut()
                    val authHuaweiIdTask: Task<AuthHuaweiId> =
                            HuaweiIdAuthManager.parseAuthResultFromIntent(data)
                    if (authHuaweiIdTask.isSuccessful) {
                        huaweiAccount = authHuaweiIdTask.result
                        val credential =
                                HwIdAuthProvider.credentialWithToken(huaweiAccount?.accessToken)
                        agConnectAuth.signIn(credential)
                                ?.addOnSuccessListener {
                                    it.user.displayName
                                    it.user.email
                                    it.user.uid
                                    it.user.providerInfo
                                    mCloudDBZoneWrapper
                                            .setmUiCallBack(this)
                                    mCloudDBZoneWrapper
                                            .openCloudDBZoneV2()
                                }
                                ?.addOnFailureListener {
                                    Log.e(tag, "SignIn failed")
                                }
                    } else {
                        Log.e(tag, "SignIn failed")
                    }
                }
                AppConstants.LOGIN_FACEBOOK_RESULTCODE -> {
                    callbackManager?.onActivityResult(requestCode, resultCode, data)
                }
            }
    }

    /**
     * Start dashboard activity
     */
    private fun startDashboardActivity() {
        if (profileType.equals(getString(R.string.consumer))) {
            AppPreferences.isLogin = true
            AppPreferences.userType = AppConstants.LOGIN_CONSUMER_TYPE
            val intentMainActivity = Intent(this, MainActivity::class.java)
            intentMainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intentMainActivity)
            finishAffinity()
        } else if (profileType.equals(getString(R.string.service_provider))) {
            AppPreferences.isLogin = true
            AppPreferences.userType = AppConstants.SERVICE_PROVIDER_TYPE
            val intent = Intent(this, AddServiceActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finishAffinity()
        }
    }

    /**
     * Asking for location permission
     * which is used to fetch current location on home Fragment
     */
    private fun locationPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(tag, "sdk < 28 Q")
            if (checkSelfPermission(
                            ACCESS_FINE_LOCATION
                    ) != PERMISSION_GRANTED
                    && checkSelfPermission(
                            ACCESS_COARSE_LOCATION
                    ) != PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION
                )
                requestPermissions(strings, AppConstants.INITIAL_VALUE_ONE)
            }
        } else {
            if (checkSelfPermission(
                            ACCESS_FINE_LOCATION
                    ) != PERMISSION_GRANTED && checkSelfPermission(
                            ACCESS_COARSE_LOCATION
                    ) != PERMISSION_GRANTED && checkSelfPermission(
                            AppConstants.LOGIN_LOCATION_PERMISSION
                    ) != PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        AppConstants.LOGIN_LOCATION_PERMISSION
                )
                requestPermissions(strings, AppConstants.INITIAL_VALUE_TWO)
            }
        }
    }
    @Suppress("ComplexCondition")
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstants.INITIAL_VALUE_ONE) {
            if (grantResults.size > 1 && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED) {
                Log.i(tag, "onRequestPermissionsResult: apply LOCATION PERMISSION successful")
            } else {
                Log.i(tag, "onRequestPermissionsResult: apply LOCATION PERMISSION  failed")
            }
        }
        if (requestCode == AppConstants.INITIAL_VALUE_TWO) {
            if (grantResults.size > 2 && grantResults[2] == PERMISSION_GRANTED && grantResults[0] == PERMISSION_GRANTED
                    && grantResults[1] == PERMISSION_GRANTED) {
                Log.i(tag, "onRequestPermissionsResult: apply LOCATION PERMISSION successful")
            } else {
                Log.i(tag, "onRequestPermissionsResult: apply LOCATION PERMISSION  failed")
            }
        }
    }

    /**
     * Store data in Shared pref
     * using graph API
     */
    private fun loadFacebookUserProfile(newAccessToken: AccessToken) {
        val graphRequest =
                GraphRequest.newMeRequest(newAccessToken) { `object`, _ ->
                    try {
                        Utils.facebookLogin(this@LoginActivity, `object`)
                        AppPreferences.username = `object`.getString(
                                AppConstants.LOGIN_EMAIL_SCOPE
                        )
                        startDashboardActivity()
                    } catch (e: JSONException) {
                        Log.i(tag, "Facebook load failed")
                    }
                }
        val parameters = Bundle()
        parameters.putString(
                AppConstants.LOGIN_FACEBOOK_FIELDS_KEY,
                "${AppConstants.FACEBOOK_FIELD_FIRST_NAME},${AppConstants.FACEBOOK_FIELD_LAST_NAME}," +
                        "${AppConstants.FACEBOOK_FIELD_EMAIL},${AppConstants.FACEBOOK_FIELD_ID}")
        graphRequest.parameters = parameters
        graphRequest.executeAsync()
    }
    @Suppress("EmptyFunctionBlock")
    override fun onAddOrQuery(BookInfoList: MutableList<LoginInfo>?) {
        Log.w(tag, "onAddOrQuery")
    }
    @Suppress("EmptyFunctionBlock")
    override fun onSubscribe(bookInfoList: MutableList<LoginInfo>?) {
        Log.w(tag, "onSubscribe")
    }
    @Suppress("EmptyFunctionBlock")
    override fun onDelete(BookInfoList: MutableList<LoginInfo>?) {
        Log.w(tag, "onDelete")
    }
    @Suppress("EmptyFunctionBlock")
    override fun updateUiOnError(errorMessage: String?) {
        Log.w(tag, "EmptyFunctionBlock")
    }
    /**
     * Insert data into cloud db
     * Based on table which you pass
     */
    override fun onInitCloud() {
        huaweiAccount?.let {
            AppPreferences.username = it.email
            processAddAction(it)
            huaweiLogin(this@LoginActivity, it)
            mCloudDBZoneWrapper.insertDbZoneInfo(loginInfo)
            startDashboardActivity()
        }
    }

    override fun onInsertSuccess(cloudDBZoneResult: Int?) {
        Utils.showToast(this@LoginActivity, getString(R.string.msg_data_saved_success))
    }
}
