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

package com.huawei.hms.urbanhomeservices.kotlin.clouddb;

import android.app.Activity;
import android.util.Log;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

/**
 * It's a helper class for login process.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class LoginHelper {
    private static final String TAG = LoginHelper.class.getName();
    private List<OnLoginEventCallBack> mLoginCallbacks = new ArrayList<>();
    private Activity mActivity;

    public LoginHelper(Activity activity) {
        mActivity = activity;
    }

    /**
     * This method is used to anonymous sign-in
     */
    public void login() {
        logOut();
        AGConnectAuth auth = AGConnectAuth.getInstance();
        auth.signInAnonymously().addOnSuccessListener(mActivity, new OnSuccessListener<SignInResult>() {
            @Override
            public void onSuccess(SignInResult signInResult) {
                Log.w(TAG, "addOnSuccessListener");
                for (OnLoginEventCallBack loginEventCallBack : mLoginCallbacks) {
                    loginEventCallBack.onLogin(true, signInResult);
                }
            }
        }).addOnFailureListener(mActivity, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "sign in for agc failed: ");
                for (OnLoginEventCallBack loginEventCallBack : mLoginCallbacks) {
                    loginEventCallBack.onLogOut(false);
                }
            }
        });
    }

    /**
     * This method is used to AGConnect Auth Service logout
     */
    public void logOut() {
        AGConnectAuth auth = AGConnectAuth.getInstance();
        auth.signOut();
    }

    /**
     * This method is used to provide callback methods
     *
     * @param loginEventCallBack call back of login event
     */
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
