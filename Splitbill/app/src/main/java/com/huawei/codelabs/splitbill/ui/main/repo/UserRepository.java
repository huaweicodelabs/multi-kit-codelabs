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
import com.huawei.codelabs.splitbill.ui.main.helper.Constants;
import com.huawei.codelabs.splitbill.ui.main.models.User;
import com.huawei.codelabs.splitbill.ui.main.models.UserEditFields;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final CloudDBZone mCloudDBZone;
    private ListenerHandler mRegister;
    private final String TAG = "UserRepository";
    private MutableLiveData<List<User>> userLiveData;
    private final MutableLiveData<Boolean> addUpdateSuccess = new MutableLiveData<>();
    MutableLiveData<Boolean> userIfUserExist;
    MutableLiveData<String> userName;
    private final MutableLiveData<Boolean> isUserCreated = new MutableLiveData<>();
    private final MutableLiveData<Integer> userIdLiveData = new MutableLiveData<>();


    public UserRepository(CloudDBZone mCloudDBZone, ListenerHandler mRegister) {
        this.mCloudDBZone = mCloudDBZone;
        this.mRegister = mRegister;
    }

    /**
     * Monitor data change from database. Update group info list live data if data have changed
     */
    private final OnSnapshotListener<User> mSnapshotListener = new OnSnapshotListener<User>() {
        @Override
        public void onSnapshot(CloudDBZoneSnapshot<User> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
            if (e != null) {
                Log.w(TAG, "onSnapshot: " + e.getMessage());
                return;
            }
            CloudDBZoneObjectList<User> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
            try {
                if (snapshotObjects != null) {
                    userIfUserExist.postValue(snapshotObjects.size() > 0);
                } else {
                    userIfUserExist.postValue(false);
                }
            } finally {
                cloudDBZoneSnapshot.release();
            }
        }
    };

    /**
     * Query User info from cloud DB
     *
     * @return groupLiveData - Live data to observe cloud DB query completion
     */
    public MutableLiveData<Boolean> checkIfUserExist(String phoneNumber) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
        }
        userIfUserExist = new MutableLiveData<>();
        try {
            CloudDBZoneQuery<User> snapshotQuery = CloudDBZoneQuery.where(User.class).equalTo(UserEditFields.PHONE, phoneNumber);
            mCloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mSnapshotListener);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "subscribeSnapshot: " + e.getMessage());
        }
        return userIfUserExist;
    }


    /**
     * Query group list info from cloud DB
     *
     * @return groupLiveData - Live data to observe cloud DB query completion
     */
    public MutableLiveData<List<User>> getUserList() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
        }
        userLiveData = new MutableLiveData<>();
        try {
            CloudDBZoneQuery<User> snapshotQuery = CloudDBZoneQuery.where(User.class).equalTo(UserEditFields.STATUS, Constants.STATUS_ACTIVE);
            mRegister = mCloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, new OnSnapshotListener<User>() {
                        @Override
                        public void onSnapshot(CloudDBZoneSnapshot<User> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
                            if (e != null) {
                                Log.w(TAG, "onSnapshot: " + e.getMessage());
                                return;
                            }
                            CloudDBZoneObjectList<User> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
                            ArrayList<User> userInfoList = new ArrayList<>();
                            try {
                                if (snapshotObjects != null) {
                                    while (snapshotObjects.hasNext()) {
                                        User userInfo = snapshotObjects.next();
                                        userInfoList.add(userInfo);
                                    }
                                }
                                userLiveData.postValue(userInfoList);
                            } catch (AGConnectCloudDBException snapshotException) {
                                Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.getMessage());
                            } finally {
                                cloudDBZoneSnapshot.release();
                            }
                        }
                    });
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "subscribeSnapshot: " + e.getMessage());
        }
        return userLiveData;
    }

    /**
     * Add / Edit user info
     *
     * @param user - user object with user info
     * @return isUserCreated - Live data to update staus of DB upsert
     */
    public MutableLiveData<Boolean> upsertUserData(User user) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(user);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                isUserCreated.postValue(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                isUserCreated.postValue(false);
            }
        });
        return isUserCreated;
    }


    // Update group ID to the latest value
    public MutableLiveData<Integer> getUserIdLiveData() {
        final long[] mUserIndex = new long[1];
        CloudDBZoneQuery<User> snapshotQuery = CloudDBZoneQuery.where(User.class);
        Task<Long> countQueryTask = mCloudDBZone.executeCountQuery(snapshotQuery, UserEditFields.ID,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        countQueryTask.addOnSuccessListener(new OnSuccessListener<Long>() {
            @Override
            public void onSuccess(Long aLong) {
                userIdLiveData.postValue((int) (aLong + 1));
                mUserIndex[0] = aLong;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Count query is failed: " + Log.getStackTraceString(e));
            }
        });

        return userIdLiveData;
    }

}
