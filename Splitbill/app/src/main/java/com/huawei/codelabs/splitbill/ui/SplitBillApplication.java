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

package com.huawei.codelabs.splitbill.ui;

import android.app.Application;
import android.os.StrictMode;

import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement;
import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.codelabs.splitbill.ui.main.db.CloudDBZoneWrapper;


public class SplitBillApplication extends Application {

    private CloudDBZoneWrapper mCloudDBZoneWrapper;
    private static AGCStorageManagement storageManagement;

    @Override
    public void onCreate() {
        CloudDBZoneWrapper.initAGConnectCloudDB(this);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        super.onCreate();
        storageManagement = AGCStorageManagement.getInstance("splitbill-e76yq");

    }

    /**
     * Get instance of Cloud DB zone wrapper to initiate cloud DB
     *
     * @return mCloudDBZoneWrapper
     */
    public CloudDBZoneWrapper getCloudDBZoneWrapper() {
        if (mCloudDBZoneWrapper != null) {
            return mCloudDBZoneWrapper;
        }
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        return mCloudDBZoneWrapper;
    }


    @Override
    public void onTerminate() {
        mCloudDBZoneWrapper.closeCloudDBZone();
        super.onTerminate();
    }

    public static AGCStorageManagement getStorageManagement() {
        return storageManagement;
    }

    public static StorageReference getDefaultStorageRef() {
        return getStorageManagement().getStorageReference("splitbill/");
    }

    public static StorageReference getProfilePicStorageRef() {
        return getDefaultStorageRef().child("profile_pic/");
    }
}
