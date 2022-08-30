package com.huawei.hms.smartnewsapp.java.ui.View;

/*
 *
 *  * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.agconnect.crash.AGConnectCrash;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.smartnewsapp.R;
import com.huawei.hms.smartnewsapp.java.util.Constants;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton;

/**
 * Activity that displays the silent login screen
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "SmartNewsApp";
    HuaweiIdAuthButton huawei_SignIn;
    HuaweiIdAuthParams authParams;
    HuaweiIdAuthService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        huawei_SignIn = findViewById(R.id.hwi_sign_in);
        AGConnectCrash agConnectCrash = AGConnectCrash.getInstance();
        agConnectCrash.enableCrashCollection(true);
        authParams =
                new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                        .setAuthorizationCode()
                        .createParams();
        service = HuaweiIdAuthManager.getService(MainActivity.this, authParams);
        silentsign();
        huawei_SignIn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleHuaweiSignIn();
                    }
                });


        HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(this);
    }

    /*
     * sign in
     *
     */
    private void handleHuaweiSignIn() {
        startActivityForResult(service.getSignInIntent(), Constants.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Process the authorization result and obtain the authorization code from AuthHuaweiId.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                String name = huaweiAccount.getDisplayName();
                String email = huaweiAccount.getEmail();
                SharedPreferences.Editor editor = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putBoolean("login", true);
                editor.putString("name", name);
                editor.putString("email", email);
                editor.apply();
                editor.commit();
                Intent intent = new Intent(MainActivity.this, NewsActivity.class);
                startActivity(intent);

                Toast.makeText(
                                this,
                                getApplicationContext().getResources().getString(R.string.able_to_login),
                                Toast.LENGTH_LONG)
                        .show();
                finish();

            } else {
                // The sign-in failed.
                Log.e(TAG, getApplication().getResources().getString(R.string.sigin_failed));
                Toast.makeText(
                                this,
                                getApplicationContext().getResources().getString(R.string.unable_to_login),
                                Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    /*
     *  silent ssign in
     *
     */

    public void silentsign() {
        Task<AuthHuaweiId> task = service.silentSignIn();
        task.addOnSuccessListener(
                authAccount -> {
                    SharedPreferences.Editor editor = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putBoolean("login", true);
                    editor.putString("name", authAccount.getDisplayName());
                    editor.apply();
                    editor.commit();
                    Intent intent = new Intent(MainActivity.this, NewsActivity.class);
                    startActivity(intent);
                    finish();
                });
        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // The sign-in failed. Try to sign in explicitly using getSignInIntent().
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            Log.i(TAG, "sign failed status:" + apiException.getStatusCode());
                        }
                        huawei_SignIn.setVisibility(View.VISIBLE);

                    }
                });
    }
}
