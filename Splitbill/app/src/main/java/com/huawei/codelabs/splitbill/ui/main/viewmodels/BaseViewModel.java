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

package com.huawei.codelabs.splitbill.ui.main.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.huawei.codelabs.splitbill.ui.SplitBillApplication;
import com.huawei.codelabs.splitbill.ui.main.db.CloudDBZoneWrapper;
import com.huawei.codelabs.splitbill.ui.main.models.User;
import com.huawei.codelabs.splitbill.ui.main.repo.BaseRepository;
import com.huawei.codelabs.splitbill.ui.main.repo.UserRepository;

import java.util.List;


public class BaseViewModel extends AndroidViewModel {

    public BaseRepository baseRepository;

    public LiveData<Boolean> openCloudDBLiveData;
    public MutableLiveData<Boolean> isRegisteredUser;
    public MutableLiveData<List<User>> usersLiveData;
    private CloudDBZoneWrapper mCloudDBZoneWrapper;
    public UserRepository userRepository;

    public BaseViewModel(@NonNull Application application) {
        super(application);
        baseRepository = new BaseRepository(application.getApplicationContext());
    }

    public void initAndCheckCloudDBStatus(CloudDBZoneWrapper cloudDBZoneWrapper) {
        openCloudDBLiveData = new MutableLiveData<>();
        openCloudDBLiveData = baseRepository.initAndCheckCloudDBStatus(cloudDBZoneWrapper);
    }

    public MutableLiveData<Boolean> checkIfUserExist(String phoneNumber) {
        initUserRepository();
        isRegisteredUser = userRepository.checkIfUserExist(phoneNumber);
        return isRegisteredUser;
    }

    public MutableLiveData<List<User>> getUserLiveData() {
        initUserRepository();
        usersLiveData = userRepository.getUserList();
        return usersLiveData;
    }

    public MutableLiveData<Boolean> upsertUserData(User user) {
        initUserRepository();
        return userRepository.upsertUserData(user);
    }

    public void initUserRepository(){
        if(userRepository == null) {
            mCloudDBZoneWrapper = ((SplitBillApplication) getApplication()).getCloudDBZoneWrapper();
            userRepository = new UserRepository(mCloudDBZoneWrapper.getCloudDBZone(), mCloudDBZoneWrapper.getHandler());
        }
    }


    public MutableLiveData<Integer> getUserId() {
        return userRepository.getUserIdLiveData() ;
    }
}
