/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
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
package com.huawei.hms.knowmyboard.dtse.activity.ml

import android.hardware.Camera

class CameraConfiguration {
    var fps = 20.0f
    var previewWidth = MAX_WIDTH
    var previewHeight = MAX_HEIGHT
    val isAutoFocus = true
    fun setCameraFacing(facing: Int) {
        synchronized(lock) {
            require(!(facing != CAMERA_FACING_BACK && facing != CAMERA_FACING_FRONT)) { "Invalid camera: $facing" }
            cameraFacing = facing
        }
    }

    companion object {
        const val CAMERA_FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK
        const val CAMERA_FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT
        const val MAX_WIDTH = 960
        const val MAX_HEIGHT = 720
        protected var cameraFacing = CAMERA_FACING_BACK
        private val lock = Any()
        @JvmName("getCameraFacing1")
        @JvmStatic
        fun getCameraFacing(): Int {
            synchronized(lock) { return cameraFacing }
        }
    }
}