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

import androidx.lifecycle.MutableLiveData;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.codelabs.splitbill.ui.main.models.LoginUser;

public class SplashRepository {
    protected AGConnectAuth agConnectAuth;
        private final LoginUser loginUser = new LoginUser();
        public MutableLiveData <LoginUser> checkIfUserIsAuthenticatedInHms() {
            agConnectAuth = AGConnectAuth.getInstance();

            MutableLiveData<LoginUser> isUserAuthenticateInFirebaseMutableLiveData = new MutableLiveData<>();
            AGConnectUser currentUser = agConnectAuth.getCurrentUser();
            if (currentUser == null) {

                loginUser.isAuthenticated = false;
                isUserAuthenticateInFirebaseMutableLiveData.setValue(loginUser);
            } else {
                loginUser.userID = currentUser.getUid();
                loginUser.isAuthenticated = true;
                isUserAuthenticateInFirebaseMutableLiveData.setValue(loginUser);
            }
            return isUserAuthenticateInFirebaseMutableLiveData;
    }

        public MutableLiveData<LoginUser> addUserToLiveData(String uid) {
            MutableLiveData<LoginUser> userMutableLiveData = new MutableLiveData<>();
            AGConnectUser currentUser = AGConnectAuth.getInstance().getCurrentUser() ;
            LoginUser loginUser =new LoginUser(currentUser.getUid()+"",currentUser.getDisplayName()+"",currentUser.getEmail()+"");

            userMutableLiveData.setValue(loginUser);

        return userMutableLiveData;
    }
}