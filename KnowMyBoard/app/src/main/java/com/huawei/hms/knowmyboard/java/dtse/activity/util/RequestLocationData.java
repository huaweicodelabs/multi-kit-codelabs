/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
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

package com.huawei.hms.knowmyboard.dtse.activity.util;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hms.knowmyboard.dtse.activity.viewmodel.LoginViewModel;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsStates;
import com.huawei.hms.location.SettingsClient;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class RequestLocationData {
    static String TAG = "TAG";
    SettingsClient settingsClient;
    private int isLocationSettingSuccess = 0;
    private LocationRequest myLocationRequest;

    // Define a fusedLocationProviderClient object.
    private FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback myLocationCallback;
    Context context;
    Activity activity;
    LocationResult locationResult;
    LoginViewModel loginViewModel;

    public RequestLocationData(Context context, FragmentActivity activity, LoginViewModel loginViewModel) {
        setContext(context);
        setActivity(activity);
        setLoginViewModel(loginViewModel);
    }

    public LoginViewModel getLoginViewModel() {
        return loginViewModel;
    }

    public void setLoginViewModel(LoginViewModel loginViewModel) {
        this.loginViewModel = loginViewModel;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void initFusionLocationProviderClint() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        settingsClient = LocationServices.getSettingsClient(getActivity());
    }

    public void checkDeviceLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        myLocationRequest = new LocationRequest();
        builder.addLocationRequest(myLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        // Check the device location settings.
        settingsClient.checkLocationSettings(locationSettingsRequest)
                // Define the listener for success in calling the API for checking device location settings.
                .addOnSuccessListener(locationSettingsResponse -> {
                    LocationSettingsStates locationSettingsStates =
                            locationSettingsResponse.getLocationSettingsStates();
                    StringBuilder stringBuilder = new StringBuilder();
                    // Check whether the location function is enabled.
                    stringBuilder.append(",isLocationUsable=")
                            .append(locationSettingsStates.isLocationUsable());
                    // Check whether HMS Core (APK) is available.
                    stringBuilder.append(",isHMSLocationUsable=")
                            .append(locationSettingsStates.isHMSLocationUsable());
                    // Set the location type.
                    myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    // Set the number of location updates to 1.
                    myLocationRequest.setNumUpdates(1);
                    isLocationSettingSuccess = 1;


                })
                // Define callback for failure in checking the device location settings.
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.i(TAG, "checkLocationSetting onFailure:" + e.getMessage());
                    }
                });

    }

    public void checkPermission() {
        // Dynamically apply for required permissions if the API level is 28 or lower.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(TAG, "android sdk <= 28 Q");
            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] strings =
                        {Manifest.permission.CAMERA, Manifest.permission.MANAGE_MEDIA, Manifest.permission.MEDIA_CONTENT_CONTROL, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(getActivity(), strings, 1);
            }
        } else {
            // Dynamically apply for the android.permission.ACCESS_BACKGROUND_LOCATION permission in addition to the preceding permissions if the API level is higher than 28.
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(),
                    "android.permission.ACCESS_BACKGROUND_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                String[] strings = {Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.MEDIA_CONTENT_CONTROL, Manifest.permission.MANAGE_MEDIA,
                        "android.permission.ACCESS_BACKGROUND_LOCATION"};
                ActivityCompat.requestPermissions(getActivity(), strings, 2);
            }
        }
    }

    public LocationResult refreshLocation() {
        Log.d(TAG, "Refreshing location");

        if (isLocationSettingSuccess == 1) {
            myLocationCallback = new LocationCallback() {
                private LocationResult locationResult;

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        this.locationResult = locationResult;
                        getLoginViewModel().setLocationResult(locationResult);
                    }
                }
            };
            fusedLocationProviderClient.requestLocationUpdates(myLocationRequest, myLocationCallback, Looper.getMainLooper());

        } else {
            Log.d(TAG, "Failed to get location settings");
        }
        return locationResult;
    }

    public void disableLocationData() {
        fusedLocationProviderClient.disableBackgroundLocation();
        fusedLocationProviderClient.removeLocationUpdates(myLocationCallback);
    }

}
