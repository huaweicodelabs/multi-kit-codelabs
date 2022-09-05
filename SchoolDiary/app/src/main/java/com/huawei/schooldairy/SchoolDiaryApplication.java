/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
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
package com.huawei.schooldairy;

import android.app.Application;

import com.huawei.agconnect.AGCRoutePolicy;
import com.huawei.schooldairy.model.CloudDBZoneWrapper;

/**
 * Application class Initiate the Application and hold the application Context
 * @author: Huawei
 * @since: 25-05-2022
 */
public class SchoolDiaryApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CloudDBZoneWrapper.initAGConnectCloudDB(this);
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
    }
    private static AGCRoutePolicy regionRoutePolicy = AGCRoutePolicy.SINGAPORE; // Set default region policy

    public static AGCRoutePolicy getRegionRoutePolicy() {
        return regionRoutePolicy;
    }

    public static void setRegionRoutePolicy(AGCRoutePolicy regionRoutePolicy) {
        SchoolDiaryApplication.regionRoutePolicy = regionRoutePolicy;
    }
}
