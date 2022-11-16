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

import android.app.Application
import android.util.Log
import com.hms.quickline.core.util.Constants
import com.hms.quickline.core.util.Constants.AUDIO
import com.hms.quickline.core.util.Constants.LOCAL_STREAM_ID
import com.hms.quickline.core.util.Constants.LOCAL_TRACK_ID
import com.hms.quickline.core.util.Constants.MEETING_ID
import com.hms.quickline.core.util.Constants.VIDEO_FPS
import com.hms.quickline.core.util.Constants.VIDEO_HEIGHT
import com.hms.quickline.core.util.Constants.VIDEO_WIDTH
import com.hms.quickline.data.model.CallsCandidates
import com.hms.quickline.data.model.CallsSdp
import com.hms.quickline.data.model.Users
import com.hms.quickline.domain.repository.CloudDbWrapper
import com.hms.quickline.data.webrtc.observer.DataChannelObserver
import com.hms.quickline.data.webrtc.observer.SdpObserverImpl
import com.hms.quickline.data.webrtc.util.PeerConnectionUtil
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.agconnect.cloud.database.Text
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import org.webrtc.*
import java.lang.IllegalStateException

class WebRtcClient(
    private val context: Application,
    private val eglBase: EglBase,
    private val meetingID: String,
    private val dataChannelObserver: DataChannelObserver,
    private val peerConnectionObserver: PeerConnection.Observer
) {

    private var cloudDBZone: CloudDBZone? = CloudDbWrapper.cloudDBZone

    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null

    private val peerConnectionUtil = PeerConnectionUtil(
        context,
        eglBase.eglBaseContext
    )

    private val peerConnectionFactory = peerConnectionUtil.peerConnectionFactory

    private val _mediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("RtpDataChannels", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("DtlsSrtpkeyAgreement", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"))
    }

    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val peerConnection by lazy { buildPeerConnection(peerConnectionObserver) }

    private var localDataChannel: DataChannel? = null

    private var videoCapturer = getFrontCameraCapturer()

    private var isFrontCamera = true

    private fun buildPeerConnection(observer: PeerConnection.Observer) =
        peerConnectionFactory.createPeerConnection(
            peerConnectionUtil.iceServer,
            observer
        )

    fun createLocalDataChannel() {
        Log.d(WEB_RTC_DATA_CHANNEL_TAG, "createLocalDataChannel: called")

        Log.d(
            WEB_RTC_DATA_CHANNEL_TAG,
            "createLocalDataChannel: before initializing - $localDataChannel"
        )

        localDataChannel =
            peerConnection?.createDataChannel(Constants.DATA_CHANNEL_NAME, DataChannel.Init())

        Log.d(
            WEB_RTC_DATA_CHANNEL_TAG,
            "createLocalDataChannel: after initializing - $localDataChannel"
        )

        localDataChannel?.let {
            Log.d(WEB_RTC_DATA_CHANNEL_TAG, "createLocalDataChannel: observer registered")
            it.registerObserver(dataChannelObserver)
        }
    }


    fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(false)
        setEnableHardwareScaler(true)
        init(eglBase.eglBaseContext, null)
    }

    private fun getFrontCameraCapturer() = Camera2Enumerator(context).run {
        deviceNames.find {
            isFrontFacing(it)
        }?.let {
            createCapturer(it, null)
        } ?: throw IllegalStateException()
    }

    private fun getBackCameraCapturer() = Camera2Enumerator(context).run {
        deviceNames.find {
            isBackFacing(it)
        }?.let {
            createCapturer(it, null)
        } ?: throw IllegalStateException()
    }

    fun startLocalVideoCapture(localSurfaceView: SurfaceViewRenderer) {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, eglBase.eglBaseContext)
        (videoCapturer as VideoCapturer).initialize(
            surfaceTextureHelper,
            localSurfaceView.context,
            localVideoSource.capturerObserver
        )
        videoCapturer.startCapture(VIDEO_HEIGHT, VIDEO_WIDTH, VIDEO_FPS)

        localAudioTrack =
            peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + AUDIO, localAudioSource)
        localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)

        localVideoTrack?.addSink(localSurfaceView)

        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)

        peerConnection?.addStream(localStream)
    }

    fun startVoice() {

        localAudioTrack =
            peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + AUDIO, localAudioSource)

        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(localAudioTrack)

        peerConnection?.addStream(localStream)
    }

    fun call() {
        Log.d(TAG, "contacts: called")

        peerConnection?.createOffer(
            SdpObserverImpl(
                onCreateSuccessCallback = { sdp ->
                    Log.d(TAG, "contacts: onCreateSuccessCallback called")

                    peerConnection?.setLocalDescription(SdpObserverImpl(
                        onSetSuccessCallback = {
                            Log.d(TAG, "contacts: onSetSuccess called")

                            val offerSdp = CallsSdp()
                            offerSdp.meetingID = meetingID
                            offerSdp.sdp = Text(sdp.description)
                            offerSdp.callType = sdp.type.name

                            val upsertTask = cloudDBZone?.executeUpsert(offerSdp)
                            upsertTask?.addOnSuccessListener { cloudDBZoneResult ->
                                Log.i(TAG, "Calls Sdp Upsert success: $cloudDBZoneResult")
                            }?.addOnFailureListener {
                                Log.i(TAG, "Calls Sdp Upsert failed: ${it.message}")
                            }
                            Log.e(TAG, "onSetSuccess")
                        }
                    ), sdp)
                }
            ), _mediaConstraints
        )
    }

    fun answer() {
        Log.d(TAG, "answer: called")

        peerConnection?.createAnswer(
            SdpObserverImpl(
                onCreateSuccessCallback = { sdp ->
                    Log.d(TAG, "answer: onCreateSuccessCallback called")

                    val answerSdp = CallsSdp()
                    answerSdp.meetingID = meetingID
                    answerSdp.sdp = Text(sdp.description)
                    answerSdp.callType = sdp.type.name

                    val upsertTask = cloudDBZone?.executeUpsert(answerSdp)
                    upsertTask?.addOnSuccessListener { cloudDBZoneResult ->
                        Log.i(TAG, "Calls Answer Sdp Upsert success: $cloudDBZoneResult")
                    }?.addOnFailureListener {
                        Log.i(TAG, "Calls Answer Sdp Upsert failed: ${it.message}")
                    }

                    peerConnection?.setLocalDescription(SdpObserverImpl(
                        onSetSuccessCallback = {
                            Log.d(TAG, "answer: onSetSuccessCallback called")
                        }
                    ), sdp)
                }
            ), _mediaConstraints
        )
    }

    fun endCall() {

        val callsSdp = CallsSdp()
        callsSdp.meetingID = meetingID
        callsSdp.callType = Constants.TYPE.END.name
        val upsertTask = cloudDBZone?.executeUpsert(callsSdp)

        upsertTask?.addOnSuccessListener { cloudDBZoneResult ->
            Log.i(TAG, "Calls Sdp Upsert success: $cloudDBZoneResult")
        }?.addOnFailureListener {
            Log.i(TAG, "Calls Sdp Upsert failed: ${it.message}")
        }

        CloudDbWrapper.getUserById(meetingID, object : CloudDbWrapper.ICloudDbWrapper {
            override fun onUserObtained(users: Users) {
                users.isCalling = false
                val upsertTaskIsCalling = cloudDBZone?.executeUpsert(users)
                upsertTaskIsCalling?.addOnSuccessListener { cloudDBZoneResult ->
                    Log.i(TAG, "Calls Sdp Upsert success: $cloudDBZoneResult")
                }?.addOnFailureListener {
                    Log.i(TAG, "Calls Sdp Upsert failed: ${it.message}")
                }
            }
        })
    }

    fun setRemoteDescription(sessionDescription: SessionDescription) =
        peerConnection?.setRemoteDescription(SdpObserverImpl(), sessionDescription)

    fun addIceCandidate(iceCandidate: IceCandidate) = peerConnection?.addIceCandidate(iceCandidate)

    fun closePeerConnection() = peerConnection?.close()

    fun clearCandidates() {
        val queryCallsCandidates =
            CloudDBZoneQuery.where(CallsCandidates::class.java).equalTo(MEETING_ID, meetingID)
        val queryTaskCallsCandidates = cloudDBZone?.executeQuery(
            queryCallsCandidates,
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )
        queryTaskCallsCandidates?.addOnSuccessListener { snapshot ->
            val callsCandidatesList = mutableListOf<CallsCandidates>()
            try {
                while (snapshot.snapshotObjects.hasNext()) {
                    callsCandidatesList.add(snapshot.snapshotObjects.next())
                }
            } catch (e: AGConnectCloudDBException) {
                Log.w(TAG, "Snapshot Error: " + e.message)
            } finally {
                val iceCandidateArray: MutableList<IceCandidate> = mutableListOf()
                for (data in callsCandidatesList) {
                    val iceCandidate = IceCandidate(
                        data.sdpMid,
                        data.sdpMLineIndex!!,
                        data.sdpCandidate
                    )
                    if (data.callType != null && data.callType == Constants.USERTYPE.OFFER_USER.name) {
                        iceCandidateArray.add(iceCandidate)
                    } else if (data.callType != null && data.callType == Constants.USERTYPE.ANSWER_USER.name) {
                        iceCandidateArray.add(iceCandidate)
                    }
                }

                peerConnection?.removeIceCandidates(iceCandidateArray.toTypedArray())

                val deleteTask = cloudDBZone?.executeDelete(callsCandidatesList)
                deleteTask?.addOnSuccessListener {
                    Log.i(TAG, "Candidates Delete success: $it")
                }?.addOnFailureListener {
                    Log.i(TAG, "Candidates Delete failed: $it")
                }
                snapshot.release()
            }
        }?.addOnFailureListener {
            Log.w(TAG, "QueryTask Failure: " + it.message)
        }
    }

    fun clearSdp() {

        val queryCallsSdp =
            CloudDBZoneQuery.where(CallsSdp::class.java).equalTo(MEETING_ID, meetingID)
        val queryTaskCallsSdp = cloudDBZone?.executeQuery(
            queryCallsSdp,
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )
        queryTaskCallsSdp?.addOnSuccessListener { snapshot ->
            val callsSdpList = mutableListOf<CallsSdp>()
            try {
                while (snapshot.snapshotObjects.hasNext()) {
                    callsSdpList.add(snapshot.snapshotObjects.next())
                }
            } catch (e: AGConnectCloudDBException) {
                Log.w(TAG, "Snapshot Error: " + e.message)
            } finally {
                val deleteTask = cloudDBZone?.executeDelete(callsSdpList)
                deleteTask?.addOnSuccessListener {
                    Log.i(TAG, "Sdp Delete success: $it")
                }?.addOnFailureListener {
                    Log.i(TAG, "Sdp Delete failed: $it")
                }
                snapshot.release()
            }
        }?.addOnFailureListener {
            Log.w(TAG, "QueryTask Failure: " + it.message)
        }

        peerConnection?.close()
    }

    fun checkDataChannelState() {
        if (localDataChannel?.state() == DataChannel.State.OPEN) {
            Log.d(WEB_RTC_DATA_CHANNEL_TAG, "checkDataChannelState: OPEN")
        } else {
            Log.d(WEB_RTC_DATA_CHANNEL_TAG, "checkDataChannelState: CLOSE")
        }
    }

    fun enableVideo(isVideoEnabled: Boolean) {
        localVideoTrack?.setEnabled(isVideoEnabled)
    }

    fun enableAudio(isAudioEnable: Boolean) {
        localAudioTrack?.setEnabled(isAudioEnable)
    }

    fun switchCamera() {
        isFrontCamera = !isFrontCamera
        videoCapturer.stopCapture()
        videoCapturer = if (isFrontCamera) getFrontCameraCapturer()
        else getBackCameraCapturer()
    }

    companion object {
        private const val TAG = "WebRtcClient"
        private const val WEB_RTC_DATA_CHANNEL_TAG = "WebRtcDataChannel"
    }
}