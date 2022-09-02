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
package com.huawei.discovertourismapp.activity;

import static android.content.ContentValues.TAG;
import static com.huawei.discovertourismapp.utils.Constants.REQUEST_CODE_SIGN_IN;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.core.content.ContextCompat;

import com.huawei.discovertourismapp.MainActivity;
import com.huawei.discovertourismapp.utils.TourismSharedPref;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.huawei.hms.support.api.entity.common.CommonConstant;
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton;
import com.huawei.discovertourismapp.R;
import com.huawei.discovertourismapp.utils.AppLog;
import com.huawei.discovertourismapp.utils.Constants;

public class LoginActivity extends BaseActivity {
    HuaweiIdAuthButton huaweiIdAuthButton;
    private AccountAuthService mAuthService;
    private AccountAuthParams mAuthParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        makeFullScreen();
        setContentView(R.layout.activity_login);
        changeStatusBarColor(ContextCompat.getColor(this, R.color.colorGreen));
        Button btnLogin = findViewById(R.id.btn_login_mobile);
        huaweiIdAuthButton = findViewById(R.id.btn_login_account);
        huaweiIdAuthButton.setOnClickListener(v -> {
            signInByHwId();
        });
        btnLogin.setOnClickListener(v -> {
            {
                onLoginClick();
            }
        });
    }

    private void signInByHwId() {
        mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setEmail()
                .createParams();
        mAuthService = AccountAuthManager.getService(this, mAuthParam);
        Task<AuthAccount> task = mAuthService.silentSignIn();
        task.addOnSuccessListener(authAccount -> signinResult(authAccount));
        task.addOnFailureListener(e -> {
            if (e instanceof ApiException) {
                Intent signInIntent = mAuthService.getSignInIntent();
                signInIntent.putExtra(CommonConstant.RequestParams.IS_FULL_SCREEN, true);
                startActivityForResult(signInIntent, Integer.parseInt(REQUEST_CODE_SIGN_IN));
            }
        });
    }

    private void signinResult(AuthAccount authAccount) {
        TourismSharedPref.initializeInstance(this);
        TourismSharedPref.getInstance().putString(Constants.USER_NAME, authAccount.getDisplayName());
        TourismSharedPref.getInstance().putString(Constants.EMAIL, authAccount.getEmail());
        TourismSharedPref.getInstance().putString(Constants.ALREADY_LOGIN, "1");
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("name", authAccount.getDisplayName());
        i.putExtra("email", authAccount.getEmail());
        startActivity(i);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Integer.parseInt(Constants.REQUEST_CODE_SIGN_IN)) {
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                AuthAccount authAccount = authAccountTask.getResult();
                signinResult(authAccount);
            } else {
                AppLog.logE(TAG, "sign in failed : " + ((ApiException) authAccountTask.getException()).getStatusCode());
            }
        }
    }

    private void onLoginClick() {
        Intent intent = new Intent(LoginActivity.this, FirstLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
