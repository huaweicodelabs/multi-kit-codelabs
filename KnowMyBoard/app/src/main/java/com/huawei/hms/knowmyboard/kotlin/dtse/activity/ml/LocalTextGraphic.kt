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

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.huawei.hms.mlsdk.text.MLText

class LocalTextGraphic(overlay: GraphicOverlay?, private val text: MLText.Base?) : BaseGraphic(
    overlay!!
) {
    private val rectPaint: Paint
    private val textPaint: Paint
    override fun draw(canvas: Canvas?) {
        checkNotNull(text) { "Attempting to draw a null text." }

        // Draw text boundaries accurately based on boundary points.
        val points = text.vertexes
        if (points != null && points.size == 4) {
            for (i in points.indices) {
                points[i].x = translateX(points[i].x.toFloat()).toInt()
                points[i].y = translateY(points[i].y.toFloat()).toInt()
            }
            val pts = floatArrayOf(
                points[0].x.toFloat(),
                points[0].y.toFloat(),
                points[1].x.toFloat(),
                points[1].y.toFloat(),
                points[1].x.toFloat(),
                points[1].y.toFloat(),
                points[2].x.toFloat(),
                points[2].y.toFloat(),
                points[2].x.toFloat(),
                points[2].y.toFloat(),
                points[3].x.toFloat(),
                points[3].y.toFloat(),
                points[3].x.toFloat(),
                points[3].y.toFloat(),
                points[0].x.toFloat(),
                points[0].y.toFloat()
            )
            val averageHeight = (points[3].y - points[0].y + (points[2].y - points[1].y)) / 2.0f
            val textSize = averageHeight * 0.7f
            val offset = averageHeight / 4
            textPaint.textSize = textSize
            canvas!!.drawLines(pts, rectPaint)
            val path = Path()
            path.moveTo(points[3].x.toFloat(), points[3].y - offset)
            path.lineTo(points[2].x.toFloat(), points[2].y - offset)
            canvas.drawLines(pts, rectPaint)
            canvas.drawTextOnPath(text.stringValue, path, 0f, 0f, textPaint)
        }
    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val TEXT_SIZE = 45.0f
        private const val STROKE_WIDTH = 4.0f
    }

    init {
        rectPaint = Paint()
        rectPaint.color = TEXT_COLOR
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = STROKE_WIDTH
        textPaint = Paint()
        textPaint.color = TEXT_COLOR
        textPaint.textSize = TEXT_SIZE
    }
}