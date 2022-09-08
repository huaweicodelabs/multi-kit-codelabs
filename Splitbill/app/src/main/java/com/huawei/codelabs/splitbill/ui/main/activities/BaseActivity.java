/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
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

import static com.huawei.codelabs.splitbill.ui.main.helper.Constants.USER;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.ui.SplitBillApplication;
import com.huawei.codelabs.splitbill.ui.main.helper.Constants;
import com.huawei.codelabs.splitbill.ui.main.models.LoginUser;
import com.huawei.codelabs.splitbill.ui.main.models.User;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.BaseViewModel;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Update Cloud DB status
    protected void openAndCheckCloudBStatus(BaseViewModel baseViewModel, LoginUser user, String phoneNumber) {
        baseViewModel.initAndCheckCloudDBStatus(((SplitBillApplication) getApplication()).getCloudDBZoneWrapper());
        baseViewModel.openCloudDBLiveData.observe(this, isOpened -> {
            if (isOpened) {
                if (phoneNumber != null)
                    checkIfRegisteredUser(baseViewModel, user, phoneNumber);
                else
                    goToMainActivity(user);
            }
        });
    }

    protected void checkIfRegisteredUser(BaseViewModel baseViewModel, LoginUser loginUser, String phoneNumber) {
        baseViewModel.checkIfUserExist(phoneNumber);
        baseViewModel.isRegisteredUser.observe(BaseActivity.this, userExist -> {
            if (userExist) goToMainActivity(loginUser);
            else createUser(baseViewModel, loginUser, phoneNumber);
        });
    }

    protected void createUser(BaseViewModel baseViewModel, LoginUser loginUser, String phoneNumber) {
        baseViewModel.getUserId().observe(BaseActivity.this, userId -> {
            AGConnectUser agConnectUser = AGConnectAuth.getInstance().getCurrentUser();
            User user = new User();
            user.setId(userId);
            user.setPhone(phoneNumber);
            user.setAgc_user_id(agConnectUser.getUid());
            user.setStatus(Constants.STATUS_ACTIVE);
            insertUserData(baseViewModel, user, loginUser);
        });
    }

    protected void insertUserData(BaseViewModel baseViewModel, User dbUser, LoginUser loginUser) {
        baseViewModel.upsertUserData(dbUser).observe(BaseActivity.this, userCreated -> {
            if (userCreated)
                goToMainActivity(loginUser);
            else
                Toast.makeText(BaseActivity.this, getString(R.string.add_user_failed), Toast.LENGTH_LONG).show();
        });
    }

    // Go to main activity
    protected void goToMainActivity(LoginUser loginUser) {
        Intent mIntent = new Intent(this, MainActivity.class);
        if (loginUser != null) {
            mIntent.putExtra(USER, loginUser);
        }
        startActivity(mIntent);
        finish();
    }
}
