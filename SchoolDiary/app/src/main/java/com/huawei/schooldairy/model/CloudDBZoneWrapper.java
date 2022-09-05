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
package com.huawei.schooldairy.model;

import android.content.Context;
import android.util.Log;

import com.huawei.agconnect.AGConnectInstance;
import com.huawei.agconnect.AGConnectOptionsBuilder;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;
import com.huawei.schooldairy.SchoolDiaryApplication;
import com.huawei.schooldairy.ui.listeners.DBZoneListener;
import com.huawei.schooldairy.ui.listeners.UiStudentCallBack;
import com.huawei.schooldairy.ui.listeners.UiTaskCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for Cloud DB operations
 * @author: Huawei
 * @since: 25-05-2021
 */
public class CloudDBZoneWrapper {

    private static final String TAG = "CloudDBZoneWrapper";
    private static final String CLOUD_DB_NAME = "SchoolDB";
    private AGConnectCloudDB mCloudDB;
    private CloudDBZone mCloudDBZone;
    private CloudDBZoneConfig mConfig;
    private UiTaskCallBack mUiTaskCallBack = UiTaskCallBack.DEFAULT;
    private UiStudentCallBack mUiStudentCallBack = UiStudentCallBack.DEFAULT;

    public CloudDBZoneWrapper() {
        SchoolDiaryApplication.setRegionRoutePolicy(
                AGConnectInstance.getInstance().getOptions().getRoutePolicy());
        mCloudDB = AGConnectCloudDB.getInstance();
    }

    /**
     * Set the storage location
     */
    public void setStorageLocation(Context context) {
        if (mCloudDBZone != null) {
            closeCloudDBZone();
        }
        AGConnectOptionsBuilder builder = new AGConnectOptionsBuilder()
                .setRoutePolicy(SchoolDiaryApplication.getRegionRoutePolicy());
        AGConnectInstance instance = AGConnectInstance.buildInstance(builder.build(context));
        mCloudDB = AGConnectCloudDB.getInstance(instance, AGConnectAuth.getInstance());
    }

    /**
     * Init AGConnectCloudDB in Application
     * @param context application context
     */
    public static void initAGConnectCloudDB(Context context) {
        AGConnectCloudDB.initialize(context);
    }

    /**
     * Call AGConnectCloudDB.createObjectType to init schema
     */
    public void createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "createObjectType: " + e.getMessage());
        }
    }

    /**
     * Open cloud db zone
     */
    public void openCloudDBZoneV2(DBZoneListener listener) {
        mConfig = new CloudDBZoneConfig(CLOUD_DB_NAME, CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(cloudDBZone -> {
            if (null != listener) {
                listener.getCloudDbZone(cloudDBZone);
            }
            mCloudDBZone = cloudDBZone;
        }).addOnFailureListener(e -> Log.w(TAG, "Open cloudDBZone failed for " + e.getMessage()));
    }

    /**
     * Call AGConnectCloudDB.closeCloudDBZone
     */
    public void closeCloudDBZone() {
        try {
            mCloudDB.closeCloudDBZone(mCloudDBZone);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "closeCloudDBZone: " + e.getMessage());
        }
    }

    /**
     * Add a callback to update Task list
     * @param uiTaskCallBack callback to update task list
     */
    public void addTaskCallBacks(UiTaskCallBack uiTaskCallBack) {
        this.mUiTaskCallBack = uiTaskCallBack;
    }

    /**
     * Add a callback to update User list
     */
    public void addStudentCallBacks(UiStudentCallBack uiStudentCallBack) {
        this.mUiStudentCallBack = uiStudentCallBack;
    }


    /**
     * Query TaskItems
     * @param query query condition
     */
    public void queryTasks(CloudDBZoneQuery<TaskItem> query, int tag) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<TaskItem>> queryTask = mCloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask
                .addOnSuccessListener(snapshot -> processQueryResult(snapshot, tag))
                .addOnFailureListener(e -> mUiTaskCallBack.updateUiOnError("DB Query Error, Something went wrong!"));
    }

    /**
     * Query UserData with condition
     * @param query query condition
     */
    public void queryUserData(CloudDBZoneQuery<UserData> query, int tag) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<UserData>> queryTask = mCloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(snapshot -> processUsersListResult(snapshot, tag))
                .addOnFailureListener(e -> mUiStudentCallBack.updateStudentUiOnError("DB Query Error, Something went wrong!"));
    }

    /**
     * Process the UserData gives the result which is queried
     */
    private void processUsersListResult(CloudDBZoneSnapshot<UserData> snapshot, int tag) {
        CloudDBZoneObjectList<UserData> taskItemCursor = snapshot.getSnapshotObjects();
        List<UserData> studentItemList = new ArrayList<>();
        try {
            while (taskItemCursor.hasNext()) {
                UserData studentItem = taskItemCursor.next();
                studentItemList.add(studentItem);
            }
        } catch (AGConnectCloudDBException e) {
            mUiTaskCallBack.updateUiOnError("DB Upsert Error, Something went wrong!");
        } finally {
            snapshot.release();
        }
        mUiStudentCallBack.onStudentAddOrQuery(studentItemList, tag);
    }

    /**
     * Process the TaskItem list gives the result which is queried
     */
    private void processQueryResult(CloudDBZoneSnapshot<TaskItem> snapshot, int tag) {
        CloudDBZoneObjectList<TaskItem> taskItemCursor = snapshot.getSnapshotObjects();
        List<TaskItem> taskItemList = new ArrayList<>();
        try {
            while (taskItemCursor.hasNext()) {
                TaskItem taskItem = taskItemCursor.next();
                taskItemList.add(taskItem);
            }
        } catch (AGConnectCloudDBException e) {
            mUiTaskCallBack.updateUiOnError("DB Upsert Error, Something went wrong!");
        } finally {
            snapshot.release();
        }
        mUiTaskCallBack.onAddOrQuery(taskItemList, tag);
    }

    /**
     * Upsert single TaskItem
     * @param taskItem TaskItem added or modified from local
     */
    public void upsertTaskItem(TaskItem taskItem, int tag) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(taskItem);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            mUiTaskCallBack.onRefresh(tag);
        }).addOnFailureListener(e -> {
            mUiTaskCallBack.updateUiOnError("DB Upsert Error, Something went wrong!");
        });
    }

    /**
     * Upsert Bulk TaskItems
     * @param taskItem TaskItem added or modified from local
     */
    public void upsertTaskItems(List<TaskItem> taskItem, int tag) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(taskItem);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            mUiTaskCallBack.onRefresh(tag);
        }).addOnFailureListener(e -> {
            mUiTaskCallBack.updateUiOnError("DB Upsert Error, Something went wrong!");
            e.printStackTrace();
        });
    }

    /**
     * Delete TaskItem
     * @param taskItemList tasks selected by user
     */
    public void deleteTaskItems(List<TaskItem> taskItemList) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<Integer> deleteTask = mCloudDBZone.executeDelete(taskItemList);
        if (deleteTask.getException() != null) {
            mUiTaskCallBack.updateUiOnError("DB Deletion Error, Something went wrong!");
            return;
        }
        //mUiTaskCallBack.onDelete(taskItemList);
    }

}