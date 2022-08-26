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

package com.huawei.tiktoksample.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;
import com.huawei.tiktoksample.db.clouddb.User;
import com.huawei.tiktoksample.db.dao.CloudDBHelper;
import com.huawei.tiktoksample.db.dao.DBConstants;

public class UserProfile extends ViewModel {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    public MutableLiveData<Boolean> userMutableLiveData = new MutableLiveData<>();
    public MutableLiveData<User> userLiveData = new MutableLiveData<>();
    public void saveUser(User user, Context context) {

        CloudDBHelper.getInstance().openDb((isConnected, cloudDBZone) -> {
            if (isConnected && cloudDBZone != null) {
                if (cloudDBZone == null) {
                    return;
                } else {
                    Task<Integer> insertTask = cloudDBZone.executeUpsert(user);
                    insertTask.addOnSuccessListener(integer -> {
                        userMutableLiveData.setValue(true);
                        CloudDBHelper.getInstance().closeDb(context);
                    }).addOnFailureListener(e -> {
                        userMutableLiveData.setValue(false);
                        CloudDBHelper.getInstance().closeDb(context);
                    });
                }
            }
        });
    }

    public void query(String email, Context context) {
        this.context = context;
        CloudDBZoneQuery<User> query = CloudDBZoneQuery.where(User.class).equalTo(DBConstants.USER_EMAIL, email);
        processNumberCheck(query);
    }

    private void processNumberCheck(CloudDBZoneQuery<User> query) {
        CloudDBHelper.getInstance().openDb((isConnected, cloudDBZone) -> {
            if (cloudDBZone != null) {
                Task<CloudDBZoneSnapshot<User>> queryTask = cloudDBZone.executeQuery(query,
                        CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
                queryTask.addOnSuccessListener(this::processSnapShot);
            }
        });
    }

    private void processSnapShot(CloudDBZoneSnapshot<User> userCloudDBZoneSnapshot) {
        try {
            if (userCloudDBZoneSnapshot.getSnapshotObjects() != null &&
                    userCloudDBZoneSnapshot.getSnapshotObjects().size() >= 1) {
                CloudDBZoneObjectList<User> userCloudDBZoneObjectList = userCloudDBZoneSnapshot.getSnapshotObjects();
                User user = userCloudDBZoneObjectList.get(0);
                userLiveData.setValue(user);
            } else {
                userLiveData.setValue(null);
            }
        } catch (AGConnectCloudDBException e) {
            userLiveData.setValue(null);
            e.printStackTrace();
        }
        CloudDBHelper.getInstance().closeDb(context);
    }


}
