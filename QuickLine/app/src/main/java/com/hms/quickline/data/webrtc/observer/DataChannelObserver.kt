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

import org.webrtc.DataChannel

open class DataChannelObserver(
    private val onBufferedAmountChangeCallback: (Long) -> Unit = {},
    private val onStateChangeCallback: () -> Unit = {},
    private val onMessageCallback: (DataChannel.Buffer) -> Unit = {},
) : DataChannel.Observer {
    override fun onBufferedAmountChange(p0: Long) {
        onBufferedAmountChangeCallback(p0)
    }

    override fun onStateChange() {
        onStateChangeCallback()
    }

    override fun onMessage(p0: DataChannel.Buffer?) {
        p0?.let {
            onMessageCallback(it)
        }
    }
}