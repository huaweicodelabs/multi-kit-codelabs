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

package com.hms.quickline.data.webrtc.observer

import org.webrtc.*

open class PeerConnectionObserver(
    private val onIceCandidateCallback: (IceCandidate) -> Unit = {},
    private val onAddStreamCallback: (MediaStream) -> Unit = {},
    private val onTrackCallback: (RtpTransceiver) -> Unit = {},
    private val onDataChannelCallback: (DataChannel) -> Unit = {},
) : PeerConnection.Observer {
    override fun onIceCandidate(iceCandidate: IceCandidate?) {
        iceCandidate?.let {
            onIceCandidateCallback(iceCandidate)
        }
    }

    override fun onDataChannel(dataChannel: DataChannel?) {
        dataChannel?.let {
            onDataChannelCallback(it)
        }
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
    }

    override fun onAddStream(mediaStream: MediaStream?) {
        mediaStream?.let {
            onAddStreamCallback(mediaStream)
        }
    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
    }

    override fun onRemoveStream(p0: MediaStream?) {
    }

    override fun onRenegotiationNeeded() {
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {

    }

    override fun onTrack(transceiver: RtpTransceiver?) {
        super.onTrack(transceiver)
        transceiver?.let {
            onTrackCallback(transceiver)
        }
    }
}