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

package com.huawei.codelabs.splitbill.ui.main.repo;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.OnSnapshotListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.codelabs.splitbill.ui.main.helper.Constants;
import com.huawei.codelabs.splitbill.ui.main.models.Group;
import com.huawei.codelabs.splitbill.ui.main.models.GroupEditFields;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class GroupRepository extends BaseRepository {

    private final String TAG = "GroupRepository";
    private MutableLiveData<List<Group>> groupLiveData;
    private final MutableLiveData<Boolean> addUpdateSuccess = new MutableLiveData<>();
    private final MutableLiveData<Integer> groupIdLiveData = new MutableLiveData<>();

    /**
     * Monitor data change from database. Update group info list live data if data have changed
     */
    private final OnSnapshotListener<Group> mSnapshotListener = new OnSnapshotListener<Group>() {
        @Override
        public void onSnapshot(CloudDBZoneSnapshot<Group> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
            if (e != null) {
                Log.w(TAG, "onSnapshot: " + e.getMessage());
                return;
            }
            CloudDBZoneObjectList<Group> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
            ArrayList<Group> groupInfoList = new ArrayList<>();
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        Group groupInfo = snapshotObjects.next();
                        groupInfoList.add(groupInfo);
                    }
                }
                groupLiveData.postValue(groupInfoList);
            } catch (AGConnectCloudDBException snapshotException) {
                Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.getMessage());
            } finally {
                cloudDBZoneSnapshot.release();
            }
        }
    };


    public GroupRepository(CloudDBZone mCloudDBZone, ListenerHandler mRegister) {
        this.mCloudDBZone = mCloudDBZone;
        this.mRegister = mRegister;
    }

    /**
     * Query group list info from cloud DB
     * @return groupLiveData - Live data to observe cloud DB query completion
     */
    public MutableLiveData<List<Group>> getGroupList() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
        }
        groupLiveData = new MutableLiveData<>();
        try {
            CloudDBZoneQuery<Group> snapshotQuery = CloudDBZoneQuery.where(Group.class).equalTo(GroupEditFields.STATUS, Constants.STATUS_ACTIVE);
            mRegister = mCloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mSnapshotListener);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "subscribeSnapshot: " + e.getMessage());
        }
        return groupLiveData;
    }

    /**
     * Add / Edit group info
     * @param group - group object with group info
     * @return addUpdateSuccess - Live data to update staus of DB upsert
     */
    public MutableLiveData<Boolean> upsertGroupData(Group group) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(group);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                addUpdateSuccess.postValue(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                addUpdateSuccess.postValue(false);
            }
        });
        return addUpdateSuccess;
    }

    /**
     * Get total no. of records in the group
     * @return live data with record count
     */
    public MutableLiveData<Integer> getGroupIdLiveData() {
        CloudDBZoneQuery<Group> snapshotQuery = CloudDBZoneQuery.where(Group.class);
        Task<Long> countQueryTask = mCloudDBZone.executeCountQuery(snapshotQuery, GroupEditFields.ID,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        countQueryTask.addOnSuccessListener(new OnSuccessListener<Long>() {
            @Override
            public void onSuccess(Long aLong) {
                Log.i(TAG, "The total number of groups is " + aLong);
                groupIdLiveData.postValue((int) (aLong + 1));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Count query is failed: " + Log.getStackTraceString(e));
            }
        });

        return groupIdLiveData;
    }
}
