/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
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
 */
package com.huawei.hms.knowmyboard.dtse.activity.ml

import android.graphics.Canvas
import com.huawei.hms.mlsdk.common.LensEngine

abstract class BaseGraphic(private val graphicOverlay: GraphicOverlay) {
    /**
     * Draw results
     * @param canvas canvas
     */
    abstract fun draw(canvas: Canvas?)
    fun scaleX(x: Float): Float {
        return x * graphicOverlay.widthScaleValue
    }

    fun scaleY(y: Float): Float {
        return y * graphicOverlay.heightScaleValue
    }

    fun translateX(x: Float): Float {
        return if (graphicOverlay.cameraFacing == LensEngine.FRONT_LENS) {
            graphicOverlay.width - scaleX(x)
        } else {
            scaleX(x)
        }
    }

    fun translateY(y: Float): Float {
        return scaleY(y)
    }
}