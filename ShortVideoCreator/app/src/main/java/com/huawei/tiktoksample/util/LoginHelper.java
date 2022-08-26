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

package com.huawei.tiktoksample.util;

import android.app.Activity;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.SignInResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Handle login events
 */
public class LoginHelper {
    private static final String TAG = "LoginHelper";

    private List<OnLoginEventCallBack> mLoginCallbacks = new ArrayList<>();

    private Activity mActivity;

    public LoginHelper(Activity activity) {
        mActivity = activity;
    }

    public void login() {
        AGConnectAuth auth = AGConnectAuth.getInstance();
        auth.signInAnonymously().addOnSuccessListener(mActivity, signInResult -> {
            for (OnLoginEventCallBack loginEventCallBack : mLoginCallbacks) {
                loginEventCallBack.onLogin(true, signInResult);
            }
        }).addOnFailureListener(mActivity, e -> {
            for (OnLoginEventCallBack loginEventCallBack : mLoginCallbacks) {
                loginEventCallBack.onLogOut(false);
            }
        });
    }

    public void logOut() {
        AGConnectAuth auth = AGConnectAuth.getInstance();
        auth.signOut();
    }

    public void addLoginCallBack(OnLoginEventCallBack loginEventCallBack) {
        if (!mLoginCallbacks.contains(loginEventCallBack)) {
            mLoginCallbacks.add(loginEventCallBack);
        }
    }

    public interface OnLoginEventCallBack {
        void onLogin(boolean showLoginUserInfo, SignInResult signInResult);

        void onLogOut(boolean showLoginUserInfo);
    }

}
