/* Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
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
import com.huawei.codelabs.splitbill.ui.main.models.Friends;
import com.huawei.codelabs.splitbill.ui.main.models.FriendsEditFields;
import com.huawei.codelabs.splitbill.ui.main.models.Group;
import com.huawei.codelabs.splitbill.ui.main.models.GroupEditFields;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class FriendsRepository extends BaseRepository {
    public static final String TAG = "FriendsRepository";
    private MutableLiveData<List<Friends>> friendsLiveData;
    private final MutableLiveData<Boolean> friendsUpdateSuccess = new MutableLiveData<>();
    private final MutableLiveData<Integer> friendssLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> friendsIdLiveData = new MutableLiveData<>();
    /**
     * Monitor data change from database. Update expense info list live data if data have changed
     */
    private final OnSnapshotListener<Friends> mSnapshotListener = new OnSnapshotListener<Friends>() {
        @Override
        public void onSnapshot(CloudDBZoneSnapshot<Friends> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
            if (e != null) {
                Log.w(TAG, "onSnapshot: " + e.getMessage());
                return;
            }
            CloudDBZoneObjectList<Friends> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
            ArrayList<Friends> friendsInfoList = new ArrayList<>();
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        Friends friendsInfo = snapshotObjects.next();
                        friendsInfoList.add(friendsInfo);
                    }
                }
                friendsLiveData.postValue(friendsInfoList);
            } catch (AGConnectCloudDBException snapshotException) {
                Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.getMessage());
            } finally {
                cloudDBZoneSnapshot.release();
            }
        }
    };

    public FriendsRepository(CloudDBZone mCloudDBZone, ListenerHandler mRegister) {
        this.mCloudDBZone = mCloudDBZone;
        this.mRegister = mRegister;
    }

    public MutableLiveData<List<Friends>> getFriendsList() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
        }
        friendsLiveData = new MutableLiveData<>();
        try {
            CloudDBZoneQuery<Friends> snapshotQuery = CloudDBZoneQuery.where(Friends.class);
            Task<CloudDBZoneSnapshot<Friends>> queryTask = mCloudDBZone.executeQuery(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
            queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<Friends>>() {
                @Override
                public void onSuccess(CloudDBZoneSnapshot<Friends> snapshot) {
                    processQueryResult(snapshot);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Failed : " + e);
                }
            });

        } catch (Exception e) {
            Log.w(TAG, "subscribeSnapshot: " + e.getMessage());
        }
        return friendsLiveData;
    }

    private void processQueryResult(CloudDBZoneSnapshot<Friends> snapshot) {
        CloudDBZoneObjectList<Friends> friendsInfoCursor = snapshot.getSnapshotObjects();
        List<Friends> friendsInfoList = new ArrayList<>();
        try {
            while (friendsInfoCursor.hasNext()) {
                Friends friendsInfo = friendsInfoCursor.next();
                friendsInfoList.add(friendsInfo);
            }
            friendsLiveData.postValue(friendsInfoList);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "processQueryResult: " + e.getMessage());
        }
        snapshot.release();

    }

    public MutableLiveData<Boolean> upsertExpenseData(Friends friends) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
        }

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(friends);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                friendsUpdateSuccess.postValue(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                friendsUpdateSuccess.postValue(false);
            }
        });
        return friendsUpdateSuccess;
    }

    public MutableLiveData<Integer> getFriendsIdLiveData() {
        CloudDBZoneQuery<Friends> snapshotQuery = CloudDBZoneQuery.where(Friends.class);
        Task<Long> countQueryTask = mCloudDBZone.executeCountQuery(snapshotQuery, FriendsEditFields.CONTACT_ID,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        countQueryTask.addOnSuccessListener(new OnSuccessListener<Long>() {
            @Override
            public void onSuccess(Long aLong) {
                Log.i(TAG, "The total number of groups is " + aLong);
                friendsIdLiveData.postValue((int) (aLong + 1));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Count query is failed: " + Log.getStackTraceString(e));
            }
        });

        return friendsIdLiveData;
    }

}
