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

package com.myapps.hibike.data.repository

import android.app.PendingIntent
import android.util.Log
import com.huawei.agconnect.appmessaging.AGConnectAppMessaging
import com.huawei.hms.location.Geofence
import com.huawei.hms.location.GeofenceRequest
import com.huawei.hms.location.GeofenceService
import javax.inject.Inject

class GeofenceRepository @Inject constructor(
    private val geofenceService: GeofenceService,
    private val pendingIntent: PendingIntent
){

    private var geofenceList: ArrayList<Geofence> = arrayListOf()

    companion object{
        const val TAG = "GeofenceRepository"
        const val CUSTOM_EVENT = "GeofenceSuccess"
    }

    fun createGeofence(){
        val specificLocations = arrayListOf(
            mapOf("first" to arrayOf(40.003018, 32.803493)),
            mapOf("second" to arrayOf(40.002607, 32.806496)),
            mapOf("third" to arrayOf(40.001827383726116, 32.80392988071175)),
        )

        specificLocations.forEach { map ->
            val location = map.values.first()
            geofenceList.add(
                Geofence.Builder()
                    .setUniqueId(map.keys.first())
                    .setValidContinueTime(10000)
                    .setRoundArea(location[0], location[1], 5000f)
                    .setConversions(Geofence.ENTER_GEOFENCE_CONVERSION or Geofence.EXIT_GEOFENCE_CONVERSION)
                    .build()
            )
        }

        geofenceService.createGeofenceList(getAddGeofenceRequest(), pendingIntent)
            ?.addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    AGConnectAppMessaging.getInstance().apply {
                        isFetchMessageEnable = true
                        isDisplayEnable = true
                        trigger(CUSTOM_EVENT)
                    }
                } else {
                    Log.e(TAG, "${task.result}")
                }
            }
    }

    private fun getAddGeofenceRequest(): GeofenceRequest? {
        val builder = GeofenceRequest.Builder()
        builder.setInitConversions(GeofenceRequest.ENTER_INIT_CONVERSION)
        builder.createGeofenceList(geofenceList)
        return builder.build()
    }

}