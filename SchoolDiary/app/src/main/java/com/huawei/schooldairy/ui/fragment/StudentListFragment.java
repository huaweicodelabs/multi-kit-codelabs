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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.schooldairy.ui.listeners.UiStudentCallBack;
import com.huawei.schooldairy.databinding.FragmentTeachersHomeBinding;
import com.huawei.schooldairy.model.CloudDBZoneWrapper;
import com.huawei.schooldairy.model.UserData;
import com.huawei.schooldairy.ui.activities.HomeActivity;
import com.huawei.schooldairy.ui.activities.StudentDetailActivity;
import com.huawei.schooldairy.ui.adapters.StudentListAdapter;
import com.huawei.schooldairy.userutils.Constants;

import java.util.List;

/**
 * Home page fragment containing students list
 * @author: Huawei
 * @since: 25-05-2021
 */
public class StudentListFragment extends Fragment implements UiStudentCallBack {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private FragmentTeachersHomeBinding binding;

    private int index;
    private HomeActivity mActivity;
    private StudentListAdapter studentAdapter;
    private CloudDBZoneWrapper mCloudDBZoneWrapper;
    private Handler mHandler;
    private String teacherId;

    public static StudentListFragment newInstance(int index) {
        StudentListFragment fragment = new StudentListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Get the index for differentiate the Fragment in Homepage
     * display this Fragment or TaskFragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (HomeActivity) getActivity();
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
    }

    /**
     * Initiate ViewBinding, Student list Adapter, hide fab button
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTeachersHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.fab.setVisibility(View.GONE);
        studentAdapter = new StudentListAdapter(getActivity(), null, (itemType, position, studentItem) -> {
            Intent intent = new Intent(getActivity(), StudentDetailActivity.class);
            intent.putExtra("student_id", studentItem.getUserID());
            intent.putExtra("student_name", studentItem.getUserID());
            startActivity(intent);
        });
        binding.taskRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.taskRecycler.setAdapter(studentAdapter);
        initCloudDB();
        return root;
    }

    /**
     * Initialize cloud db and initiate the DB operation for getting TaskList
     */
    private void initCloudDB() {
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            if (null != AGConnectAuth.getInstance().getCurrentUser()) {
                teacherId = AGConnectAuth.getInstance().getCurrentUser().getUid();
                mCloudDBZoneWrapper.addStudentCallBacks(this);
                mCloudDBZoneWrapper.createObjectType();
                mCloudDBZoneWrapper.openCloudDBZoneV2(mCloudDBZone -> getStudentList(index, teacherId));
            }
        });
    }

    /**
     * Create the query for get students list
     * Intitate the DB operation
     * @param inputValue
     * @param teacherId
     */
    private void getStudentList(int inputValue, String teacherId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        CloudDBZoneQuery<UserData> query;
        if (inputValue == Constants.STUDENT_ITEM) {
            query = CloudDBZoneQuery.where(UserData.class)
                    .equalTo("TeacherId", teacherId)
                    .and()
                    .equalTo("UserType", String.valueOf(Constants.USER_STUDENT));
            getListData(query);
        } else {
            query = CloudDBZoneQuery.where(UserData.class);
            getListData(query);
        }
    }

    /**
     * Call the DB wrapper class method, to get UserData
     * @param query
     */
    private void getListData(CloudDBZoneQuery<UserData> query) {
        new Handler().postDelayed(() -> {
            mCloudDBZoneWrapper.queryUserData(query, 1);
        }, 300);
    }


    /**
     * Lisener method of DB operation
     * OnResult method of retrieve Student list
     * @param studentItemList
     * @param tag
     */
    @Override
    public void onStudentAddOrQuery(List<UserData> studentItemList, int tag) {
        studentAdapter.updateList(studentItemList);
        binding.progressBar.setVisibility(View.GONE);
    }

    /**
     * Lisener method of DB operation
     * OnError method of retrieve Student list
     * @param errorMessage
     */
    @Override
    public void updateStudentUiOnError(String errorMessage) {
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }
}