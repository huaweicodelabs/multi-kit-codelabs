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
package com.huawei.schooldairy.ui.fragment;

import static com.huawei.schooldairy.userutils.Constants.STATUS_CLOSED;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.schooldairy.databinding.FragmentTeachersHomeBinding;
import com.huawei.schooldairy.model.CloudDBZoneWrapper;
import com.huawei.schooldairy.model.TaskItem;
import com.huawei.schooldairy.model.UserData;
import com.huawei.schooldairy.ui.activities.CreateTaskActivity;
import com.huawei.schooldairy.ui.activities.HomeActivity;
import com.huawei.schooldairy.ui.activities.TaskDetailActivity;
import com.huawei.schooldairy.ui.activities.TaskSummaryActivity;
import com.huawei.schooldairy.ui.adapters.TaskListAdapter;
import com.huawei.schooldairy.ui.listeners.UiStudentCallBack;
import com.huawei.schooldairy.ui.listeners.UiTaskCallBack;
import com.huawei.schooldairy.userutils.Constants;
import com.huawei.schooldairy.userutils.PrefUtil;
import com.huawei.schooldairy.userutils.UserUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Home page common fragment for Task list and Task history list
 * @author: Huawei
 * @since: 25-05-2021
 */
public class TaskListFragment extends Fragment implements UiTaskCallBack {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "TaskListFragment";
    private FragmentTeachersHomeBinding binding;
    private static final int REQ_CODE = 1000;

    private int index;
    private TaskListAdapter taskAdapter;
    private CloudDBZoneWrapper mCloudDBZoneWrapper;

    private Handler mHandler;
    private int userType;
    private AGConnectUser user;
    private List<TaskItem> taskItemsList = new ArrayList<>();

    public static TaskListFragment newInstance(int index) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Get the index for differentiate the Fragment in Homepage
     * display as Current Tasks or Task History
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
    }

    /**
     * Initialize views, fab button (if user is teacher), RecyclerView adapters
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentTeachersHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.progressBar.setVisibility(View.VISIBLE);
        if (PrefUtil.getInstance(getActivity()).getBool("IS_MAPPED")) {
            userType = PrefUtil.getInstance(getActivity()).getInt("USER_TYPE");
        }

        if (userType == Constants.USER_TEACHER && index == Constants.TASK_ITEM) {
            binding.fab.setVisibility(View.VISIBLE);
            binding.fab.setOnClickListener(view -> {
                startActivityForResult(new Intent(getActivity(), CreateTaskActivity.class), REQ_CODE);
            });
        } else {
            binding.fab.setVisibility(View.GONE);
        }

        taskAdapter = new TaskListAdapter(getActivity(), taskItemsList, (itemType, position, taskItem) -> {
            if (userType == Constants.USER_TEACHER) {
                Intent intent = new Intent(getActivity(), TaskSummaryActivity.class);
                intent.putExtra("TaskId", taskItem.getTaskID());
                intent.putExtra("GroupId", taskItem.getGroup_id());
                startActivity(intent);
            } else {
                Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
                intent.putExtra("TaskId", taskItem.getTaskID());
                intent.putExtra("GroupId", taskItem.getGroup_id());
                startActivity(intent);
            }
        });
        binding.taskRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.taskRecycler.setAdapter(taskAdapter);

        initCloudDB();

        return root;
    }

    /**
     * Initialize cloud db and initiate the DB operation for getting TaskList
     */
    private void initCloudDB() {
        user = AGConnectAuth.getInstance().getCurrentUser();
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            if (null != AGConnectAuth.getInstance().getCurrentUser()) {
                mCloudDBZoneWrapper.addTaskCallBacks(this);
                mCloudDBZoneWrapper.createObjectType();
                mCloudDBZoneWrapper.openCloudDBZoneV2(mCloudDBZone -> generateTaskListQuery(index));
            }
        });
    }

    /**
     * Create Query for getting the Today's TaskItems list
     * This list may be for Students or Teachers - based on Logged in User
     * This also may be for Current TaskItems or Task History - based on the parameter
     * @param inputValue
     */
    private void generateTaskListQuery(int inputValue) {
        binding.progressBar.setVisibility(View.VISIBLE);

        CloudDBZoneQuery<TaskItem> query;
        Date date = UserUtil.getCurrentDateTimeAsUTC();
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);

        if (inputValue == Constants.TASK_ITEM) {
            query = CloudDBZoneQuery.where(TaskItem.class)
                    .greaterThanOrEqualTo("DueDate", date)
                    .and().notEqualTo("Status", STATUS_CLOSED);

            if (userType == Constants.USER_TEACHER)
                query = query.and().equalTo("CreadtedBy", user.getUid());

            if (userType == Constants.USER_STUDENT)
                query = query.and().equalTo("StudentID", user.getUid());

            getTaskListFromDB(query);

        } else if (inputValue == Constants.TASK_HISTORY_ITEM) {
            query = CloudDBZoneQuery.where(TaskItem.class)
                    .lessThanOrEqualTo("DueDate", date)
                    .and().equalTo("StudentID", user.getUid());
            getTaskListFromDB(query);
        } else {
            query = CloudDBZoneQuery.where(TaskItem.class);
            getTaskListFromDB(query);
        }
    }

    /**
     * Call the method for querying the TaskItems defined
     * in cloud DB wrapper class based on the Query
     * @param query
     */
    private void getTaskListFromDB(CloudDBZoneQuery<TaskItem> query) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mCloudDBZoneWrapper.queryTasks(query, 1);
        }, 500);
    }

    /**
     * Collect the the students list for create Task
     *
     * Add listener for UserData DB operation &
     * Initiate the to collect student list which is mapped to
     * currently logged teacher. On result create Task for all the collected Student.
     * @param data
     */
    public void upsertTaskItem(Intent data) {
        mCloudDBZoneWrapper.addStudentCallBacks(new UiStudentCallBack() {
            @Override
            public void onStudentAddOrQuery(List<UserData> studentItemList, int tag) {
                createAndInsertTaskList(data, studentItemList);
            }

            @Override
            public void updateStudentUiOnError(String errorMessage) {
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        new Handler(Looper.getMainLooper()).post(() -> {
            mCloudDBZoneWrapper.queryUserData(CloudDBZoneQuery.where(UserData.class)
                    .equalTo("TeacherId", user.getUid())
                    .and()
                    .equalTo("UserType", String.valueOf(Constants.USER_STUDENT)), 2);
        });
    }

    /**
     * Create the TaskItem record from the details got from {@link CreateTaskActivity} and
     * Insert TaskItem which is created by the Teacher user through Cloud DB wrapper class.
     * It will create a Task group with common group id
     * and it creates individual task for each student with unique task id.
     * @param taskData
     * @param studentsList
     */
    private void createAndInsertTaskList(Intent taskData, List<UserData> studentsList) {
        List<TaskItem> taskItemList = new ArrayList();
        Date cDate = new Date();
        String taskGroupId = String.valueOf(UUID.randomUUID()); // unique for each Task
        String date = "";
        for (int ind = 0; ind < studentsList.size(); ind++) {
            TaskItem task = new TaskItem();
            task.setTaskID(String.valueOf(UUID.randomUUID()));//unique for each student
            task.setGroup_id(taskGroupId);

            task.setTaskName(taskData.getStringExtra("task_name"));
            task.setTaskDescription(taskData.getStringExtra("task_desc"));
            task.setStatus(Constants.STATUS_NEW);

            task.setStudentID(studentsList.get(ind).getUserID());
            task.setCreadtedBy(user.getUid());

            date = taskData.getStringExtra("due_date");
            task.setDueDate(UserUtil.localToUTCDate(date));
            task.setCreatedDate(cDate);
            taskItemList.add(task);
        }
        mCloudDBZoneWrapper.upsertTaskItems(taskItemList, 0);
    }


    /**
     * Listener method for DB operation
     * OnResult method of retrieve the list from TaskItem table.
     * @param taskItemList
     * @param tag
     */
    @Override
    public void onAddOrQuery(List<TaskItem> taskItemList, int tag) {
        taskItemsList.clear();
        HashMap<String, TaskItem> tempMap = new HashMap<>();
        for (TaskItem taskItem : taskItemList) {
            if (!tempMap.containsKey(taskItem.getGroup_id())) {
                taskItemsList.add(taskItem);
                tempMap.put(taskItem.getGroup_id(), taskItem);
            }
        }
        taskAdapter.updateList(taskItemsList);
        binding.progressBar.setVisibility(View.GONE);
    }

    /**
     * Listener method for DB operation
     * OnError method of anyOperation on the TaskItem table.
     * @param errorMessage
     */
    @Override
    public void updateUiOnError(String errorMessage) {
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * Listener method for DB operation
     * onRefresh method of insert record on the TaskItem table.
     * after insert again the query will be called.
     * @param tag
     */
    @Override
    public void onRefresh(int tag) {
        generateTaskListQuery(index);
    }

    /**
     * onResume method of Activity
     * It have the boolean value for refresh the page
     * NEED_UPDATE if true this page will be refresh else not.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (HomeActivity.NEED_UPDATE) {
            generateTaskListQuery(index);
            HomeActivity.NEED_UPDATE = false;
        }
    }

    /**
     * The result of {@link CreateTaskActivity}
     * Got the Task details such as name, description, due date
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQ_CODE) {
                if (null != data.getExtras()) {
                    upsertTaskItem(data);
                }
            }
        }
    }
}