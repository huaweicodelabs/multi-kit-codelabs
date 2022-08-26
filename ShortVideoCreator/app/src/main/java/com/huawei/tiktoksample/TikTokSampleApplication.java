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

package com.huawei.tiktoksample;

import android.app.Application;

import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement;
import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.videokit.player.WisePlayerFactory;
import com.huawei.tiktoksample.db.dao.CloudDBHelper;


public class TikTokSampleApplication extends Application {
    private static TikTokSampleApplication mInstance;
    private static WisePlayerFactory wisePlayerFactory = null;
    private static AGCStorageManagement storageManagement;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        CloudDBHelper.getInstance().init(getApplicationContext());
        storageManagement = AGCStorageManagement.getInstance();
        HwAds.init(this);

    }
    public static TikTokSampleApplication getInstance() {
        return mInstance;
    }
    public static AGCStorageManagement getStorageManagement() {
        return storageManagement;
    }
    public static StorageReference getDefaultStorageRef() {
        return getStorageManagement().getStorageReference("tiktoksample/");
    }
}
