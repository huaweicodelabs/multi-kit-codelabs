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

package com.hms.quickline.data.webrtc

import android.util.Log
import com.hms.quickline.data.webrtc.listener.SignalingListener
import com.hms.quickline.core.util.Constants
import com.hms.quickline.core.util.Constants.MEETING_ID
import com.hms.quickline.data.model.CallsCandidates
import com.hms.quickline.data.model.CallsSdp
import com.hms.quickline.domain.repository.CloudDbWrapper
import com.huawei.agconnect.cloud.database.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import kotlinx.coroutines.*
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.lang.NullPointerException
import java.util.*
import kotlin.coroutines.CoroutineContext

class SignalingClient(
    private val meetingID: String,
    private val signalingListener: SignalingListener
) : CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.IO + job

    private var sdpType: Constants.TYPE? = null

    private var cloudDB: AGConnectCloudDB? = CloudDbWrapper.cloudDB
    private var cloudDBZone: CloudDBZone? = CloudDbWrapper.cloudDBZone

    private var mRegisterSdp: ListenerHandler? = null
    private var mRegisterCandidate: ListenerHandler? = null

    init {
        connectionEstablishedTask()
        listenOfferAndAnswer()
    }

    private fun connectionEstablishedTask() = launch {

        cloudDB?.enableNetwork(Constants.CloudDbZoneName).also {
            signalingListener.onConnectionEstablished()
        }
    }

    private fun listenOfferAndAnswer() = launch {
        try {
            addCallsSdpSubscription()
            addCallsCandidatesSubscription()
        } catch (e: Exception) {
            Log.d(TAG, "checkingIsThereAnyOfferOrAnswer: error : ${e.localizedMessage}")
        }
    }

    private val mCallsSdpSnapshotListener = OnSnapshotListener<CallsSdp> { cloudDBZoneSnapshot, e ->

        e?.let {
            Log.w(TAG, "onSnapshot: " + e.message)
            return@OnSnapshotListener
        }

        val snapshot = cloudDBZoneSnapshot.snapshotObjects
        var callSdpTemp = CallsSdp()
        try {
            while (snapshot.hasNext()) {
                callSdpTemp = snapshot.next()
            }
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "Snapshot Error: " + e.message)
        } finally {

            try {
                when (callSdpTemp.callType.toString()) {
                    Constants.TYPE.OFFER.name -> {
                        signalingListener.onOfferReceived(
                            SessionDescription(
                                SessionDescription.Type.OFFER,
                                callSdpTemp.sdp.toString()
                            )
                        )
                        sdpType = Constants.TYPE.OFFER
                    }

                    Constants.TYPE.ANSWER.name -> {
                        signalingListener.onAnswerReceived(
                            SessionDescription(
                                SessionDescription.Type.ANSWER,
                                callSdpTemp.sdp.toString()
                            )
                        )
                        sdpType = Constants.TYPE.ANSWER
                    }
                    Constants.TYPE.END.name -> {
                        signalingListener.onCallEnded()
                        sdpType = Constants.TYPE.END
                    }
                }

                Log.d(TAG, "Current data: $callSdpTemp")
            }   catch (e: NullPointerException) {
                Log.w(TAG, "callSdpTemp.callType Error: " + e.message)
            }
        }
    }

    private val mCallsCandidatesSnapshotListener =
        OnSnapshotListener<CallsCandidates> { cloudDBZoneSnapshot, e ->

            e?.let {
                Log.w(TAG, "onSnapshot: " + e.message)
                return@OnSnapshotListener
            }

            val snapshot = cloudDBZoneSnapshot.snapshotObjects
            val callsCandidatesTemp = arrayListOf<CallsCandidates>()
            try {
                while (snapshot.hasNext()) {
                    callsCandidatesTemp.add(snapshot.next())
                }
            } catch (e: AGConnectCloudDBException) {
                Log.w(TAG, "Snapshot Error: " + e.message)
            } finally {

                for (data in callsCandidatesTemp) {
                    val iceCandidate = IceCandidate(
                        data.sdpMid,
                        data.sdpMLineIndex!!,
                        data.sdpCandidate
                    )
                    if (sdpType == Constants.TYPE.OFFER && data.callType == Constants.USERTYPE.OFFER_USER.name) {
                        signalingListener.onIceCandidateReceived(iceCandidate)
                    } else if (sdpType == Constants.TYPE.ANSWER && data.callType == Constants.USERTYPE.ANSWER_USER.name) {
                        signalingListener.onIceCandidateReceived(iceCandidate)
                    } else if (sdpType == Constants.TYPE.END) {
                        signalingListener.onCallEnded()
                    }

                    Log.e(TAG, "candidateQuery: $data")
                }
            }
        }

    fun sendIceCandidateModelToUser(iceCandidate: IceCandidate, isJoin: Boolean) = runBlocking {
        Log.d(TAG, "sendIceCandidateModelToUser: called")
        val type: String = when (isJoin) {
            true -> Constants.USERTYPE.ANSWER_USER.name
            false -> Constants.USERTYPE.OFFER_USER.name
        }

        val callsCandidates = CallsCandidates()
        callsCandidates.uuid = UUID.randomUUID().toString()
        callsCandidates.meetingID = meetingID
        callsCandidates.serverUrl = iceCandidate.serverUrl
        callsCandidates.sdpMid = iceCandidate.sdpMid
        callsCandidates.sdpMLineIndex = iceCandidate.sdpMLineIndex
        callsCandidates.sdpCandidate = iceCandidate.sdp
        callsCandidates.callType = type

        val upsertTask = cloudDBZone?.executeUpsert(callsCandidates)

        upsertTask?.addOnSuccessListener { cloudDBZoneResult ->
            Log.i(TAG, "Calls Candidate Upsert success: $cloudDBZoneResult")
        }?.addOnFailureListener {
            Log.i(TAG, "Calls Candidate Upsert failed: ${it.message}")
        }
    }

    private fun addCallsSdpSubscription() {

        try {
            val snapshotQuery =
                CloudDBZoneQuery.where(CallsSdp::class.java).equalTo(MEETING_ID, meetingID)
            mRegisterSdp = cloudDBZone?.subscribeSnapshot(
                snapshotQuery,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                mCallsSdpSnapshotListener
            )
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "subscribeSnapshot: " + e.message)
        }
    }

    private fun addCallsCandidatesSubscription() {

        try {
            val snapshotQuery =
                CloudDBZoneQuery.where(CallsCandidates::class.java).equalTo(MEETING_ID, meetingID)
            mRegisterCandidate = cloudDBZone?.subscribeSnapshot(
                snapshotQuery,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                mCallsCandidatesSnapshotListener
            )
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "subscribeSnapshot: " + e.message)
        }
    }

    fun removeEventsListener() {
        mRegisterCandidate?.remove()
        mRegisterSdp?.remove()
    }

    fun destroy() {
        job.complete()
    }

    companion object {
        private const val TAG = "SignalingClient"
    }
}