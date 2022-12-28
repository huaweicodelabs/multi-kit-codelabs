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

package com.myapps.hibike.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.huawei.hms.location.*
import com.myapps.hibike.service.GeofenceBroadcastReceiver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocationModule {

    @Provides
    @Singleton
    fun provideLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.apply {
            interval = 10000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return locationRequest
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return FusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideSettingsClient(
        @ApplicationContext context: Context
    ): SettingsClient {
        return LocationServices.getSettingsClient(context)
    }

    @Provides
    @Singleton
    fun provideGeofenceService(
        @ApplicationContext context: Context
    ): GeofenceService {
        return LocationServices.getGeofenceService(context)
    }

    @Provides
    @Singleton
    fun providePendingIntent(
        @ApplicationContext context: Context
    ): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = GeofenceBroadcastReceiver.ACTION_PROCESS_LOCATION
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }
    }
}