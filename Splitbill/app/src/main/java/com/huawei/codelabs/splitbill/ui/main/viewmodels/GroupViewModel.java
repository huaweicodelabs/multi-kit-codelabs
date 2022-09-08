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
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.codelabs.splitbill.ui.SplitBillApplication;
import com.huawei.codelabs.splitbill.ui.main.db.CloudDBZoneWrapper;
import com.huawei.codelabs.splitbill.ui.main.models.Group;
import com.huawei.codelabs.splitbill.ui.main.repo.GroupRepository;

import java.util.ArrayList;
import java.util.List;

public class GroupViewModel extends BaseViewModel {
    private final GroupRepository groupRepository;
    public MutableLiveData<List<Group>> groupsLiveData;
    protected AGConnectAuth agConnectAuth;
    private Handler mHandler = null;
    private final CloudDBZoneWrapper mCloudDBZoneWrapper;
    private static final String TAG = "GroupViewModel";
    List<Group> groupsList = new ArrayList<>();

    public GroupViewModel(@NonNull Application application) {
        super(application);
        agConnectAuth = AGConnectAuth.getInstance();
        mHandler = new Handler(Looper.getMainLooper());
        mCloudDBZoneWrapper = ((SplitBillApplication) application).getCloudDBZoneWrapper();
        groupRepository = new GroupRepository(mCloudDBZoneWrapper.getCloudDBZone(), mCloudDBZoneWrapper.getHandler());
    }

    public MutableLiveData<List<Group>> getGroupsLiveData() {
        groupsLiveData = groupRepository.getGroupList();
        return groupsLiveData;
    }

    public MutableLiveData<Boolean> upsertGroupData(Group group) {
        return groupRepository.upsertGroupData(group);
    }

    public MutableLiveData<Integer> getGroupId() {
        return groupRepository.getGroupIdLiveData() ;
    }
}