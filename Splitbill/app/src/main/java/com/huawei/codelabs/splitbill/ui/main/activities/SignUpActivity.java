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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.PhoneAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.ActivitySignUpBinding;
import com.huawei.codelabs.splitbill.ui.main.helper.Common;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.SignUPViewModel;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.SignUPViewModelFactory;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;

import java.util.Locale;

public class SignUpActivity extends BaseActivity implements View.OnClickListener {

    public ActivitySignUpBinding activitySignUpBinding;
    boolean progressStats = true;
    private int interval;
    private SignUPViewModel signUPViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySignUpBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        activitySignUpBinding.authProgressBar.setVisibility(View.INVISIBLE);
        initAuthService();
        initLoginViewModel();
    }

    private void initLoginViewModel() {
        signUPViewModel = new ViewModelProvider(this, new SignUPViewModelFactory(getApplication(), this)).get(SignUPViewModel.class);
    }

    private void progress() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (progressStats) {
                activitySignUpBinding.authProgressBar.setVisibility(View.INVISIBLE);
            } else if (!progressStats) {
                activitySignUpBinding.authProgressBar.setVisibility(View.INVISIBLE);
            }
        }, 1500);
    }

    private void initAuthService() {
        if (getIntent().getExtras() != null) {
            String mobile_number = getIntent().getExtras().getString("Mobile_Number");
            activitySignUpBinding.txtMobileNumber.setText(mobile_number);
        }
        activitySignUpBinding.tvGetcode.setOnClickListener(this);

        activitySignUpBinding.txtVerificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 6) {
                    loginWithSignUpCode();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_getcode) {
            sendVerificationCode();
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
        String phoneNumber = activitySignUpBinding.txtMobileNumber.getText().toString().trim();
        if (!phoneNumber.isEmpty()) {
            Task<VerifyCodeResult> task = PhoneAuthProvider.requestVerifyCode(COUNTRY_CODE, phoneNumber, settings);
            task.addOnSuccessListener(TaskExecutors.uiThread(), verifyCodeResult -> {
                Toast.makeText(SignUpActivity.this, "OTP sent successfully.", Toast.LENGTH_SHORT).show();
                activitySignUpBinding.txtVerificationCode.setText("");
                activitySignUpBinding.tvGetcode.setEnabled(false);
                activitySignUpBinding.tvGetcode.setTextColor(getResources().getColor(R.color.black50PercentColor));
                interval = Integer.parseInt(verifyCodeResult.getShortestInterval());
                new CountDownTimer(interval * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        activitySignUpBinding.tvGetcode.setText(String.valueOf(interval));
                        interval--;
                        signUPViewModel.loginPhone(activitySignUpBinding.txtMobileNumber.getText().toString().trim(), activitySignUpBinding.txtVerificationCode.getText().toString().trim(), COUNTRY_CODE).observe(SignUpActivity.this, user -> {
                            activitySignUpBinding.authProgressBar.setVisibility(View.VISIBLE);
                            progress();
                            signUPSuccess(user);
                        });

                    }

                    @Override
                    public void onFinish() {
                        activitySignUpBinding.tvGetcode.setEnabled(true);
                        activitySignUpBinding.tvGetcode.setText(getString(R.string.get_code));
                        activitySignUpBinding.tvGetcode.setTextColor(getResources().getColor(R.color.design_default_color_primary));
                    }
                }.start();

                activitySignUpBinding.txtVerificationCode.requestFocus();

            }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    activitySignUpBinding.tvGetcode.setEnabled(true);
                    activitySignUpBinding.tvGetcode.setText(getString(R.string.get_code));
                    activitySignUpBinding.tvGetcode.setTextColor(getResources().getColor(R.color.design_default_color_primary));
                    Toast.makeText(SignUpActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Common.showToast(getString(R.string.error_mobile), this);
        }
    }

    private void signUPSuccess(SignInResult signInResult) {
        Intent signUpActivity = new Intent(this, AuthActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Mobile_Number", activitySignUpBinding.txtMobileNumber.getText().toString());
        signUpActivity.putExtras(bundle);
        AGConnectAuth.getInstance().signOut();
        startActivity(signUpActivity);
        finish();
    }


    private void loginWithSignUpCode() {
        signUPViewModel.loginPhone(activitySignUpBinding.txtMobileNumber.getText().toString().trim(), activitySignUpBinding.txtVerificationCode.getText().toString().trim(), COUNTRY_CODE).observe(SignUpActivity.this, user -> {
            activitySignUpBinding.authProgressBar.setVisibility(View.VISIBLE);
            progress();
            signUPSuccess(user);
        });
    }
}