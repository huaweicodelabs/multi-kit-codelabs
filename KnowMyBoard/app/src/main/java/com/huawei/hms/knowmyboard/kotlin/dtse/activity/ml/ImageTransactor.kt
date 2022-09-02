/**
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 2020.2.21-Changed name from VisionImageProcessor to ImageTransactor.
 * 2020.2.21-Deleted method: process(Bitmap bitmap, GraphicOverlay graphicOverlay,String path,boolean flag);
 * process(Bitmap bitmap, GraphicOverlay graphicOverlay,String path);
 * Huawei Technologies Co., Ltd.
 */
package com.huawei.hms.knowmyboard.dtse.activity.ml

import android.graphics.Bitmap
import java.nio.ByteBuffer

interface ImageTransactor {
    /**
     * Process a frame of image captured dynamically by a camera
     * @param data image data
     * @param frameMetadata metadata
     * @param graphicOverlay graphicOverlay
     */
    fun process(data: ByteBuffer?, frameMetadata: FrameMetadata?, graphicOverlay: GraphicOverlay?)

    /**
     * Process a still image
     * @param bitmap bitmap
     * @param graphicOverlay graphicOverlay
     */
    fun process(bitmap: Bitmap?, graphicOverlay: GraphicOverlay?)

    /**
     * stop
     */
    fun stop()
}