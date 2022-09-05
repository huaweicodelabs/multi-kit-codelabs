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

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.schooldairy.ui.listeners.UiStudentCallBack;
import com.huawei.schooldairy.ui.listeners.UiTaskCallBack;
import com.huawei.schooldairy.databinding.ActivityStudentDetailBinding;
import com.huawei.schooldairy.model.CloudDBZoneWrapper;
import com.huawei.schooldairy.model.TaskItem;
import com.huawei.schooldairy.model.UserData;
import com.huawei.schooldairy.ui.adapters.TaskListAdapter;

import java.util.List;

/**
 * Activity holds the information and Tasks of the one particular Student
 * @author: Huawei
 * @since: 25-05-2021
 */
public class StudentDetailActivity extends AbstractBaseActivity implements UiTaskCallBack {

    private ActivityStudentDetailBinding binding;
    private CloudDBZoneWrapper mCloudDBZoneWrapper;
    private Handler mHandler;
    private String studentId = "";
    private TaskListAdapter taskListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStudentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle("Task History");

        if (getIntent().getExtras().containsKey("student_id")) {
            studentId = getIntent().getExtras().getString("student_id");
        }

        binding.rcStudentList.setLayoutManager(new LinearLayoutManager(StudentDetailActivity.this, LinearLayoutManager.VERTICAL, false));
        taskListAdapter = new TaskListAdapter(StudentDetailActivity.this, null, (itemType, position, taskItem) -> {
            Intent intent = new Intent(StudentDetailActivity.this, TaskDetailActivity.class);
            intent.putExtra("TaskId", taskItem.getTaskID());
            intent.putExtra("GroupId", taskItem.getGroup_id());
            startActivity(intent);
        });
        binding.rcStudentList.setAdapter(taskListAdapter);
        showProgressDialog("Loading...");
        initCloudDB();
    }

    /**
     * Initialize cloud DB and initiate the DB operation to get Student List
     */
    private void initCloudDB() {
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            if (null != AGConnectAuth.getInstance().getCurrentUser()) {
                mCloudDBZoneWrapper.addTaskCallBacks(this);
                mCloudDBZoneWrapper.createObjectType();
                mCloudDBZoneWrapper.openCloudDBZoneV2(mCloudDBZone -> {
                    queryUserDetails();
                });
            }
        });
    }


    /**
     * Add UserData DB operation listener
     * Get Student list through DB wrapper class
     */
    public void queryUserDetails() {
        mCloudDBZoneWrapper.addStudentCallBacks(new UiStudentCallBack() {
            @Override
            public void onStudentAddOrQuery(List<UserData> studentItemList, int tag) {
                hideDialog();
                if (studentItemList.size() > 0) {
                    UserData currentUser = studentItemList.get(0);
                    binding.txtStudentName.setText(currentUser.getUserName());
                    binding.txtStudentDetail.setText("My Student");
                    getTaskList(currentUser.getUserID());
                }
            }

            @Override
            public void updateStudentUiOnError(String errorMessage) {
                hideDialog();
                showToast(errorMessage);
            }
        });
        new Handler().post(() -> {
            mCloudDBZoneWrapper.queryUserData(CloudDBZoneQuery.where(UserData.class).equalTo("UserID", studentId), 1);
        });
    }

    /**
     * Create query to get the particular student data.
     * @param studentId
     */
    private void getTaskList(String studentId) {
        CloudDBZoneQuery<TaskItem> query;
        query = CloudDBZoneQuery.where(TaskItem.class).equalTo("StudentID", studentId);
        getListData(query);
    }

    /**
     *
     * @param query
     */
    private void getListData(CloudDBZoneQuery<TaskItem> query) {
        new Handler().post(() -> {
            mCloudDBZoneWrapper.queryTasks(query, 1);
        });
    }

    /**
     * Listener method of DB operation
     * OnResult of retrieve TaskItem list
     * @param taskItemList
     * @param tag
     */
    @Override
    public void onAddOrQuery(List<TaskItem> taskItemList, int tag) {
        hideDialog();
        taskListAdapter.updateList(taskItemList);
    }

    /**
     * Listener method of DB operation
     * onError of retrieve TaskItem list
     * @param errorMessage
     */
    @Override
    public void updateUiOnError(String errorMessage) {
        hideDialog();
        showToast(errorMessage);
    }

    /**
     * Listener method of DB operation
     * onRefresh of retrieve TaskItem list
     * @param tag
     */
    @Override
    public void onRefresh(int tag) {

    }
}