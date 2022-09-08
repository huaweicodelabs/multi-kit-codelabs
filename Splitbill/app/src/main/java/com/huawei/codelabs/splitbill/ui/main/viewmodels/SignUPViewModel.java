/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
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
import androidx.lifecycle.LiveData;

import com.huawei.agconnect.auth.SignInResult;
import com.huawei.codelabs.splitbill.ui.main.activities.SignUpActivity;
import com.huawei.codelabs.splitbill.ui.main.repo.SignUPRepository;
public class SignUPViewModel extends BaseViewModel {
    public SignUPRepository authRepository;

    public LiveData<SignInResult> authenticatedUserLiveData;

    public SignUPViewModel(@NonNull Application application) {
        super(application);

    }
    public SignUPViewModel(@NonNull Application application, SignUpActivity authActivity) {
        super(application);
        authRepository = new SignUPRepository(authActivity);
    }


    public LiveData <SignInResult>  loginPhone(String phonenumber , String verifycode , String countrycode) {
        Log.d("Data:", "create user onSuccess: call");
        authenticatedUserLiveData = authRepository.loginPhone(phonenumber,verifycode,countrycode);
        return  authenticatedUserLiveData;
    }

}