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

package com.hms.quickline.service

/**
 * 功能描述
 *
 * @author b00557735
 * @since 2022-05-12
 */
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hms.quickline.R
import com.hms.quickline.core.util.Constants
import com.hms.quickline.core.util.Constants.ANSWER
import com.hms.quickline.core.util.Constants.CALLER_NAME
import com.hms.quickline.core.util.Constants.DECLINE
import com.hms.quickline.core.util.Constants.MEETING_ID
import com.hms.quickline.core.util.Constants.IS_JOIN
import com.hms.quickline.core.util.Constants.UID
import com.hms.quickline.data.model.CallsSdp
import com.hms.quickline.data.model.Users
import com.hms.quickline.ui.call.VideoCallActivity
import com.hms.quickline.domain.repository.CloudDbWrapper
import com.huawei.agconnect.cloud.database.CloudDBZone
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ActionReceiver : BroadcastReceiver() {

    private var cloudDBZone: CloudDBZone? = CloudDbWrapper.cloudDBZone
    private val TAG = "ActionReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val callerName = intent.getStringExtra(CALLER_NAME) ?: context.resources.getString(R.string.unknown)
        val notificationUtils = NotificationUtils(context = context, callerName = callerName)
        val uid = intent.getStringExtra(UID)

        when (intent.action) {
            ANSWER -> {
                val videoIntent = Intent(context, VideoCallActivity::class.java)
                videoIntent.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    Log.i(TAG, uid.toString())
                    putExtra(MEETING_ID, uid)
                    putExtra(IS_JOIN, true)
                }
                context.startActivity(videoIntent)
                notificationUtils.getManager().cancel(150)
            }
            DECLINE -> {
                notificationUtils.getManager().cancel(150)

                val callsSdp = CallsSdp()
                callsSdp.meetingID = uid
                callsSdp.callType = Constants.TYPE.END.name
                val upsertSdpTask = cloudDBZone?.executeUpsert(callsSdp)

                upsertSdpTask?.addOnSuccessListener { cloudDBZoneResult ->
                    Log.i(TAG, "Calls Sdp Upsert success: $cloudDBZoneResult")
                }?.addOnFailureListener {
                    Log.e(TAG, "Calls Sdp Upsert failed: ${it.message}")
                }

                uid?.let { id ->
                    CloudDbWrapper.getUserById(id, object : CloudDbWrapper.ICloudDbWrapper {
                        override fun onUserObtained(users: Users) {
                            users.isCalling = false

                            val upsertUserTask = users.let { cloudDBZone?.executeUpsert(it) }
                            upsertUserTask?.addOnSuccessListener { cloudDBZoneResult ->
                                Log.i(TAG, "Calls UserCalling Upsert success: $cloudDBZoneResult")
                            }?.addOnFailureListener { exp ->
                                Log.e(TAG, "Calls UserCalling Upsert failed: ${exp.message}")
                            }
                        }
                    })
                }
            }
        }
    }
}