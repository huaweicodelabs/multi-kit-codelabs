/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
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

package com.huawei.hms.urbanhomeservices.kotlin.clouddb;

import android.content.Context;
import android.util.Log;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.OnSnapshotListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Proxying implementation of CloudDBZone.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class CloudDBZoneWrapper<T extends CloudDBZoneObject> {
    private static final String TAG = CloudDBZoneWrapper.class.getName();
    private final AGConnectCloudDB mCloudDB;
    private CloudDBZone mCloudDBZone;

    /**
     * This method is used to set UI call back
     *
     * @param mUiCallBack Used to provide overridden methods
     */
    public void setmUiCallBack(UiCallBack mUiCallBack) {
        this.mUiCallBack = mUiCallBack;
    }

    private UiCallBack<T> mUiCallBack;
    private T cloudObject;

    /**
     * sets the cloud object
     *
     * @param cloudObject used to set Cloud object.
     */
    public void setCloudObject(T cloudObject) {
        if (cloudObject != null) {
            this.cloudObject = cloudObject;
        }
    }

    /**
     * This method is used to get instance of AGConnectCloudDB
     */
    public CloudDBZoneWrapper() {
        mCloudDB = AGConnectCloudDB.getInstance();
    }

    /**
     * initialize the Cloud DB
     *
     * @param context This method is used to initialize AGConnectCloudDB
     */
    public static void initAGConnectCloudDB(Context context) {
        AGConnectCloudDB.initialize(context);
    }

    /**
     * This method is used to add subscription
     */
    private void addSubscription() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        try {
            AGConnectAuth.getInstance().getCurrentUser();
            CloudDBZoneQuery<T> snapshotQuery = (CloudDBZoneQuery<T>) CloudDBZoneQuery
                    .where(cloudObject.getClass()).equalTo(AppConstants.SHADOW_FLG, true);
            mCloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mSnapshotListener);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "subscribeSnapshot: AGConnectCloudDBException");
        }
    }

    /**
     * This method is used to processQueryResult
     *
     * @param snapshot cloud db zone snap shot
     */
    private void processQueryResult(CloudDBZoneSnapshot<T> snapshot) {
        CloudDBZoneObjectList<T> dbZoneCursor = snapshot.getSnapshotObjects();
        List<T> dbZoneList = new ArrayList<>();
        try {
            while (dbZoneCursor.hasNext()) {
                T dbZoneInfo = dbZoneCursor.next();
                dbZoneList.add(dbZoneInfo);
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "processQueryResult: AGConnectCloudDBException");
        } finally {
            snapshot.release();
        }
        mUiCallBack.onAddOrQuery(dbZoneList);
    }

    /**
     * This interface contain callback method such Add, Subscribe, Delete etc.
     *
     * @param <T> cloud db zone object class
     */
    public interface UiCallBack<T extends CloudDBZoneObject> {
        void onAddOrQuery(List<T> dbZoneList);

        void onSubscribe(List<T> dbZoneList);

        void onDelete(List<T> dbZoneList);

        void updateUiOnError(String errorMessage);

        void onInitCloud();

        void onInsertSuccess(Integer cloudDBZoneResult);
    }

    /**
     * This listener used to get snapshot.
     */
    private OnSnapshotListener<T> mSnapshotListener = (cloudDBZoneSnapshot, e) -> {
        if (e != null) {
            Log.w(TAG, "onSnapshot");
            return;
        }
        CloudDBZoneObjectList<T> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
        List<T> dbZoneList = new ArrayList<>();
        try {
            if (snapshotObjects != null) {
                while (snapshotObjects.hasNext()) {
                    T objectInfo = snapshotObjects.next();
                    dbZoneList.add(objectInfo);
                }
            }
            mUiCallBack.onSubscribe(dbZoneList);
        } catch (AGConnectCloudDBException snapshotException) {
            Log.w(TAG, "onSnapshot:(getObject)");
        } finally {
            cloudDBZoneSnapshot.release();
        }
    };

    /**
     * This method is used to open cloud db.
     */
    public void openCloudDBZoneV2() {
        CloudDBZoneConfig mConfig = new CloudDBZoneConfig(AppConstants.URBAN_HOME_SERVICES,
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(cloudDBZone -> {
            Log.w(TAG, "open clouddbzone success");
            mCloudDBZone = cloudDBZone;
            mUiCallBack.onInitCloud();
            addSubscription();
        }).addOnFailureListener(e ->
                Log.w(TAG, "open clouddbzone failed"));
    }

    /**
     * This method is used to insert data in cloud db.
     *
     * @param objectInfo object info
     */
    public void insertDbZoneInfo(T objectInfo) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(objectInfo);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            mUiCallBack.onInsertSuccess(cloudDBZoneResult);
        }).addOnFailureListener(e -> {
            mUiCallBack.updateUiOnError("Insert table info failed");
        });
    }

    /**
     * This method is used to create object type
     */
    public void createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "createObjectType: AGConnectCloudDBException ");
        }
    }

    /**
     * This method is used to query all data.
     *
     * @param query cloud db zone query
     */
    public void queryAllData(CloudDBZoneQuery<T> query) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<T>> queryTask = mCloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<T>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<T> snapshot) {
                processQueryResult(snapshot);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mUiCallBack.updateUiOnError("Query failed");
            }
        });
    }

    /**
     * Delete T
     *
     * @param tableObject selected by user
     */
    public void deleteTableData(List<T> tableObject) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<Integer> deleteTask = mCloudDBZone.executeDelete(tableObject);
        if (deleteTask.getException() != null) {
            mUiCallBack.updateUiOnError("Delete book info failed");
            return;
        }
        mUiCallBack.onDelete(tableObject);
    }
}
