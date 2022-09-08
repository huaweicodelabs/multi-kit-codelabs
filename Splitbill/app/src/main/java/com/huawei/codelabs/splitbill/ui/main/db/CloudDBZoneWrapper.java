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

package com.huawei.codelabs.splitbill.ui.main.db;

import android.content.Context;
import android.util.Log;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.codelabs.splitbill.ui.main.helper.ObjectTypeInfoHelper;
import com.huawei.hmf.tasks.Task;

public class CloudDBZoneWrapper {

    private static final String TAG = "CloudDBZoneWrapper";
    private final AGConnectCloudDB mCloudDB;
    private CloudDBZone mCloudDBZone;
    private ListenerHandler mRegister;

    public static final String DB_NAME = "SplitBillSampleApp";


    public CloudDBZoneWrapper() {
        mCloudDB = AGConnectCloudDB.getInstance();
    }

    /**
     * Init AGConnectCloudDB in Application
     *
     * @param context application context
     */
    public static void initAGConnectCloudDB(Context context) {
        Log.e(TAG, "initAGConnectCloudDB:  CloudDBZone");
        AGConnectCloudDB.initialize(context);
    }

    /**
     * Get CloudDB task to open AGConnectCloudDB
     */
    public Task<CloudDBZone> openCloudDBZoneV2() {
        CloudDBZoneConfig mConfig = new CloudDBZoneConfig(DB_NAME,
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        return mCloudDB.openCloudDBZone2(mConfig, true);
    }

    public CloudDBZone getCloudDBZone() {
        return mCloudDBZone;
    }

    public void setCloudDBZone(CloudDBZone cloudDBZone) {
        mCloudDBZone = cloudDBZone;
    }

    public ListenerHandler getHandler() {
        return mRegister;
    }

    /**
     * Call AGConnectCloudDB.createObjectType to init schema
     */
    public void createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "createObjectType: CloudDBZone" + e.getMessage());
        }
    }

    /**
     * Call AGConnectCloudDB.closeCloudDBZone
     */
    public void closeCloudDBZone() {
        try {
            mRegister.remove();
            mCloudDB.closeCloudDBZone(mCloudDBZone);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "CloudDBZone: " + e.getMessage());
        }
    }

}
