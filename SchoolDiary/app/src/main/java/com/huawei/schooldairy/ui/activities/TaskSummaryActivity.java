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

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.schooldairy.ui.listeners.UiStudentCallBack;
import com.huawei.schooldairy.ui.listeners.UiTaskCallBack;
import com.huawei.schooldairy.databinding.ActivityTaskSummaryBinding;
import com.huawei.schooldairy.model.CloudDBZoneWrapper;
import com.huawei.schooldairy.model.TaskItem;
import com.huawei.schooldairy.model.UserAndTask;
import com.huawei.schooldairy.model.UserData;
import com.huawei.schooldairy.ui.adapters.TaskSumListAdapter;
import com.huawei.schooldairy.userutils.Constants;
import com.huawei.schooldairy.userutils.UserUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for particular task details and students assigned for that particular task
 * @author: Huawei
 * @since: 25-05-2021
 */
public class TaskSummaryActivity extends AbstractBaseActivity implements UiTaskCallBack, UiStudentCallBack {

    private static final String TAG = "TaskSummaryActivity";
    private AGConnectUser user;
    private CloudDBZoneWrapper mCloudDBZoneWrapper;
    private Handler mHandler;
    ActivityTaskSummaryBinding binding;
    private TaskSumListAdapter taskSumListAdapter;
    private List<UserAndTask> taskItems = new ArrayList();
    private String groupId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskSummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showBackArrow();

        if (getIntent().getExtras().containsKey("GroupId")) {
            groupId = getIntent().getStringExtra("GroupId");
        }

        user = AGConnectAuth.getInstance().getCurrentUser();
        taskSumListAdapter = new TaskSumListAdapter(this, taskItems, (itemType, position, taskItem) -> {
            Intent intent = new Intent(TaskSummaryActivity.this, TaskDetailActivity.class);
            intent.putExtra("TaskId", taskItem.getTaskID());
            intent.putExtra("GroupId", taskItem.getGroup_id());
            startActivity(intent);
        });
        binding.taskSummaryList.setAdapter(taskSumListAdapter);
        binding.btnCloseTask.setOnClickListener(v -> closeCurrentTask());

        showProgressDialog("Loading...");
        initCloudDB();

    }

    /**
     * Initialize cloud db and initiate the DB operation for getting TaskList
     */
    private void initCloudDB() {
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            if (null != AGConnectAuth.getInstance().getCurrentUser()) {
                mCloudDBZoneWrapper.addStudentCallBacks(TaskSummaryActivity.this);
                mCloudDBZoneWrapper.addTaskCallBacks(TaskSummaryActivity.this);
                mCloudDBZoneWrapper.createObjectType();
                mCloudDBZoneWrapper.openCloudDBZoneV2(mCloudDBZone -> {
                    getSubmittedTaskList(groupId);
                });
            }
        });
    }

    /**
     * Update the current status of the task as Closed
     */
    private void closeCurrentTask() {
        if (taskItems.size() > 0) {
            List<TaskItem> closeTaskItems = new ArrayList<>();
            for (int i = 0; i < taskItems.size(); i++) {
                TaskItem taskItem = taskItems.get(i).getTaskItem();
                taskItem.setStatus(Constants.STATUS_CLOSED);
                closeTaskItems.add(taskItem);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                mCloudDBZoneWrapper.upsertTaskItems(closeTaskItems, 3);
            });
        }
    }

    /**
     * Get the Task list from the DB
     * @param groupId
     */
    private void getSubmittedTaskList(String groupId) {
        new Handler(Looper.getMainLooper()).post(() -> {
            mCloudDBZoneWrapper.queryTasks(CloudDBZoneQuery.where(TaskItem.class).equalTo("group_id", groupId), 1);
        });
    }


    /**
     * Get the student list from the DB
     */
    private void getStudentList() {
        new Handler().post(() -> {
            mCloudDBZoneWrapper.queryUserData(
                    CloudDBZoneQuery.where(UserData.class)
                            .equalTo("UserType", String.valueOf(Constants.USER_STUDENT))
                            .and()
                            .equalTo("TeacherId", user.getUid()),
                     2);
        });
    }

    /**
     * Display the Task details Overall status.
     * @param taskItem
     * @param pendingCount
     * @param submittedCount
     * @param evaluatedCount
     */
    private void displayTaskDetails(TaskItem taskItem, int pendingCount, int submittedCount, int evaluatedCount) {
        binding.txtTaskName.setText(taskItem.getTaskName());
        binding.txtTaskDesc.setText(taskItem.getTaskDescription());
        String dateString = UserUtil.dateToString(taskItem.getDueDate());
        binding.txtTaskDueDate.setText(UserUtil.utcToLocalString(dateString));
        binding.txtNewCount.setText(String.valueOf(pendingCount));
        binding.txtSubmitCount.setText(String.valueOf(submittedCount));
        binding.txtEvalCount.setText(String.valueOf(evaluatedCount));
        if (taskItem.getStatus() == Constants.STATUS_CLOSED) {
            binding.btnCloseTask.setEnabled(false);
            binding.btnCloseTask.setClickable(false);
        }

    }

    /**
     * Listener method of DB operation
     * OnResult method of retrieve the TaskItem list
     * @param taskItemList
     * @param tag
     */
    @Override
    public void onAddOrQuery(List<TaskItem> taskItemList, int tag) {
        taskItems.clear();
        for (int ind = 0; ind < taskItemList.size(); ind++) {
            UserAndTask userAndTask = new UserAndTask();
            userAndTask.setTaskItem(taskItemList.get(ind));
            taskItems.add(userAndTask);
        }
        getStudentList();
    }
    /**
     * Listener method of DB operation
     * OnError method of retrieve the TaskItem list
     * @param errorMessage
     */
    @Override
    public void updateUiOnError(String errorMessage) {
        hideDialog();
        showToast(errorMessage);
    }
    /**
     * Listener method of DB operation
     * onRefresh of TaskItem operation method
     * @param tag
     */
    @Override
    public void onRefresh(int tag) {
        showProgressDialog("Loading...");
        HomeActivity.NEED_UPDATE = true;
        getSubmittedTaskList(groupId);
    }

    /**
     * Listener method of DB operation
     * OnResult of UserData method
     * @param studentItemList
     * @param tag
     */
    @Override
    public void onStudentAddOrQuery(List<UserData> studentItemList, int tag) {
        int pendingCount = 0, submittedCount = 0, evaluatedCount = 0;
        for (int ind = 0; ind < taskItems.size(); ind++) {
            //Getting counts
            int tStatus = taskItems.get(ind).getTaskItem().getStatus();
            pendingCount += (tStatus == Constants.STATUS_NEW) ? 1 : 0;
            submittedCount += (tStatus == Constants.STATUS_SUBMITTED) ? 1 : 0;
            evaluatedCount += (tStatus == Constants.STATUS_EVALUATED) ? 1 : 0;
            // Map Task and Student
            for (int jnd = 0; jnd < studentItemList.size(); jnd++) {
                if (taskItems.get(ind).getTaskItem().getStudentID().equals(studentItemList.get(jnd).getUserID())) {
                    taskItems.get(ind).setUserData(studentItemList.get(jnd));
                }
            }
        }
        taskSumListAdapter.updateList(taskItems);
        displayTaskDetails(taskItems.get(0).getTaskItem(), pendingCount, submittedCount, evaluatedCount);
        hideDialog();
    }
    /**
     * Listener method of DB operation
     * OnError method of retrieve the UserData list
     * @param errorMessage
     */
    @Override
    public void updateStudentUiOnError(String errorMessage) {
        hideDialog();
        showToast(errorMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCloudDBZoneWrapper != null)
            mCloudDBZoneWrapper.closeCloudDBZone();
    }

}