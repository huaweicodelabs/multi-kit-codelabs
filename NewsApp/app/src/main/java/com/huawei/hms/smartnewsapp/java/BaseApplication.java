package com.huawei.hms.smartnewsapp.java;

/*
 *
 *  * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

import android.util.Log;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.network.NetworkKit;
import com.huawei.hms.searchkit.SearchKitInstance;
import com.huawei.hms.smartnewsapp.R;
import com.huawei.hms.smartnewsapp.java.di.components.DaggerAppComponent;
import com.huawei.hms.smartnewsapp.java.util.Constants;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;


public class BaseApplication extends DaggerApplication {
    private static final String API_KEY = "client/api_key";
    private static final String TAG = "BaseApplication";
    @Override
    /**
     *  Binding an application instance to application component
     *
     */
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().application(this).build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initNetworkKIT();
        initSearchKit();
        initMLkit();


    }

    /**
     *  Initialise ML Kit
     *
     */
    private void initMLkit() {
        AGConnectServicesConfig config = AGConnectServicesConfig.fromContext(this);
        MLApplication.getInstance().setApiKey(config.getString(API_KEY));
    }

    /**
     * Asynchronously load the NetworkKit object.
     */

    public void initNetworkKIT() {
        NetworkKit.init(
                getApplicationContext(),
                new NetworkKit.Callback() {
                    @Override
                    public void onResult(boolean status) {
                        if (status) {
                            Log.d(TAG, getApplicationContext().getResources().getString(R.string.Network_sucess));
                        } else {
                            Log.d(TAG, getApplicationContext().getResources().getString(R.string.Network_failed));
                        }
                    }
                });
    }

    /**
     * Initialize the SearchKit instance.
     */
    private void initSearchKit() {
        SearchKitInstance.init(this, Constants.CLIENT_ID);
    }
}
