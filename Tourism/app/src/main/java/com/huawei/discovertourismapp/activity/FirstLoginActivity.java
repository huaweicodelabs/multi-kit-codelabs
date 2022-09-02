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

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.huawei.discovertourismapp.MainActivity;
import com.huawei.discovertourismapp.R;
import com.huawei.discovertourismapp.fragments.LoginFragment;
import com.huawei.discovertourismapp.fragments.LoginPhoneFragment;
import com.huawei.discovertourismapp.utils.Constants;
import com.huawei.discovertourismapp.utils.TourismSharedPref;

public class FirstLoginActivity extends BaseActivity implements LoginFragment.LoginFragmentListener,
        LoginPhoneFragment.LoginPhoneFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homedecor_login);
        LoginFragment loginPhoneFragment = new LoginFragment();
        loginPhoneFragment.setLoginFragmentListener(this);
        addFragmentToWithoutBackStack(loginPhoneFragment);
    }

    @Override
    public void setLoginPhoneFragmentListener(Bundle bundle) {
        String number = bundle.getString(Constants.PHONE_NUMBER);
        Toast.makeText(FirstLoginActivity.this, getString(R.string.showMessageSuccess), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(FirstLoginActivity.this, MainActivity.class);
        intent.putExtra(Constants.PHONE_NUMBER, number);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        ActivityCompat.finishAffinity(FirstLoginActivity.this);
    }

    @Override
    public void setLoginFragmentListener(Bundle bundle) {
        LoginPhoneFragment loginMobileFragment = new LoginPhoneFragment();
        loginMobileFragment.setArguments(bundle);
        loginMobileFragment.setLoginPhoneFragmentListener(this);
        addFragmentOnStack(loginMobileFragment, LoginPhoneFragment.TAG);
    }
}
