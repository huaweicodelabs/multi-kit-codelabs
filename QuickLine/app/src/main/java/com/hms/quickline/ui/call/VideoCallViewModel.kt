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

package com.hms.quickline.ui.call

import android.util.Log
import com.hms.quickline.core.base.BaseViewModel
import com.hms.quickline.data.model.Users
import com.hms.quickline.domain.repository.CloudDbWrapper
import com.huawei.agconnect.cloud.database.CloudDBZone
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VideoCallViewModel @Inject constructor() : BaseViewModel() {

    private val TAG = "VideoCallViewModel"

    fun getUserCalling(meetingID: String, cloudDBZone: CloudDBZone) {

        CloudDbWrapper.getUserById(meetingID, object : CloudDbWrapper.ICloudDbWrapper {
            override fun onUserObtained(users: Users) {
                users.isCalling = false

                cloudDBZone.executeUpsert(users)?.addOnSuccessListener { cloudDBZoneResult ->
                    Log.i(TAG, "User Calling Info Upsert success: $cloudDBZoneResult")
                }?.addOnFailureListener {
                    Log.e(TAG, "User Calling Info Upsert failed: ${it.message}")
                }
            }
        })
    }

    fun getUserAvailable(userId: String, isAvailable: Boolean, cloudDBZone: CloudDBZone) {

        CloudDbWrapper.getUserById(userId, object : CloudDbWrapper.ICloudDbWrapper {
            override fun onUserObtained(users: Users) {
                users.isAvailable = isAvailable

                cloudDBZone.executeUpsert(users)?.addOnSuccessListener { cloudDBZoneResult ->
                    Log.i(TAG, "User Calling Info Upsert success: $cloudDBZoneResult")
                }?.addOnFailureListener {
                    Log.e(TAG, "User Calling Info Upsert failed: ${it.message}")
                }
            }
        })
    }

}