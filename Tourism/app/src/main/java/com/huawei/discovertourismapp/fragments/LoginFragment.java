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
package com.huawei.discovertourismapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.discovertourismapp.R;
import com.huawei.discovertourismapp.utils.Constants;
import com.huawei.discovertourismapp.utils.Util;
import com.huawei.discovertourismapp.utils.Validation;
import com.huawei.discovertourismapp.viewmodel.AuthServiceViewModel;

public class LoginFragment extends Fragment {
    private LoginFragmentListener loginFragmentListener;
    private AuthServiceViewModel authServiceViewModel;

    public void setLoginFragmentListener(LoginFragmentListener loginFragmentListener) {
        this.loginFragmentListener = loginFragmentListener;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authServiceViewModel = new ViewModelProvider(requireActivity()).get(AuthServiceViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_phone, container, false);

        Button mOtpBtn = view.findViewById(R.id.otp_btn);
        EditText etPhoneNumber = view.findViewById(R.id.etPhoneNumber);

        mOtpBtn.setOnClickListener(v -> {

            Bundle bundle = new Bundle();
            if (Validation.isValidation(etPhoneNumber.getText().toString(), "+91")) {
                Util util=new Util();
                util.showProgressBar(getActivity());
                bundle.putString(Constants.PHONE_NUMBER, etPhoneNumber.getText().toString());
                bundle.putString(Constants.COUNTRY_CODE, "+91");
                authServiceViewModel.getOTP("+91", etPhoneNumber.getText().toString());
                authServiceViewModel.verifyCodeResultMutableLiveData.observe(requireActivity(), verifyCodeResult -> {
                    loginFragmentListener.setLoginFragmentListener(bundle);
                    util.stopProgressBar();
                });

            } else {
                Toast.makeText(requireContext(), R.string.enter_number, Toast.LENGTH_SHORT).show();
            }

        });
        return view;


    }

    public interface LoginFragmentListener {
        public void setLoginFragmentListener(Bundle bundle);
    }
}