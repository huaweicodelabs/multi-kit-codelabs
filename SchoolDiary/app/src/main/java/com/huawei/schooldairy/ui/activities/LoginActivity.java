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
package com.huawei.schooldairy.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.api.entity.hwid.HwIDConstant;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.huawei.schooldairy.R;
import com.huawei.schooldairy.ui.listeners.UiStudentCallBack;
import com.huawei.schooldairy.databinding.ActivityLoginBinding;
import com.huawei.schooldairy.model.CloudDBZoneWrapper;
import com.huawei.schooldairy.model.UserData;
import com.huawei.schooldairy.userutils.Constants;
import com.huawei.schooldairy.userutils.PrefUtil;

import java.util.ArrayList;
import java.util.List;
/**
 * Login Activity handles the Login process for New and Existing user
 * @author: Huawei
 * @since: 25-05-2021
 */
public class LoginActivity extends AbstractBaseActivity {

    private static final int REQUEST_PERMISSION = 2;
    private static final int REQUEST_CODE_PENDING_INTENT = 0;
    private static final int REQUEST_CODE_SIGN_IN = 8888;
    private static final int UID_LENGTH = 5;
    private CloudDBZone mCloudDBZone;
    private ActivityLoginBinding binding;
    private CloudDBZoneWrapper mCloudDBZoneWrapper;
    private Handler mHandler;
    public static final String TAG = "LoginActivity";

    /**
     * Init views, get permission, login button functionality with Account kit
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginButton.setOnClickListener(view -> {
            showProgressDialog("Login..." );
            HuaweiIdAuthParamsHelper huaweiIdAuthParamsHelper = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM);
            List<Scope> scopeList = new ArrayList<>();
            scopeList.add(new Scope(HwIDConstant.SCOPE.ACCOUNT_BASEPROFILE));
            huaweiIdAuthParamsHelper.setScopeList(scopeList);
            HuaweiIdAuthParams authParams = huaweiIdAuthParamsHelper.setAccessToken().createParams();
            HuaweiIdAuthService service = HuaweiIdAuthManager.getService(LoginActivity.this, authParams);
            startActivityForResult(service.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        });


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(getString(R.string.LOGIN_ACTIVITY_TAG), "sdk < 28 Q");
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
            if (ActivityCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, permissions[1]) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, permissions[2]) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, permissions[3]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this, permissions, 1);
            }
        } else {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE, getString(R.string.BACKGROUND_LOCATION_PERMISSION)};
            if (ActivityCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, permissions[1]) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, permissions[2]) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, permissions[3]) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, permissions[4]) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(LoginActivity.this, permissions, REQUEST_PERMISSION);
            }
        }
    }

    /**
     * Initialize cloud db and initiate the DB operation for getting User
     * who currently logged in with their user id.
     */
    private void initCloudDB() {
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            if (null != AGConnectAuth.getInstance().getCurrentUser()) {
                mCloudDBZoneWrapper.createObjectType();
                mCloudDBZoneWrapper.openCloudDBZoneV2(mCloudDBZone -> {
                    this.mCloudDBZone = mCloudDBZone;
                    queryUserDetails();
                });
            }
        });
    }

    /**
     * Initiate logged in user validation
     */
    @Override
    protected void onResume() {
        super.onResume();
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid().length() > UID_LENGTH) {
            binding.loginButton.setVisibility(View.GONE);
            validateLogin();
        } else {
            binding.loginButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * If login for the first time, on login result, validate the user with cloud DB user table.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.getAccessToken());
                AGConnectAuth.getInstance()
                        .signIn(credential)
                        .addOnSuccessListener(signInResult -> {
                            hideDialog();
                            AGConnectUser user = signInResult.getUser();
                            validateLogin();
                        })
                        .addOnFailureListener(e -> {
                            hideDialog();
                            Log.e(getString(R.string.SIGN_IN_FAILED_TAG), e.getLocalizedMessage());
                        });

            } else {
                Log.e(getString(R.string.SIGN_IN_FAILED_TAG), getString(R.string.SIGN_IN_FAILED_MSG));
                hideDialog();
            }
        }
    }

    /* Here we are validating whether the user is teacher or student and whether it is mapped or not
     * 1.First we are checking the preference value if it is not available we are fetching from
     *  cloud db and updating in shared preference
     * 2.Based on Usertype we are redirecting to the home screen
     * USERTYPE
     * 0 : Constants.USER_STUDENT,
     * 1 : Constants.USER_TEACHER
     * */
    private void validateLogin() {
        if (PrefUtil.getInstance(LoginActivity.this).getBool("IS_MAPPED")) {
            // if the user type is either --> Constants.USER_STUDENT, Constants.USER_TEACHER
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        } else {
            if (PrefUtil.getInstance(LoginActivity.this).getInt("USER_TYPE") == Constants.USER_TEACHER) {
                Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(i);
                finish();
            } else {
                initCloudDB();
            }
        }
    }

    /**
     * Add the Cloud db wrapper listener for UserData table.
     * Getting the user detail with Cloud db wrapper class by
     * the user id which is get from logged in result.
     *
     */
    public void queryUserDetails() {
        if (mCloudDBZone == null) {
            Log.e(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        showProgressDialog("Validating user..." );
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        mCloudDBZoneWrapper.addStudentCallBacks(new UiStudentCallBack() {
            @Override
            public void onStudentAddOrQuery(List<UserData> studentItemList, int tag) {
                hideDialog();
                if (studentItemList.size() > 0) {
                    UserData currentUser = studentItemList.get(0);
                    int userType = Integer.parseInt(currentUser.getUserType());
                    PrefUtil.getInstance(LoginActivity.this).setInt("USER_TYPE", userType);
                    PrefUtil.getInstance(LoginActivity.this).setBool("IS_MAPPED", true);

                    Intent i;
                    if (userType == Constants.USER_STUDENT || userType == Constants.USER_TEACHER)
                        i = new Intent(LoginActivity.this, HomeActivity.class);
                    else
                        i = new Intent(LoginActivity.this, UserSelectionActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(LoginActivity.this, UserSelectionActivity.class);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void updateStudentUiOnError(String errorMessage) {
                hideDialog();
                showToast(errorMessage);
            }
        });
        new Handler().post(() -> {
            mCloudDBZoneWrapper
                    .queryUserData(CloudDBZoneQuery.where(UserData.class)
                    .equalTo("UserID", user.getUid()), 1);
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCloudDBZoneWrapper != null)
            mCloudDBZoneWrapper.closeCloudDBZone();
    }

}