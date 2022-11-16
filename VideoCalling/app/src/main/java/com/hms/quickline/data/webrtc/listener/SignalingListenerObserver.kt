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
w
package com.hms.quickline.data.webrtc.listener

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class SignalingListenerObserver(
    private val onConnectionEstablishedCallback: () -> Unit = {},
    private val onOfferReceivedCallback: (SessionDescription) -> Unit = {},
    private val onAnswerReceivedCallback: (SessionDescription) -> Unit = {},
    private val onIceCandidateReceivedCallback: (IceCandidate) -> Unit = {},
    private val onCallEndedCallback: () -> Unit = {}
) : SignalingListener {
    override fun onConnectionEstablished() {
        onConnectionEstablishedCallback()
    }

    override fun onOfferReceived(description: SessionDescription) {
        onOfferReceivedCallback(description)
    }

    override fun onAnswerReceived(description: SessionDescription) {
        onAnswerReceivedCallback(description)
    }

    override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        onIceCandidateReceivedCallback(iceCandidate)
    }

    override fun onCallEnded() {
        onCallEndedCallback()
    }
}