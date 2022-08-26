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
@file:Suppress("NewLineAtEndOfFile","WildcardImport")
package com.huawei.hms.urbanhomeservices.kotlin.fragments.home

import android.app.Application
import android.location.Location
import androidx.lifecycle.LiveData
import com.huawei.hms.location.*
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants

/**
 * Fetch Location from Huawei Location Kit
 *
 * @author: Huawei
 * @since 20-01-21
 */

class LocationLiveData(application: Application?) : LiveData<LocationModel?>() {
    var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)
    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onActive() {
        super.onActive()
        startLocationUpdates()
    }

    /**
     * To start location updates
     */
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest(), locationCallback, null)
    }

    /**
     * For fetching location from HMS Location Kit callback method
     */
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locations = locationResult.getLocations()
            if (locations.isNotEmpty()) {
                for (location in locations) {
                    setLocationData(location)
                }
            }
        }
    }

    /**
     * To set location data
     */
    private fun setLocationData(location: Location) {
        val locationModel = LocationModel(location.longitude, location.latitude)
        locationModel.latitud = location.latitude
        locationModel.longitude = location.longitude
        value = LocationModel(location.longitude, location.latitude)
    }

    /**
     * To request location data
     */
    private fun locationRequest(): LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.setInterval(AppConstants.LOCATION_REQ_INTERVAL)
        locationRequest.setFastestInterval(AppConstants.LOCATION_FASTEST_INTERVAL)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        return locationRequest
    }
}

/**
 * Location model class
 */
class LocationModel(var longitude: Double, var latitud: Double)