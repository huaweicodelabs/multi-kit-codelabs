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

package com.huawei.hms.urbanhomeservices.java.fragments.home;

import android.app.Application;
import android.location.Location;

import androidx.lifecycle.LiveData;

import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;

import java.util.List;

/**
 * Fetch Location from Huawei Location Kit
 *
 * @author: Huawei
 * @since 20-01-21
 */

public class LocationLiveData extends LiveData<com.huawei.hms.urbanhomeservices.kotlin.fragments.home.LocationModel> {

    public FusedLocationProviderClient fusedLocationClient;

    public LocationLiveData(Application application) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onActive() {
        super.onActive();
        startLocationUpdates();
    }

    /**
     * To start location updates
     */

    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest(), locationCallback, null);
    }

    /**
     * For fetching location from HMS Location Kit callback method
     */

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null) {
                List<Location> locations = locationResult.getLocations();
                if (!locations.isEmpty()) {
                    for (Location location : locations) {
                        setLocationData(location);
                    }
                }
            }
        }
    };


    /**
     * To set location data
     *
     * @param location location data
     */

    private void setLocationData(Location location) {
        com.huawei.hms.urbanhomeservices.kotlin.fragments.home.LocationModel locationModel = new com.huawei.hms.urbanhomeservices.kotlin.fragments.home.LocationModel(location.getLongitude(), location.getLatitude());
        locationModel.setLatitud(location.getLatitude());
        locationModel.setLongitude(location.getLongitude());
        setValue(new com.huawei.hms.urbanhomeservices.kotlin.fragments.home.LocationModel(location.getLongitude(), location.getLatitude()));
    }

    /**
     * To request location data
     *
     * @return locationRequest returns location
     */

    private LocationRequest locationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(AppConstants.LOCATION_REQ_INTERVAL);
        locationRequest.setFastestInterval(AppConstants.LOCATION_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
}

/**
 * Location model class
 */

class LocationModel {

    double longitude;
    double latitud;

    public LocationModel(double longitude, double latitude) {
        this.latitud = latitude;
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }
}

