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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.clouddb.LoginInfo;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;
import com.huawei.hms.urbanhomeservices.java.utils.AppPreferences;
import com.huawei.hms.urbanhomeservices.java.utils.Utils;

/**
 * This activity basically helps to select user type :
 * 1 : Consumer type
 * 2 : Service provider type.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private LoginInfo loginInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        locationPermission();
        checkUserLoginType();
        loginInfo = new LoginInfo();
        String[] items = getResources().getStringArray(R.array.select_user_type);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spnLogin = findViewById(R.id.spnLogin);
        spnLogin.setAdapter(adapter);
        Button btnNext = findViewById(R.id.btnnext);
        btnNext.setOnClickListener(v -> {
            if (spnLogin.getSelectedItemId() == AppConstants.INTIAL_VALUE) {
                Utils.showToast(v.getContext(), getString(R.string.select_type));
            }
            checkLogin(spnLogin);
        });
    }

    /**
     * Check if the user is already signed in based on user type and navigate to particular activity
     */
    private void checkUserLoginType() {
        if (AppPreferences.isLogin() && AppPreferences.getUserType().equals(getResources().getString(R.string.select_consumer_type))) {
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        } else if (AppPreferences.isLogin() && AppPreferences.getUserType().equals(getResources().getString(R.string.select_provider_type))) {
            Intent intent = new Intent(SplashActivity.this, AddServiceActivity.class);
            intent.putExtra(AppConstants.LOGIN_USER_TYPE, getResources().getString(R.string.select_provider_type));
            startActivity(intent);
            finish();
        }
    }

    /**
     * Select the user type from spinner
     *
     * @param spnLogin user type drop down.
     */
    private void checkLogin(Spinner spnLogin) {
        if (spnLogin.getSelectedItemId() == AppConstants.INITIAL_VALUE_ONE) {
            if (AppPreferences.isLogin() && AppPreferences.getUserType().equals(getResources().getString(R.string.select_consumer_type))) {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                AppPreferences.setIsLogin(true);
                AppPreferences.setUserType(getResources().getString(R.string.select_consumer_type));
                mainIntent.putExtra(AppConstants.LOGIN_USER_TYPE, spnLogin.getSelectedItem().toString());
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
                finish();
            } else {
                Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                loginIntent.putExtra(AppConstants.LOGIN_USER_TYPE, spnLogin.getSelectedItem().toString());
                startActivity(loginIntent);
            }
        } else if (spnLogin.getSelectedItemId() == AppConstants.INITIAL_VALUE_TWO) {
            if (AppPreferences.isLogin() && AppPreferences.getUserType().equals(getResources().getString(R.string.select_provider_type))) {
                Intent intent = new Intent(SplashActivity.this, AddServiceActivity.class);
                AppPreferences.setIsLogin(true);
                AppPreferences.setUserType(getResources().getString(R.string.select_provider_type));
                intent.putExtra(AppConstants.LOGIN_USER_TYPE, spnLogin.getSelectedItem().toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                loginIntent.putExtra(AppConstants.LOGIN_USER_TYPE, spnLogin.getSelectedItem().toString());
                startActivity(loginIntent);
            }
        }
    }

    /**
     * Asking for location permission
     * which is used to fetch current location on home Fragment
     */
    private void locationPermission() {
        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(SplashActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(SplashActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(SplashActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Utils.showToast(this, "Permission Granted");
                    }
                } else {
                    Utils.showToast(this, "Permission Denied");
                }
            }
        }
    }
}
