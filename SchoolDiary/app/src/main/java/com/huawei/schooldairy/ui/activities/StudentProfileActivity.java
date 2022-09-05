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
package com.huawei.schooldairy.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.schooldairy.databinding.ActivityStudentProfileBinding;
import com.huawei.schooldairy.userutils.UserUtil;

/**
 * Activity holds the student information.
 * @author: Huawei
 * @since: 25-05-2021
 */

public class StudentProfileActivity extends AbstractBaseActivity {

    ActivityStudentProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStudentProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        showBackArrow();
        initViews();
    }

    /**
     * Initialize views for Display Student profile
     */
    private void initViews() {
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        binding.tvStudentName.setText(user.getDisplayName());
        binding.tvStudentId.setText(UserUtil.hideInfoWithStar(user.getUid()));
        binding.tvEmailId.setText(user.getEmail());

        binding.tvScan.setOnClickListener(v -> {
            Intent intent = new Intent(StudentProfileActivity.this, StudentMapActivity.class);
            intent.putExtra("initFrom", "StudentProfileActivity");
            startActivity(intent);
        });
    }
}