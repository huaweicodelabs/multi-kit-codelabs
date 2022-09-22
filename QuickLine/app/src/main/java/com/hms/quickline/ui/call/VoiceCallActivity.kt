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

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.hms.quickline.R
import com.hms.quickline.core.adapter.loadImage
import com.hms.quickline.core.util.Constants
import com.hms.quickline.core.util.Constants.MEETING_ID
import com.hms.quickline.core.util.Constants.NAME
import com.hms.quickline.core.util.invisible
import com.hms.quickline.core.util.visible
import com.hms.quickline.databinding.ActivityVoiceCallBinding
import com.hms.quickline.data.webrtc.RTCAudioManager
import com.hms.quickline.data.webrtc.SignalingClient
import com.hms.quickline.data.webrtc.WebRtcClient
import com.hms.quickline.data.webrtc.listener.SignalingListenerObserver
import com.hms.quickline.data.webrtc.observer.DataChannelObserver
import com.hms.quickline.data.webrtc.observer.PeerConnectionObserver
import com.hms.quickline.data.webrtc.util.PeerConnectionUtil
import dagger.hilt.android.AndroidEntryPoint
import org.webrtc.*
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class VoiceCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceCallBinding

    private lateinit var meetingID: String
    private var name: String? = null
    private var isJoin by Delegates.notNull<Boolean>()

    private lateinit var webRtcClient: WebRtcClient
    private lateinit var peerConnectionUtil: PeerConnectionUtil

    private lateinit var signalingClient: SignalingClient

    @Inject
    lateinit var eglBase: EglBase

    private var isMute = false
    private var inSpeakerMode = true

    private val audioManager by lazy { RTCAudioManager.create(this) }

    var millisecondTime = 0L
    var startTime = 0L

    var seconds = 0
    var minutes = 0

    var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())

        receivingPreviousActivityData()
        initializingClasses()

        with(binding) {

            name?.let {
                tvCallingUser.text = name
            }

            micBtn.setOnClickListener {
                isMute = !isMute
                webRtcClient.enableAudio(!isMute)
                if (isMute) micBtn.setImageResource(R.drawable.ic_mic_off)
                else micBtn.setImageResource(R.drawable.ic_mic)
            }

            btnAudioOutput.setOnClickListener {
                inSpeakerMode = !inSpeakerMode
                if (inSpeakerMode) {
                    btnAudioOutput.setImageResource(R.drawable.ic_hearing)
                    audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
                } else {
                    btnAudioOutput.setImageResource(R.drawable.ic_speaker_up)
                    audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
                }
            }

            endCallBtn.setOnClickListener {
                webRtcClient.endCall()
                signalingClient.removeEventsListener()
                signalingClient.destroy()
                finish()
            }
        }
    }

    private var runnable: Runnable = object : Runnable {

        override fun run() {

            millisecondTime = SystemClock.uptimeMillis() - startTime

            seconds = (millisecondTime / 1000).toInt()

            minutes = seconds / 60

            seconds %= 60

            if (minutes.toString().length < 2) {
                "0$minutes:".also { binding.tvCallingTimeMinute.text = it }
            } else {
                binding.tvCallingTimeMinute.text = minutes.toString()
            }

            if (seconds.toString().length < 2) {
                "0$seconds".also { binding.tvCallingTimeSecond.text = it }
            } else {
                binding.tvCallingTimeSecond.text = seconds.toString()
            }

            handler?.postDelayed(this, 0)
        }
    }

    private fun receivingPreviousActivityData() {

        intent.getStringExtra(MEETING_ID)?.let {
            meetingID = it
        }

        intent.getStringExtra(NAME)?.let {
            name = it
        }

        isJoin = intent.getBooleanExtra(Constants.IS_JOIN, false)

        Log.d(TAG, "receivingPreviousFragmentData: roomName = $meetingID & isJoin = $isJoin")
    }

    private fun initializingClasses() {
        peerConnectionUtil = PeerConnectionUtil(
            application,
            eglBase.eglBaseContext
        )

        webRtcClient = WebRtcClient(
            context = application,
            eglBase = eglBase,
            meetingID = meetingID,
            dataChannelObserver = DataChannelObserver(
                onBufferedAmountChangeCallback = {
                    Log.d(WEB_RTC_DATA_CHANNEL_TAG, "onBufferedAmountChange: called")
                },
                onStateChangeCallback = {
                    Log.d(WEB_RTC_DATA_CHANNEL_TAG, "onStateChange: called")
                    webRtcClient.checkDataChannelState()
                },
                onMessageCallback = {
                    Log.d(WEB_RTC_DATA_CHANNEL_TAG, "onMessage: called")
                }
            ),
            peerConnectionObserver = PeerConnectionObserver(
                onIceCandidateCallback = {
                    signalingClient.sendIceCandidateModelToUser(it, isJoin)
                    webRtcClient.addIceCandidate(it)
                },
                onTrackCallback = {

                },
                onAddStreamCallback = {

                },
                onDataChannelCallback = { dataChannel ->
                    Log.d(
                        WEB_RTC_DATA_CHANNEL_TAG,
                        "onDataChannelCallback: state -> ${dataChannel.state()}"
                    )
                    dataChannel.registerObserver(
                        DataChannelObserver(
                            onStateChangeCallback = {
                                Log.d(
                                    WEB_RTC_DATA_CHANNEL_TAG,
                                    "onDataChannelCallback - onStateChangeCallback - remote data channel state -> ${
                                        dataChannel.state()
                                    }"
                                )
                            },
                            onMessageCallback = {
                                Log.d(
                                    WEB_RTC_DATA_CHANNEL_TAG,
                                    "onDataChannelCallback - onMessageCallback -> got Message"
                                )
                            }
                        )
                    )
                }
            )
        )
        webRtcClient.createLocalDataChannel()
        initVoice()
    }

    private fun initVoice() {
        webRtcClient.startVoice()
        handlingSignalingClient()
    }

    private fun handlingSignalingClient() {
        signalingClient = SignalingClient(
            meetingID = meetingID,
            signalingListener = SignalingListenerObserver(
                onConnectionEstablishedCallback = {
                    Log.d(
                        SIGNALING_LISTENER_TAG,
                        "handlingSignalingClient: onConnectionEstablishedCallback called"
                    )
                    binding.endCallBtn.isClickable = true
                },
                onOfferReceivedCallback = {
                    Log.d(
                        SIGNALING_LISTENER_TAG,
                        "handlingSignalingClient: onOfferReceivedCallback called"
                    )
                    webRtcClient.setRemoteDescription(it)
                    webRtcClient.answer()

                },
                onAnswerReceivedCallback = {
                    Log.d(
                        SIGNALING_LISTENER_TAG,
                        "handlingSignalingClient: onAnswerReceivedCallback called"
                    )
                    webRtcClient.setRemoteDescription(it)
                    runOnUiThread {
                        with(binding) {
                            imgVoiceLoading.invisible()
                            tvCallingText.invisible()
                            imgUserImage.visible()
                            tvCallingTimeMinute.visible()
                            tvCallingTimeSecond.visible()

                            loadImage(imgUserImage,USER_IMAGE)
                        }

                        handler?.postDelayed(runnable, 0)
                        startTime = SystemClock.uptimeMillis()
                    }
                },
                onIceCandidateReceivedCallback = {
                    Log.d(
                        SIGNALING_LISTENER_TAG,
                        "handlingSignalingClient: onIceCandidateReceivedCallback called"
                    )
                    webRtcClient.addIceCandidate(it)
                },
                onCallEndedCallback = {
                    Log.d(
                        SIGNALING_LISTENER_TAG,
                        "handlingSignalingClient: onCallEndedCallback called"
                    )
                    webRtcClient.clearSdp()
                    webRtcClient.clearCandidates()
                    webRtcClient.closePeerConnection()
                    signalingClient.removeEventsListener()
                    signalingClient.destroy()
                    finish()
                }
            )
        )

        if (!isJoin)
            webRtcClient.call()
    }

    override fun onDestroy() {
        super.onDestroy()
        webRtcClient.clearCandidates()
        webRtcClient.closePeerConnection()
        signalingClient.removeEventsListener()
        signalingClient.destroy()
    }

    companion object {
        private const val TAG = "ui_CallFragment"
        private const val WEB_RTC_DATA_CHANNEL_TAG = "ui_WebRtcDataChannel"
        private const val SIGNALING_LISTENER_TAG = "signalingListener"
        private const val PERMISSION_CODE = 101
        private const val USER_IMAGE = "https://media-exp1.licdn.com/dms/image/D4D03AQEweV5ra2apTw/profile-displayphoto-shrink_800_800/0/1630667862366?e=1658361600&v=beta&t=qlNpziZO8fxddUwj5eiVQYygZJA0tNHNdFZTkBbdg-A"
    }
}