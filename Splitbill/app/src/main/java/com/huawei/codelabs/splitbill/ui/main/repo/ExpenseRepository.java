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
import com.huawei.codelabs.splitbill.ui.main.models.Expense;
import com.huawei.codelabs.splitbill.ui.main.models.ExpenseEditFields;
import com.huawei.codelabs.splitbill.ui.main.models.GroupEditFields;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository extends BaseRepository {
    private final String TAG = "ExpenseRepository";

    private MutableLiveData<List<Expense>> expenseLiveData;
    private final MutableLiveData<Boolean> expenseUpdateSuccess = new MutableLiveData<>();

    /**
     * Monitor data change from database. Update expense info list live data if data have changed
     */
    private final OnSnapshotListener<Expense> mSnapshotListener = new OnSnapshotListener<Expense>() {
        @Override
        public void onSnapshot(CloudDBZoneSnapshot<Expense> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
            if (e != null) {
                Log.w(TAG, "onSnapshot: " + e.getMessage());
                return;
            }
            CloudDBZoneObjectList<Expense> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
            ArrayList<Expense> expenseInfoList = new ArrayList<>();
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        Expense expenseInfo = snapshotObjects.next();
                        expenseInfoList.add(expenseInfo);
                    }
                }
                expenseLiveData.postValue(expenseInfoList);
            } catch (AGConnectCloudDBException snapshotException) {
                Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.getMessage());
            } finally {
                cloudDBZoneSnapshot.release();
            }
        }
    };


    public ExpenseRepository(CloudDBZone mCloudDBZone, ListenerHandler mRegister) {
        this.mCloudDBZone = mCloudDBZone;
        this.mRegister = mRegister;
    }

    /**
     * Query expense list info from cloud DB
     *
     * @return expenseLiveData - Live data to observe cloud DB query completion
     */
    public MutableLiveData<List<Expense>> getExpenseList(int groupId) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
        }
        expenseLiveData = new MutableLiveData<>();
        try {

            CloudDBZoneQuery<Expense> snapshotQuery = CloudDBZoneQuery.where(Expense.class).equalTo(ExpenseEditFields.STATUS, 1).equalTo(ExpenseEditFields.GROUP_ID,groupId);
            mRegister = mCloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mSnapshotListener);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "subscribeSnapshot: " + e.getMessage());
        }
        return expenseLiveData;
    }


    /**
     * Query expense list info from cloud DB
     *
     * @return expenseData - Live data to observe cloud DB query completion
     */
    public MutableLiveData<List<Expense>> getExpensebyId(int expenseId) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
        }
        expenseLiveData = new MutableLiveData<>();
        try {

            CloudDBZoneQuery<Expense> snapshotQuery = CloudDBZoneQuery.where(Expense.class).equalTo(ExpenseEditFields.STATUS, 1).equalTo(ExpenseEditFields.ID,expenseId);
            mRegister = mCloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mSnapshotListener);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "subscribeSnapshot: " + e.getMessage());
        }
        return expenseLiveData;
    }


    /**
     * Add / Edit expense info
     *
     * @param expense - expense object with expense info
     * @return expenseUpdateSuccess - Live data to update staus of DB upsert
     */
    public MutableLiveData<Boolean> upsertExpenseData(Expense expense) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
        }

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(expense);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                expenseUpdateSuccess.postValue(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                expenseUpdateSuccess.postValue(false);
            }
        });
        return expenseUpdateSuccess;
    }

}
