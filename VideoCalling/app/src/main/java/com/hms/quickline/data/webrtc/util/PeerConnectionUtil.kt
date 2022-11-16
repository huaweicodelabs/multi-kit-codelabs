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

package com.hms.quickline.data.webrtc.util

import android.app.Application
import org.webrtc.*

class PeerConnectionUtil(context: Application, eglBaseContext: EglBase.Context) {

    init {
        PeerConnectionFactory.InitializationOptions
            .builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials(fieldTrials)
            .createInitializationOptions().also { initializationOptions ->
                PeerConnectionFactory.initialize(initializationOptions)
            }
    }

    private val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)

    private val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(eglBaseContext)

    val peerConnectionFactory: PeerConnectionFactory = PeerConnectionFactory
        .builder()
        .setVideoDecoderFactory(defaultVideoDecoderFactory)
        .setVideoEncoderFactory(defaultVideoEncoderFactory)
        .setOptions(PeerConnectionFactory.Options().apply {
            disableEncryption = false
            disableNetworkMonitor = true
        })
        .createPeerConnectionFactory()

    val iceServer = listOf(
        PeerConnection.IceServer.builder(serverUri).createIceServer(),
        PeerConnection.IceServer.builder(serverUri2).createIceServer()
    )

    companion object {
        private const val fieldTrials = "WebRTC-H264HighProfile/Enabled/"
        private const val serverUri = "stun:stun1.l.google.com:19302"
        private const val serverUri2 = "stun:stun2.l.google.com:19302"
    }

}