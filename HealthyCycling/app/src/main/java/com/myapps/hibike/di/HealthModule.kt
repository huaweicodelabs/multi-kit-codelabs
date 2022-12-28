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

import android.content.Context
import com.huawei.hms.hihealth.ActivityRecordsController
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.data.DataCollector
import com.huawei.hms.hihealth.data.DataType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HealthModule {

    @Provides
    @Singleton
    fun provideActivityRecordsController(
        @ApplicationContext context: Context
    ): ActivityRecordsController = HuaweiHiHealth.getActivityRecordsController(context)


    @Provides
    @Named("distance")
    @Singleton
    fun provideDataCollectorDistanceTotal(
        @ApplicationContext context: Context
    ): DataCollector = DataCollector.Builder().setDataType(DataType.DT_CONTINUOUS_DISTANCE_TOTAL)
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .setPackageName(context)
            .setDataCollectorName("distance")
            .build()

    @Provides
    @Named("speed")
    @Singleton
    fun provideDataCollectorSpeedTotal(
        @ApplicationContext context: Context
    ): DataCollector = DataCollector.Builder()
        .setDataType(DataType.POLYMERIZE_CONTINUOUS_SPEED_STATISTICS)
        .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
        .setPackageName(context)
        .setDataCollectorName("speed")
        .build()

    @Provides
    @Named("calorie")
    @Singleton
    fun provideDataCollectorCaloriesTotal(
        @ApplicationContext context: Context
    ): DataCollector = DataCollector.Builder()
        .setDataType(DataType.DT_CONTINUOUS_CALORIES_BURNT_TOTAL)
        .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
        .setPackageName(context)
        .setDataCollectorName("calories")
        .build()

    @Provides
    @Named("pedalingRate")
    @Singleton
    fun provideDataCollectorPedal(
        @ApplicationContext context: Context
    ): DataCollector = DataCollector.Builder()
        .setDataType(DataType.DT_INSTANTANEOUS_PEDALING_RATE)
        .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
        .setPackageName(context)
        .setDataCollectorName("AddActivityRecord")
        .build()
}