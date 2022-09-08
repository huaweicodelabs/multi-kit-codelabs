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

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.huawei.codelabs.splitbill.ui.main.models.LoginUser;
import com.huawei.codelabs.splitbill.ui.main.repo.SplashRepository;

public class SplashViewModel extends BaseViewModel {
    private final SplashRepository splashRepository;
    public LiveData <LoginUser> isUserAuthenticatedLiveData;
    public LiveData<LoginUser> userLiveData;

    public SplashViewModel(Application application) {
        super(application);
        splashRepository = new SplashRepository ();
    }

    public void checkIfUserIsAuthenticated() {
        isUserAuthenticatedLiveData = splashRepository.checkIfUserIsAuthenticatedInHms();
    }

    public void setUid(String uid) {
        userLiveData = splashRepository.addUserToLiveData(uid);
    }
}