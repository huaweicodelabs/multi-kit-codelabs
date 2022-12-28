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

import com.huawei.hms.location.*
import com.huawei.hms.maps.model.LatLng
import com.myapps.hibike.utils.IServiceListener
import javax.inject.Inject

class LastLocationRepository @Inject constructor(
    private val locationRequest: LocationRequest,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val settingsClient: SettingsClient
) {

    fun getLastLocation(serviceListener: IServiceListener<LatLng>) {

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location ->
                        serviceListener.onSuccess(LatLng(location.latitude, location.longitude))
                    }
                    .addOnFailureListener { e ->
                        serviceListener.onError(e)
                    }
            }
            .addOnFailureListener { e ->
                serviceListener.onError(e)
            }
    }

}