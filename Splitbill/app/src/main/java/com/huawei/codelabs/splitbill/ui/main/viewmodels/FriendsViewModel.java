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
package com.huawei.codelabs.splitbill.ui.main.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.huawei.codelabs.splitbill.ui.SplitBillApplication;
import com.huawei.codelabs.splitbill.ui.main.db.CloudDBZoneWrapper;
import com.huawei.codelabs.splitbill.ui.main.models.Expense;
import com.huawei.codelabs.splitbill.ui.main.models.Friends;
import com.huawei.codelabs.splitbill.ui.main.repo.ExpenseRepository;
import com.huawei.codelabs.splitbill.ui.main.repo.FriendsRepository;

import java.util.List;

public class FriendsViewModel extends BaseViewModel{
    private final CloudDBZoneWrapper mCloudDBZoneWrapper;
    private static final String TAG="FriendsViewModel";
    private MutableLiveData<List<Friends>> friendsLiveData;
    private final FriendsRepository friendsRepository;
    public FriendsViewModel(@NonNull Application application) {
        super(application);
        mCloudDBZoneWrapper = ((SplitBillApplication) application).getCloudDBZoneWrapper();
        friendsRepository = new FriendsRepository(mCloudDBZoneWrapper.getCloudDBZone(), mCloudDBZoneWrapper.getHandler());
    }

    public MutableLiveData<List<Friends>> getFriendsLiveData() {
        friendsLiveData = friendsRepository.getFriendsList();
        return friendsLiveData;
    }

    public MutableLiveData<Boolean> upsertFriendsData(Friends friends) {
        return friendsRepository.upsertExpenseData(friends);
    }
    public MutableLiveData<Integer> getFriendsId() {
        return friendsRepository.getFriendsIdLiveData() ;
    }
}
