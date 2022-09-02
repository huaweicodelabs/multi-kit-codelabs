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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

class CameraImageGraphic : BaseGraphic {
    private val bitmap: Bitmap
    private var isFill = true

    constructor(overlay: GraphicOverlay?, bitmap: Bitmap) : super(overlay!!) {
        this.bitmap = bitmap
    }

    constructor(overlay: GraphicOverlay?, bitmap: Bitmap, isFill: Boolean) : super(overlay!!) {
        this.bitmap = bitmap
        this.isFill = isFill
    }

    override fun draw(canvas: Canvas?) {
        val width: Int
        val height: Int
        if (isFill) {
            width = canvas!!.width
            height = canvas.height
        } else {
            width = bitmap.width
            height = bitmap.height
        }
        canvas!!.drawBitmap(bitmap, null, Rect(0, 0, width, height), null)
    }
}