/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myapps.hibike.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.huawei.hms.location.GeofenceData

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val action = intent.action
        if (ACTION_PROCESS_LOCATION == action) {
            val geofenceData = GeofenceData.getDataFromIntent(intent)
            if (geofenceData.isSuccess) {
                Log.e(TAG, "Success")
            }
            else Log.e(TAG, geofenceData.errorCode.toString())
        }
    }

    companion object {
        const val TAG = "GeoBroadcastReceiver"
        const val ACTION_PROCESS_LOCATION = "com.huawei.hmssample.geofence.GeoFenceBroadcastReceiver.ACTION_PROCESS_LOCATION"
    }
}