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

package com.huawei.hms.urbanhomeservices.java.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.FacebookAuthProvider;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.api.entity.common.CommonConstant;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.clouddb.CloudDBZoneWrapper;
import com.huawei.hms.urbanhomeservices.java.clouddb.LoginInfo;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;
import com.huawei.hms.urbanhomeservices.java.utils.AppPreferences;
import com.huawei.hms.urbanhomeservices.java.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static java.lang.Math.random;

/**
 * This activity helps in logging based on two conditions :
 * 1 : Huawei SignIn
 * 2 : AuthService SignIn
 * Also Sends user data to CloudDB
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, CloudDBZoneWrapper.UiCallBack<LoginInfo> {

    public static final String TAG = LoginActivity.class.getSimpleName();
    private HuaweiIdAuthService service;
    private LoginInfo loginInfo;
    private AGConnectAuth agConnectAuth;
    private List<Scope> scopes = Arrays.asList(new Scope(AppConstants.LOGIN_EMAIL_SCOPE));
    private String uId = null;
    private CloudDBZoneWrapper<LoginInfo> mCloudDBZoneWrapper;
    private LoginButton signWithFacebookBtn;
    private String profileType;
    private AuthHuaweiId huaweiAccount = null;
    private CallbackManager mCallbackManager = CallbackManager.Factory.create();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        profileType = getIntent().getStringExtra(AppConstants.LOGIN_USER_TYPE);
        loginInfo = new LoginInfo();
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mCloudDBZoneWrapper.setCloudObject(loginInfo);
        mCloudDBZoneWrapper.createObjectType();
        signWithFacebookBtn = findViewById(R.id.signWithFacebookBtn);
        Button hwIdSignIn = findViewById(R.id.hwid_signin);
        signWithFacebookBtn.setPermissions(AppConstants.LOGIN_EMAIL_SCOPE,
                AppConstants.LOGIN_FACEBOOK_PROFILE);
        initAuthService();
        agConnectAuth = AGConnectAuth.getInstance();
        locationPermission();
        hwIdSignIn.setOnClickListener(this);
        signWithFacebookBtn.setOnClickListener(this);
    }

    /**
     * Initialize the AuthService
     * Add Scope list for creating AuthService
     */
    private void initAuthService() {
        HuaweiIdAuthParamsHelper huaweiIdAuthParamsHelper =
                new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM);
        List<Scope> scopeList = new ArrayList<Scope>();
        scopeList.add(new Scope(CommonConstant.SCOPE.ACCOUNT_BASEPROFILE));
        scopeList.add(new Scope(CommonConstant.SCOPE.SCOPE_ACCOUNT_EMAIL));
        scopeList.add(new Scope(CommonConstant.SCOPE.SCOPE_MOBILE_NUMBER));
        scopeList.add(new Scope(CommonConstant.SCOPE.SCOPE_ACCOUNT_PROFILE));
        huaweiIdAuthParamsHelper.setScopeList(scopeList);
        service = HuaweiIdAuthManager.getService(LoginActivity.this, huaweiIdAuthParamsHelper.setAccessToken().setMobileNumber().createParams());
    }

    /**
     * Asking for location permission
     * which is used to fetch current location on home Fragment
     */
    private void locationPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(TAG, "sdk < 28 Q");
            if (checkSelfPermission(
                    ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED
                    && checkSelfPermission(
                    ACCESS_COARSE_LOCATION
            ) != PERMISSION_GRANTED
            ) {
                String[] strings = {
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION
                };
                requestPermissions(strings, AppConstants.INITIAL_VALUE_ONE);
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
                String[] strings = {
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        AppConstants.LOGIN_LOCATION_PERMISSION
                };
                requestPermissions(strings, AppConstants.INITIAL_VALUE_TWO);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hwid_signin:
                startAuthService();
                break;
            case R.id.signWithFacebookBtn:
                Log.d("Hi This is facebook", "login");
                signWithFacebookBtn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AccessToken accessToken = loginResult.getAccessToken();
                        Profile profile = Profile.getCurrentProfile();
                        String token = loginResult.getAccessToken().getToken();
                        AGConnectAuthCredential credential = FacebookAuthProvider.credentialWithToken(token);
                        if (accessToken != null) {
                            loadFacebookUserProfile(accessToken);
                        }
                        agConnectAuth.signIn(credential)
                                .addOnSuccessListener(signInResult -> {
                                    AGConnectUser user = signInResult.getUser();
                                    uId = user.getUid();
                                    Utils.showToast(LoginActivity.this, getString(R.string.msg_login_success));
                                })
                                .addOnFailureListener(e -> {
                                    Log.d("Hi This is facebook", "login");
                                    Utils.showToast(LoginActivity.this, getString(R.string.msg_login_error));
                                });
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                        Utils.showToast(LoginActivity.this, getString(R.string.msg_login_cancelled));
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                        Utils.showToast(LoginActivity.this, getString(R.string.msg_login_error));
                    }
                });
        }
    }

    /**
     * Start AuthService and request the scope parameters
     */
    private void startAuthService() {
        new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setUid()
                .setProfile()
                .setMobileNumber()
                .setEmail()
                .setIdToken()
                .setAccessToken()
                .setAuthorizationCode()
                .setScopeList(scopes)
                .createParams();
        startActivityForResult(service.getSignInIntent(), AppConstants.LOGIN_AUTH_CODE);
    }

    /**
     * Add data into cloud DB login table
     *
     * @param data  Huawei auth id data
     */
    public void processAddAction(AuthHuaweiId data) {
        int userIdRandomRange = (int) random();
        loginInfo.getUserId(userIdRandomRange);
        loginInfo.device_token = "";
        if (data.getAvatarUri() != null) {
            loginInfo.getPhotoUri(String.valueOf(data.getAvatarUri()));
        } else {
            loginInfo.photo_uri = "";
            loginInfo.user_email = data.getEmail();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case AppConstants.LOGIN_AUTH_CODE:
                    AGConnectAuth.getInstance().signOut();
                    agConnectAuth.signOut();
                    Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
                    if (authHuaweiIdTask.isSuccessful()) {
                        huaweiAccount = authHuaweiIdTask.getResult();
                        agConnectAuth.signIn(HwIdAuthProvider.credentialWithToken(huaweiAccount.getAccessToken()))
                                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                                    @Override
                                    public void onSuccess(SignInResult signInResult) {
                                        AGConnectUser user = signInResult.getUser();
                                        user.getDisplayName();
                                        user.getEmail();
                                        user.getUid();
                                        user.getProviderInfo();
                                        mCloudDBZoneWrapper.setmUiCallBack(LoginActivity.this);
                                        mCloudDBZoneWrapper.openCloudDBZoneV2();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e(TAG, "SignIn failed");
                                    }
                                });
                    } else {
                        Log.e(TAG, "SignIn failed");
                    }
                    break;
                case AppConstants.LOGIN_FACEBOOK_RESULTCODE:
                    mCallbackManager.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * Start dashboard activity
     */
    private void startDashboardActivity() {
        if (profileType.equals(getString(R.string.consumer))) {
            AppPreferences.setIsLogin(true);
            AppPreferences.setUserType(AppConstants.LOGIN_CONSUMER_TYPE);
            Intent intentMainActivity = new Intent(this, MainActivity.class);
            intentMainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentMainActivity);
            finishAffinity();
        } else if (profileType.equals(getString(R.string.service_provider))) {
            AppPreferences.setIsLogin(true);
            AppPreferences.setUserType(AppConstants.SERVICE_PROVIDER_TYPE);
            Intent intent = new Intent(this, AddServiceActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finishAffinity();
        }
    }

    /**
     * Store data in Shared pref
     * using graph API
     *
     * @param newAccessToken access token for FB
     */
    private void loadFacebookUserProfile(AccessToken newAccessToken) {

        Bundle params = new Bundle();
        params.putString(AppConstants.LOGIN_FACEBOOK_FIELDS_KEY, "id,name,email");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            Log.e("JSON", response.toString());
                            JSONObject data = response.getJSONObject();
                            try {
                                Utils.facebookLogin(LoginActivity.this, data);
                                // AppConstants.LOGIN_EMAIL_SCOPE
                                AppPreferences.setUsername(data.optString(AppConstants.LOGIN_EMAIL_SCOPE));
                                startDashboardActivity();
                            } catch (JSONException e) {
                                Log.i(TAG, "Facebook load failed");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    public void onAddOrQuery(List<LoginInfo> dbZoneList) {

    }

    @Override
    public void onSubscribe(List<LoginInfo> dbZoneList) {

    }

    @Override
    public void onDelete(List<LoginInfo> dbZoneList) {

    }

    @Override
    public void updateUiOnError(String errorMessage) {

    }

    @Override
    public void onInitCloud() {
        AppPreferences.setUsername(huaweiAccount.getEmail());
        processAddAction(huaweiAccount);
        Utils.huaweiLogin(LoginActivity.this, huaweiAccount);
        mCloudDBZoneWrapper.insertDbZoneInfo(loginInfo);
        startDashboardActivity();
    }

    @Override
    public void onInsertSuccess(Integer cloudDBZoneResult) {
        Utils.showToast(LoginActivity.this, getString(R.string.msg_data_saved_success));
    }
}

