/* Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.huawei.codelabs.splitbill.ui.main.repo;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.MutableLiveData;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.auth.PhoneAuthProvider;
import com.huawei.agconnect.auth.PhoneUser;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.TokenResult;
import com.huawei.codelabs.splitbill.ui.main.activities.AuthActivity;
import com.huawei.codelabs.splitbill.ui.main.helper.Common;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

public class LoginRepository {

    MutableLiveData<SignInResult> authenticatedUserMutableLiveData;

    MutableLiveData<SignInResult> phoneauthenticatedUserMutableLiveData;
    ActivityResultLauncher<Intent> huaweiSignIn = AuthActivity.authActivity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(result.getData());
                        if (authAccountTask.isSuccessful()) {
                            AuthAccount authAccount = authAccountTask.getResult();

                            AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(authAccount.getAccessToken());
                            AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                                @Override
                                public void onSuccess(SignInResult signInResult) {
                                    // onSuccess
                                    Common.showToast("log in", AuthActivity.authActivity);
                                    authenticatedUserMutableLiveData.setValue(signInResult);

                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            Common.showToast("fail", AuthActivity.authActivity);
                                        }
                                    });
                        }
                    }
                }
            });

    public LoginRepository(AuthActivity authActivity) {
        // this.authActivity =authActivity;
    }

    public MutableLiveData<SignInResult> signInWithHuaweiID(AuthActivity activity, HuaweiIdAuthService service) {
        authenticatedUserMutableLiveData = new MutableLiveData<>();

        huaweiSignIn.launch(service.getSignInIntent());

        return authenticatedUserMutableLiveData;
    }

    public MutableLiveData<SignInResult> loginPhone(String phonenumber, String verifycode, String countrycode) {
        Log.d("Data:", "create user onSuccess: in");
        phoneauthenticatedUserMutableLiveData = new MutableLiveData<>();
        final String countryCode = countrycode;
        final String phoneNumber = phonenumber;
        final String verifyCode = verifycode;

        PhoneUser phoneUser = new PhoneUser.Builder()
                .setCountryCode("+" + countryCode)
                .setPhoneNumber(phoneNumber)
                .setVerifyCode(verifyCode)
                .build();
        AGConnectAuth.getInstance().createUser(phoneUser)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        Log.d("Data:", "create user onSuccess: ");
                        Task<TokenResult> str = signInResult.getUser().getToken(true);
                        phoneauthenticatedUserMutableLiveData.setValue(signInResult);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e.getMessage().contains("been registered")) {
                            AGConnectAuthCredential credential = PhoneAuthProvider.credentialWithVerifyCode(countryCode, phoneNumber, "", verifyCode);
                            loginRegisteredUser(credential);
                        }
                    }
                });

        return phoneauthenticatedUserMutableLiveData;
    }

    private MutableLiveData<SignInResult> loginRegisteredUser(AGConnectAuthCredential credential) {
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        // Obtain sign-in information.
                        AGConnectUser user = signInResult.getUser();

                        Task<TokenResult> str = signInResult.getUser().getToken(true);

                        phoneauthenticatedUserMutableLiveData.setValue(signInResult);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("Data:", "onFailure: " + e.getMessage());
                    }
                });
        return phoneauthenticatedUserMutableLiveData;
    }


}