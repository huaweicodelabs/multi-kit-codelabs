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
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.huawei.hms.mlsdk.text.MLText

class LocalDataProcessor {
    private var previewWidth = 0f
    private var previewHeight = 0f
    private var widthScaleValue = 1.0f
    private var heightScaleValue = 1.0f
    private var isLandScape = false
    private var maxWidthOfImage: Int? = null
    private var maxHeightOfImage: Int? = null
    fun setLandScape(landScape: Boolean) {
        isLandScape = landScape
    }

    fun setCameraInfo(graphicOverlay: GraphicOverlay, canvas: Canvas, width: Float, height: Float) {
        previewWidth = width * graphicOverlay.widthScaleValue
        previewHeight = height * graphicOverlay.heightScaleValue
        if (previewWidth != 0f && previewHeight != 0f) {
            widthScaleValue = canvas.width.toFloat() / previewWidth
            heightScaleValue = canvas.height.toFloat() / previewHeight
        }
    }

    fun drawHmsMLVisionText(canvas: Canvas, blocks: List<MLText.Block>) {
        val rectPaint = Paint()
        rectPaint.color = Color.WHITE
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 4.0f
        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 45.0f
        for (i in blocks.indices) {
            val lines = blocks[i].contents
            for (j in lines.indices) {
                // Display by line, without displaying empty lines.
                if (lines[j].stringValue != null && lines[j].stringValue.trim { it <= ' ' }.length != 0) {
                    drawText(rectPaint, textPaint, canvas, lines[j])
                }
            }
        }
    }

    private fun drawText(rectPaint: Paint, textPaint: Paint, canvas: Canvas, text: MLText.Base) {
        val points = text.vertexes
        if (points != null && points.size == 4) {
            for (i in points.indices) {
                points[i].x = scaleX(points[i].x.toFloat()).toInt()
                points[i].y = scaleY(points[i].y.toFloat()).toInt()
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
            canvas.drawLines(pts, rectPaint)
            val path = Path()
            path.moveTo(points[3].x.toFloat(), points[3].y - offset)
            path.lineTo(points[2].x.toFloat(), points[2].y - offset)
            canvas.drawLines(pts, rectPaint)
            canvas.drawTextOnPath(text.stringValue, path, 0f, 0f, textPaint)
        }
    }

    fun scaleX(x: Float): Float {
        return x * widthScaleValue
    }

    fun scaleY(y: Float): Float {
        return y * heightScaleValue
    }

    fun getMaxWidthOfImage(frameMetadata: FrameMetadata): Int? {
        if (maxWidthOfImage == null) {
            if (isLandScape) {
                maxWidthOfImage = frameMetadata.height
            } else {
                maxWidthOfImage = frameMetadata.width
            }
        }
        return maxWidthOfImage
    }

    fun getMaxHeightOfImage(frameMetadata: FrameMetadata): Int? {
        if (maxHeightOfImage == null) {
            if (isLandScape) {
                maxHeightOfImage = frameMetadata.width
            } else {
                maxHeightOfImage = frameMetadata.height
            }
        }
        return maxHeightOfImage
    }
}