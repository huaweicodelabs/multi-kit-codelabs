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

package com.hms.quickline.core.util

object Constants {

    const val CloudDbZoneName = "COMDBZone"

    const val VIDEO_WIDTH = 1280
    const val VIDEO_HEIGHT = 720
    const val VIDEO_FPS = 30

    const val REQUEST_CODE = 100

    const val HUAWEI_ID_SIGN_IN = 8888

    const val LOCAL_TRACK_ID = "local_track"
    const val LOCAL_STREAM_ID = "stream_track"
    const val AUDIO ="_audio"


    const val DATA_CHANNEL_NAME = "sendDataChannel"

    const val MEETING_ID = "meetingID"
    const val IS_JOIN = "isJoin"
    const val ANSWER = "answer"
    const val UID = "uid"
    const val USER = "user"
    const val CALLER_NAME = "callerName"
    const val DECLINE = "decline"
    const val IS_MEETING_CONTACT = "isMeetingContact"
    const val NAME = "name"


    enum class USERTYPE {
        OFFER_USER,
        ANSWER_USER
    }

    enum class TYPE {
        OFFER,
        ANSWER,
        END
    }
}

