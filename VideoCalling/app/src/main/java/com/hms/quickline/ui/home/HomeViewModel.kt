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

package com.hms.quickline.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hms.quickline.core.base.BaseViewModel
import com.hms.quickline.data.di.IoDispatcher
import com.hms.quickline.data.model.CallsSdp
import com.hms.quickline.data.model.Users
import com.hms.quickline.domain.repository.CloudDbWrapper
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.hms.aaid.HmsInstanceId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(@IoDispatcher val dispatcher: CoroutineDispatcher) : BaseViewModel() {
    private val TAG = "HomeViewModel"

    private val availableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    fun getAvailableLiveData(): LiveData<Boolean> = availableLiveData

    private val userPushTokenLiveData: MutableLiveData<String> = MutableLiveData()
    fun getUserPushTokenLiveData(): LiveData<String> = userPushTokenLiveData

    @DelicateCoroutinesApi
    private var coroutineScope: CoroutineScope = CoroutineScope(coroutineContext)

    @DelicateCoroutinesApi
    fun getPushToken(context: Context) {
        coroutineScope.launch {
            async(dispatcher) {
                val appId = "105993909"
                val tokenScope = "HCM"
                val token = HmsInstanceId.getInstance(context).getToken(appId, tokenScope)
                Log.i("PushNotificationTAG", "get token:$token")

                userPushTokenLiveData.postValue(token)
            }
        }
    }

    /**
     * Check room exists in CloudDatabase
     */
    fun checkMeetingId(meetingId: String, hasMeetingId: (Boolean) -> Unit) {

        CloudDbWrapper.checkMeetingId(meetingId, object : CloudDbWrapper.ResultListener {
            override fun onSuccess(result: Any?) {
                val resultList: ArrayList<CallsSdp>? = result as? ArrayList<CallsSdp>

                resultList?.forEach {
                    if (it.meetingID == meetingId) hasMeetingId(true) else hasMeetingId(false)
                }
            }

            override fun onFailure(e: Exception) {
                e.localizedMessage?.let {
                    if (it == "noElements")
                        hasMeetingId(false)
                    else
                        Log.e(TAG,"Error MeetingIdCheck")
                }
            }
        })
    }

    fun checkAvailable(id : String) {
        CloudDbWrapper.getUserById(id, object : CloudDbWrapper.ICloudDbWrapper {
            override fun onUserObtained(users: Users) {
                availableLiveData.value = users.isAvailable
            }
        })
    }

    fun updateAvailable(id : String, isAvailable: Boolean,cloudDBZone : CloudDBZone) {
        CloudDbWrapper.getUserById(id, object : CloudDbWrapper.ICloudDbWrapper {
            override fun onUserObtained(users: Users) {

                users.isAvailable = isAvailable

                cloudDBZone.executeUpsert(users)?.addOnSuccessListener { cloudDBZoneResult ->
                    Log.i("HomeFragmentBusy", "Available data success: $cloudDBZoneResult")
                }?.addOnFailureListener {
                    Log.e("HomeFragmentBusy", "Available data failed: ${it.message}")
                }
            }
        })
    }
}