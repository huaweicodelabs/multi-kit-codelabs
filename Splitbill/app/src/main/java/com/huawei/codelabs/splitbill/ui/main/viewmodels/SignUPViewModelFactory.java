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

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.codelabs.splitbill.ui.main.activities.AuthActivity;
import com.huawei.codelabs.splitbill.ui.main.activities.SignUpActivity;

public class SignUPViewModelFactory implements ViewModelProvider.Factory{
    private final Application mApplication;
    SignUpActivity authActivity;



    public SignUPViewModelFactory(Application application, SignUpActivity authActivity) {
        mApplication = application;
        this.authActivity=authActivity;
        Log.d ("data:", authActivity+" mmm" + mApplication );
    }


    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new SignUPViewModel (mApplication, authActivity );
}}