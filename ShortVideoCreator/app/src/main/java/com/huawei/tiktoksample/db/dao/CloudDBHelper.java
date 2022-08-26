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

package com.huawei.tiktoksample.db.dao;

import android.content.Context;
import android.util.Log;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;
import com.huawei.tiktoksample.db.clouddb.ObjectTypeInfoHelper;
import com.huawei.tiktoksample.util.Constants;
import com.huawei.tiktoksample.util.OnDBZoneOpen;

public class CloudDBHelper {

    private AGConnectCloudDB agConnectCloudDB;
    private CloudDBZone cloudDBZone;
    private static final String TAG = CloudDBHelper.class.getSimpleName();

    private static CloudDBHelper cloudDBHelper;

    public static CloudDBHelper getInstance() {
        if (cloudDBHelper == null) {
            cloudDBHelper = new CloudDBHelper();
        }
        return cloudDBHelper;
    }

    public void init(Context context) {
        AGConnectCloudDB.initialize(context);
        try {
            agConnectCloudDB = AGConnectCloudDB.getInstance();
            agConnectCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException e) {
            e.printStackTrace();
        }
    }

    public void openDb(OnDBZoneOpen onDBZoneOpen) {
        CloudDBZoneConfig mConfig = new CloudDBZoneConfig(Constants.DB_ZONE_NAME,
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = agConnectCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(cloudDBZone -> {
            Log.i(TAG, "open cloudDBZone success");
            this.cloudDBZone = cloudDBZone;
            onDBZoneOpen.isDBZoneOpen(true, this.cloudDBZone);
        }).addOnFailureListener(e -> {
            Log.w(TAG, "open cloudDBZone failed for " + e.getMessage());
            onDBZoneOpen.isDBZoneOpen(false, null);
        });
    }

    public void closeDb(Context context) {
        try {
            agConnectCloudDB.closeCloudDBZone(cloudDBZone);
        } catch (AGConnectCloudDBException e) {
            e.printStackTrace();
        }
    }
}
