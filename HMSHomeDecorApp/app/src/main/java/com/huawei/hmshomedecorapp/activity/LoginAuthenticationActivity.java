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

package com.huawei.hmshomedecorapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton;
import com.huawei.hmshomedecorapp.R;
import com.huawei.hmshomedecorapp.utils.Constants;
import com.huawei.hmshomedecorapp.utils.SharedPreferenceUtilClass;

public class LoginAuthenticationActivity extends AppCompatActivity {


    SharedPreferenceUtilClass sharedPreferenceUtilClass;
    HuaweiIdAuthButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication_login);
        loginButton = findViewById(R.id.login_btn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void login() {
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        AccountAuthParams authParams =
                new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                        .setAccessToken()
                        .createParams();
        AccountAuthService accountAuthService = AccountAuthManager.getService(this, authParams);
        startActivityForResult(accountAuthService.getSignInIntent(), 1111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1111) {
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                AuthAccount authAccount = authAccountTask.getResult();
                sharedPreferenceUtilClass = SharedPreferenceUtilClass.getInstance(LoginAuthenticationActivity.this);
                sharedPreferenceUtilClass.saveData(Constants.USER_EMAIL, authAccount.getEmail());
                sharedPreferenceUtilClass.saveData(Constants.USER_NAME, authAccount.getDisplayName());
                sharedPreferenceUtilClass.saveData(Constants.USER_PROFILE_IMAGE, authAccount.getAvatarUri().toString());
                Log.i("TAG", "accessToken:" + authAccount.getAccessToken());
                useTokenToAuthorize(authAccount.getAccessToken());
            }
        }
    }

    public void useTokenToAuthorize(String token) {
        AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(token);
        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        AGConnectUser user = signInResult.getUser();
                        startActivity(new Intent(LoginAuthenticationActivity.this, MainActivityWithDrawer.class));

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e.getMessage().contains("already sign in a user")) {
                            AGConnectUser agConnectUser = AGConnectAuth.getInstance().getCurrentUser();
                            startActivity(new Intent(LoginAuthenticationActivity.this, MainActivityWithDrawer.class));
                        }
                        Log.d("failed because ", e.getMessage());
                    }

                });
    }

}