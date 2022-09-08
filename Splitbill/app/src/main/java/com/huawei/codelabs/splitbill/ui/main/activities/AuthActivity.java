/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
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
package com.huawei.codelabs.splitbill.ui.main.activities;

import static com.huawei.codelabs.splitbill.ui.main.helper.Constants.COUNTRY_CODE;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.PhoneAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.ActivityAuthBinding;
import com.huawei.codelabs.splitbill.ui.main.helper.Common;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.LoginViewModel;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.LoginViewModelFactory;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.api.entity.hwid.HwIDConstant;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AuthActivity extends BaseActivity implements View.OnClickListener {
    public static AuthActivity authActivity;
    boolean progressStats = true;
    public ActivityAuthBinding activityAuthBinding;
    List<Scope> scopeList;
    private HuaweiIdAuthService service;
    private int interval;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        activityAuthBinding = DataBindingUtil.setContentView (this, R.layout.activity_auth);
        activityAuthBinding.authProgressBar.setVisibility(View.INVISIBLE);
        authActivity = this;
        initAuthService();
        initLoginViewModel();
    }
    private void initLoginViewModel() {
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory(getApplication(), this)).get(LoginViewModel.class);
    }
    private void progress(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressStats){
                    activityAuthBinding.authProgressBar.setVisibility(View.INVISIBLE);
                }else if(!progressStats){
                    activityAuthBinding.authProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        },1500);
    }
    private void initAuthService() {
        if (getIntent().getExtras() != null) {
            String mobile_number = getIntent().getExtras().getString("Mobile_Number");
            activityAuthBinding.txtMobileNumber.setText(mobile_number);
        }
        activityAuthBinding.btnSignup.setOnClickListener(this);
        activityAuthBinding.tvGetcode.setOnClickListener(this);
        activityAuthBinding.btnHuaweiId.setOnClickListener(this);
        HuaweiIdAuthParamsHelper huaweiIdAuthParamsHelper = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM);
        scopeList = new ArrayList<>();
        scopeList.add(new Scope(HwIDConstant.SCOPE.ACCOUNT_BASEPROFILE));
        scopeList.add(new Scope(HwIDConstant.SCOPE.SCOPE_ACCOUNT_EMAIL));
        scopeList.add(new Scope(HwIDConstant.SCOPE.SCOPE_MOBILE_NUMBER));
        scopeList.add(new Scope(HwIDConstant.SCOPE.SCOPE_ACCOUNT_PROFILE));
        huaweiIdAuthParamsHelper.setScopeList(scopeList);
        HuaweiIdAuthParams authParams = huaweiIdAuthParamsHelper.setAccessToken().setMobileNumber().createParams();
        service = HuaweiIdAuthManager.getService(this, authParams);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_signup:
                Intent signUpActivity = new Intent(this, SignUpActivity.class);
                startActivity(signUpActivity);
                break;
            case R.id.btnHuaweiId:
                loginViewModel.signInWithHuaweiId(AuthActivity.this, service).observe(AuthActivity.this, new Observer<SignInResult>() {
                    @Override
                    public void onChanged(SignInResult user) {
                        activityAuthBinding.authProgressBar.setVisibility(View.VISIBLE);
                        progress();
                        loginSuccess (user);
                    }
                });
                break;
            case R.id.btnFacebookId:
                break;
            case R.id.tv_getcode:
                sendVerificationCode();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendVerificationCode() {
        VerifyCodeSettings settings = VerifyCodeSettings.newBuilder()
                .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)
                .sendInterval(30) //shortest send interval ，30-120s
                .locale(Locale.SIMPLIFIED_CHINESE) // This is mandatory and shold be Locale.SIMPLIFIED_CHINESE
                .build();
        String countryCode = COUNTRY_CODE;
        String phoneNumber = activityAuthBinding.txtMobileNumber.getText().toString().trim();
        if (!phoneNumber.isEmpty()) {
            Task<VerifyCodeResult> task = PhoneAuthProvider.requestVerifyCode(countryCode, phoneNumber, settings);
            task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
                @Override
                public void onSuccess(VerifyCodeResult verifyCodeResult) {
                    Toast.makeText(AuthActivity.this, "OTP sent successfully.", Toast.LENGTH_SHORT).show();
                    activityAuthBinding.txtVerificationCode.setText("");
                    activityAuthBinding.tvGetcode.setEnabled(false);
                    activityAuthBinding.tvGetcode.setTextColor(getResources().getColor(R.color.black50PercentColor));
                    interval = Integer.parseInt(verifyCodeResult.getShortestInterval());
                    new CountDownTimer(interval * 1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            activityAuthBinding.tvGetcode.setText(String.valueOf(interval));

                            interval--;
                            loginViewModel.loginPhone(activityAuthBinding.txtMobileNumber.getText().toString().trim(), activityAuthBinding.txtVerificationCode.getText().toString().trim(), COUNTRY_CODE).observe(AuthActivity.this, new Observer<SignInResult>() {
                                @Override
                                public void onChanged(SignInResult user) {
                                    activityAuthBinding.authProgressBar.setVisibility(View.VISIBLE);
                                    progress();
                                    loginSuccess (user);
                                }
                            });

                        }

                        @Override
                        public void onFinish() {
                            activityAuthBinding.tvGetcode.setEnabled(true);
                            activityAuthBinding.tvGetcode.setText(getString(R.string.get_code));
                            activityAuthBinding.tvGetcode.setTextColor(getResources().getColor(R.color.design_default_color_primary));
                        }
                    }.start();

                    activityAuthBinding.txtVerificationCode.requestFocus();

                }
            }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    activityAuthBinding.tvGetcode.setEnabled(true);
                    activityAuthBinding.tvGetcode.setText(getString(R.string.get_code));
                    activityAuthBinding.tvGetcode.setTextColor(getResources().getColor(R.color.design_default_color_primary));
                    Toast.makeText(AuthActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Common.showToast(getString(R.string.error_mobile), this);
        }
    }

    private void loginSuccess(SignInResult signInResult) {
        AGConnectUser user = signInResult.getUser();

        Common.setFirstTimeUserLoggedIn (getApplicationContext ( ), false);
        progressStats=true;
        openAndCheckCloudBStatus(loginViewModel, null, activityAuthBinding.txtMobileNumber.getText().toString().trim());
    }


}