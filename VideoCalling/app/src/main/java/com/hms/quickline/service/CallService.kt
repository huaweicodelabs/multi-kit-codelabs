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

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.hms.quickline.core.util.Constants
import com.hms.quickline.data.model.Users

import com.hms.quickline.domain.repository.CloudDbWrapper
import com.huawei.agconnect.cloud.database.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import kotlinx.coroutines.ExperimentalCoroutinesApi


/**
 * 功能描述
 *
 * @author b00557735
 * @since 2022-05-12
 */

@ExperimentalCoroutinesApi
class CallService: Service() {

    private var cloudDBZone: CloudDBZone? = CloudDbWrapper.cloudDBZone
    private var registerUser: ListenerHandler? = null
    private var currentUID :String? = ""
    private val TAG = "CallService"
    private var isNotificationShown = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentUID = intent?.getStringExtra(Constants.UID)!!
        addUserSubscription()
        return super.onStartCommand(intent, flags, startId)
    }

    private val userSnapshotListener = OnSnapshotListener<Users> { cloudDBZoneSnapshot, e ->

        e?.let {
            Log.w(TAG, "onSnapshot: " + e.message)
            return@OnSnapshotListener
        }

        val snapshot = cloudDBZoneSnapshot.snapshotObjects
        var users = Users()

        try {
            while (snapshot.hasNext()) {
                users = snapshot.next()
            }
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "Snapshot Error: " + e.message)
        } finally {
            Log.i(TAG,"isCalling:${users.isCalling}")
            if (users.isCalling == true && !isNotificationShown){
                isNotificationShown = true
                showCallNotification(users.uid, callerName = users.callerName)
            }
        }
    }

    private fun showCallNotification(uid: String,callerName: String) {
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java).putExtra(Constants.UID,uid)
            .putExtra(Constants.CALLER_NAME,callerName)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)
    }

    private fun addUserSubscription() {

        try {
            currentUID?.let {
                val snapshotQuery =
                    CloudDBZoneQuery.where(Users::class.java).equalTo(Constants.UID, it)
                registerUser = cloudDBZone?.subscribeSnapshot(
                    snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                    userSnapshotListener
                )
            }
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "subscribeSnapshot: " + e.message)
        }
    }
}