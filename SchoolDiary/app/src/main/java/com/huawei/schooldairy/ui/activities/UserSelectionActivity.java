
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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.hmf.tasks.Task;
import com.huawei.schooldairy.databinding.ActivityUserSelectionBinding;
import com.huawei.schooldairy.model.CloudDBZoneWrapper;
import com.huawei.schooldairy.model.UserData;
import com.huawei.schooldairy.userutils.Constants;
import com.huawei.schooldairy.userutils.PrefUtil;

/**
 * Activity for user type selection who is Teacher/Student
 * @author: Huawei
 * @since: 25-05-2021
 */
public class UserSelectionActivity extends AbstractBaseActivity {

    public static final String TAG = "UserSelectionActivity";
    private int FLAG = 0;
    private CloudDBZone mCloudDBZone;
    private CloudDBZoneWrapper mCloudDBZoneWrapper;
    private Handler mHandler;
    ActivityUserSelectionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //User type - Choosing spinner
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FLAG = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.btnNxt.setOnClickListener(view -> {
            if (FLAG == Constants.USER_STUDENT) {
                startActivity(new Intent(UserSelectionActivity.this, StudentMapActivity.class));
            } else {
                initCloudDB();
            }
        });

    }

    /**
     * Initialize cloud db and initiate the DB operation for getting TaskList
     */
    private void initCloudDB() {
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            if (null != AGConnectAuth.getInstance().getCurrentUser()) {
                mCloudDBZoneWrapper.createObjectType();
                mCloudDBZoneWrapper.openCloudDBZoneV2(mCloudDBZone -> {
                    this.mCloudDBZone = mCloudDBZone;
                    insertUserType();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCloudDBZoneWrapper != null)
            mCloudDBZoneWrapper.closeCloudDBZone();
    }


    /**
     * Insert the user type in the cloud db who is logged in
     */
    public void insertUserType() {
        if (mCloudDBZone == null) {
            Log.e(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        showProgressDialog("Loading...");
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        UserData userData = new UserData();
        userData.setUserID(user.getUid());
        userData.setUserName(user.getDisplayName());
        userData.setUserType(String.valueOf(Constants.USER_TEACHER));

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(userData);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {

            hideDialog();
            Toast.makeText(UserSelectionActivity.this, "TeacherMapActivity_user_insert_success " + cloudDBZoneResult + " records", Toast.LENGTH_SHORT).show();

            // Save the mapped status and current logged in user type whether he is a Student or Teacher
            PrefUtil.getInstance(UserSelectionActivity.this).setInt("USER_TYPE", Constants.USER_TEACHER);
            PrefUtil.getInstance(UserSelectionActivity.this).setBool("IS_MAPPED", true);

            Intent i = new Intent(UserSelectionActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        });
        upsertTask.addOnFailureListener(e -> {
            hideDialog();
            Log.e(TAG, e.getMessage());
            Toast.makeText(UserSelectionActivity.this, "insert_failed " + e.getLocalizedMessage() + " records", Toast.LENGTH_SHORT).show();
        });
    }
}
