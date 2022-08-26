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

package com.huawei.hms.urbanhomeservices.java;

import android.app.Application;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.hms.api.HuaweiMobileServicesUtil;
import com.huawei.hms.urbanhomeservices.java.clouddb.CloudDBZoneWrapper;
import com.huawei.hms.urbanhomeservices.java.utils.AppPreferences;

/**
 * Initialize Cloud DB object
 * Initialize SharedPrefs and AGConnect Crash
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class UrbanHomeServicesApplication extends Application {

    public void onCreate() {
        super.onCreate();
        AGConnectCloudDB.initialize(this);
        HuaweiMobileServicesUtil.setApplication(this);
        CloudDBZoneWrapper.initAGConnectCloudDB(this);
        AppPreferences.init(this);
    }

    public void onTerminate() {
        super.onTerminate();
    }

}
