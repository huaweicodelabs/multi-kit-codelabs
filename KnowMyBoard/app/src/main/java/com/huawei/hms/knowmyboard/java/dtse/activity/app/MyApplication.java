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

package com.huawei.hms.knowmyboard.dtse.activity.app;

import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.knowmyboard.dtse.activity.util.Constants;
import com.huawei.hms.maps.MapsInitializer;
import com.huawei.hms.mlsdk.common.MLApplication;

import android.app.Activity;
import android.app.Application;

public class MyApplication extends Application {
    public static Activity act;
    static HiAnalyticsInstance instance;
    @Override
    public void onCreate() {
        super.onCreate();
        MLApplication.initialize(this);
        // initialize Analytics Kit
        instance = HiAnalytics.getInstance(this);
        MLApplication.getInstance().setApiKey(Constants.API_KEY);
        MapsInitializer.setApiKey(Constants.API_KEY);
    }

    public static void setActivity(Activity activity) {
        act = activity;
    }
    public static Activity getActivity() {
        return act;
    }
}
