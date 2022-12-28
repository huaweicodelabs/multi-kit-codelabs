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

import com.huawei.hms.hihealth.ActivityRecordsController
import com.huawei.hms.hihealth.HiHealthActivities
import com.huawei.hms.hihealth.data.*
import com.huawei.hms.hihealth.options.ActivityRecordInsertOptions
import com.huawei.hms.hihealth.options.ActivityRecordReadOptions
import com.myapps.hibike.utils.IServiceListener
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class HealthRepository @Inject constructor(
    private val activityRecordsController: ActivityRecordsController,
    @Named("distance") private val dataCollectorDistanceTotal: DataCollector,
    @Named("speed") private val dataCollectorSpeedTotal: DataCollector,
    @Named("calorie") private val dataCollectorCaloriesTotal: DataCollector,
    @Named("pedalingRate") private val dataCollectorPedalRate: DataCollector,
    private val calendar: Calendar
) {

    companion object{
        const val ACTIVITY_NAME = "Ride"
        const val ACTIVITY_DESC = "This is ActivityRecord add test!"
        const val TIME_ZONE = "+0300"
        const val KEY_CALORIE = "calories_total(f)"
    }

    fun addActivityRecord(
        uniqueId: String,
        startTime: Long,
        endTime: Long,
        distance: Float,
        avgSpeed: Float,
        calorie: Float,
        serviceListener: IServiceListener<Boolean>
    ) {
        val activitySummary = ActivitySummary()

        val distanceTotalSamplePoint = SamplePoint.Builder(dataCollectorDistanceTotal).build()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
        distanceTotalSamplePoint.getFieldValue(Field.FIELD_DISTANCE).setFloatValue(distance)

        val speedTotalSamplePoint = SamplePoint.Builder(dataCollectorSpeedTotal).build()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
        speedTotalSamplePoint.getFieldValue(Field.FIELD_AVG).setFloatValue(avgSpeed)
        speedTotalSamplePoint.getFieldValue(Field.FIELD_MIN).setFloatValue(0f)
        speedTotalSamplePoint.getFieldValue(Field.FIELD_MAX).setFloatValue(100000.0f)

        val caloriesTotalSamplePoint = SamplePoint.Builder(dataCollectorCaloriesTotal).build()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
        caloriesTotalSamplePoint.getFieldValue(Field.FIELD_CALORIES_TOTAL).setFloatValue(calorie)

        activitySummary.dataSummary =
            listOf(distanceTotalSamplePoint, speedTotalSamplePoint, caloriesTotalSamplePoint)

        val activityRecord = ActivityRecord.Builder()
            .setName(ACTIVITY_NAME)
            .setDesc(ACTIVITY_DESC)
            .setId(uniqueId)
            .setActivityTypeId(HiHealthActivities.CYCLING)
            .setStartTime(startTime, TimeUnit.MILLISECONDS)
            .setEndTime(endTime, TimeUnit.MILLISECONDS)
            .setActivitySummary(activitySummary)
            .setTimeZone(TIME_ZONE)
            .build()

        val sampleSet = SampleSet.create(dataCollectorPedalRate)

        val samplePoint = sampleSet.createSamplePoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
        samplePoint.getFieldValue(Field.RPM).setFloatValue(12.0F)
        sampleSet.addSample(samplePoint)

        val insertRequest = ActivityRecordInsertOptions.Builder()
            .setActivityRecord(activityRecord)
            .addSampleSet(sampleSet)
            .build()

        val addTask = activityRecordsController.addActivityRecord(insertRequest)

        addTask.addOnSuccessListener {
            serviceListener.onSuccess(true)
        }.addOnFailureListener { e ->
            serviceListener.onError(e)
        }

    }

    fun getActivityRecordsForTotalCalorie(serviceListener: IServiceListener<Float>) {
        //For weekly data
        val startTime = calendar.timeInMillis
        val endTime = calendar.timeInMillis.plus(518400000)

        val readRequest = ActivityRecordReadOptions.Builder()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .readActivityRecordsFromAllApps()
            .read(DataType.DT_INSTANTANEOUS_PEDALING_RATE)
            .build()

        var totalCalorie = 0F
        val getTask = activityRecordsController.getActivityRecord(readRequest)
        getTask.addOnSuccessListener { activityRecordReply ->
            val activityRecordList = activityRecordReply.activityRecords
            for (activityRecord in activityRecordList) {
                val dataSummary = activityRecord.activitySummary.dataSummary
                for (samplePoint: SamplePoint in dataSummary) {
                    for (entry: Map.Entry<String, Value> in samplePoint.fieldValues.entries) {
                        if (entry.key == KEY_CALORIE) {
                            val calorie = entry.value.asFloatValue()
                            totalCalorie += calorie
                        }
                    }
                }
            }
            serviceListener.onSuccess(totalCalorie)
        }.addOnFailureListener { e ->
            serviceListener.onError(e)
        }
    }
}