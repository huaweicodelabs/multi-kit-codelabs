/**
 * Copyright 2018 Google LLC
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 2020.2.21-Changed name from CameraSourcePreview to LensEnginePreview, in addition, the onTouchEvent, handleZoom, handleFocusMetering method is added.
 * Huawei Technologies Co., Ltd.
 */
package com.huawei.hms.knowmyboard.dtse.activity.ml

import com.huawei.hms.knowmyboard.dtse.activity.ml.CameraConfiguration.Companion.getCameraFacing
import android.view.ViewGroup
import android.view.SurfaceView
import android.view.SurfaceHolder
import kotlin.Throws
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.hardware.Camera.PictureCallback
import android.view.MotionEvent
import android.graphics.RectF
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import java.io.IOException
import java.util.ArrayList

class LensEnginePreview(context: Context, attrs: AttributeSet?) : ViewGroup(
    context, attrs
) {
    private val surfaceView: SurfaceView
    private var startRequested = false
    private var lensEngine: LensEngine? = null
    private var oldDist = 1f

    /**
     * Determines whether the camera preview frame and detection result display frame are synchronous or asynchronous.
     */
    private var isSynchronous = false
    val surfaceHolder: SurfaceHolder
        get() = surfaceView.holder

    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?, isSynchronous: Boolean) {
        this.isSynchronous = isSynchronous
        if (lensEngine == null) {
            stop()
        }
        this.lensEngine = lensEngine
        if (this.lensEngine != null) {
            startRequested = true
            startLensEngine()
        }
    }

    fun stop() {
        if (lensEngine != null) {
            lensEngine!!.stop()
        }
    }

    fun release() {
        if (lensEngine != null) {
            lensEngine!!.release()
            lensEngine = null
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    private fun startLensEngine() {
        if (startRequested) {
            lensEngine!!.run()
            startRequested = false
        }
    }

    fun takePicture(pictureCallback: PictureCallback?) {
        if (lensEngine != null) {
            lensEngine!!.takePicture(pictureCallback)
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            try {
                startLensEngine()
            } catch (e: IOException) {
                Log.e(TAG, "Could not start lensEngine.", e)
            }
        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {}
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            Log.d(TAG, "surfaceChanged")
            val camera = lensEngine!!.camera ?: return
            try {
                camera.setPreviewDisplay(surfaceView.holder)
            } catch (e: IOException) {
                Log.e(TAG, "IOException", e)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        try {
            startLensEngine()
        } catch (e: IOException) {
            Log.e(TAG, "Could not start LensEngine.", e)
        }
        if (lensEngine == null) {
            return
        }
        val size = lensEngine!!.previewSize ?: return
        var width = size.width
        var height = size.height


        // When the phone is in portrait orientation, the width and height dimensions are interchangeable.
        if (isVertical) {
            val tmp = width
            width = height
            height = tmp
        }
        val viewWidth = right - left
        val viewHeight = bottom - top
        val childWidth: Int
        val childHeight: Int
        var childXOffset = 0
        var childYOffset = 0
        val widthRatio = viewWidth.toFloat() / width.toFloat()
        val heightRatio = viewHeight.toFloat() / height.toFloat()

        // To fill the view with the camera preview, while also preserving the correct aspect ratio,
        // it is usually necessary to slightly oversize the child and to crop off portions along one
        // of the dimensions. We scale up based on the dimension requiring the most correction, and
        // compute a crop offset for the other dimension.
        if (widthRatio > heightRatio) {
            childWidth = viewWidth
            childHeight = (height.toFloat() * widthRatio).toInt()
            childYOffset = (childHeight - viewHeight) / 2
        } else {
            childWidth = (width.toFloat() * heightRatio).toInt()
            childHeight = viewHeight
            childXOffset = (childWidth - viewWidth) / 2
        }
        for (i in 0 until this.childCount) {
            // One dimension will be cropped. We shift child over or up by this offset and adjust
            // the size to maintain the proper aspect ratio.
            getChildAt(i)
                .layout(
                    -1 * childXOffset,
                    -1 * childYOffset,
                    childWidth - childXOffset,
                    childHeight - childYOffset
                )
        }
    }

    private val isVertical: Boolean
        private get() {
            val orientation = context.resources.configuration.orientation
            return orientation == Configuration.ORIENTATION_PORTRAIT
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (getCameraFacing() == CameraConfiguration.CAMERA_FACING_FRONT) {
            return true
        }
        if (lensEngine == null) {
            return true
        }
        if (event.pointerCount == 1) {
            when (event.action) {
                MotionEvent.ACTION_UP -> handleFocusMetering(event, lensEngine!!.camera)
            }
        } else {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> oldDist = getFingerSpacing(event)
                MotionEvent.ACTION_MOVE -> {
                    Log.d(TAG, "toby onTouch: ACTION_MOVE")
                    val newDist = getFingerSpacing(event)
                    if (newDist > oldDist) {
                        handleZoom(true, lensEngine!!.camera)
                    } else if (newDist < oldDist) {
                        handleZoom(false, lensEngine!!.camera)
                    }
                    oldDist = newDist
                }
                else -> {}
            }
        }
        return true
    }

    private fun handleZoom(isZoomIn: Boolean, camera: Camera?) {
        val params = camera!!.parameters
        if (params.isZoomSupported) {
            val maxZoom = params.maxZoom
            var zoom = params.zoom
            if (isZoomIn && zoom < maxZoom) {
                zoom++
            } else if (zoom > 0) {
                zoom--
            }
            params.zoom = zoom
            camera.parameters = params
        } else {
            Log.i(TAG, "zoom not supported")
        }
    }

    private fun handleFocusMetering(event: MotionEvent, camera: Camera?) {
        val viewWidth = this.width
        val viewHeight = this.height
        val focusRect = calculateTapArea(event.x, event.y, 1f, viewWidth, viewHeight)
        val meteringRect = calculateTapArea(event.x, event.y, 1.5f, viewWidth, viewHeight)
        camera!!.cancelAutoFocus()
        val params = camera.parameters
        if (params.maxNumFocusAreas > 0) {
            val focusAreas: MutableList<Camera.Area> = ArrayList()
            focusAreas.add(Camera.Area(focusRect, 800))
            params.focusAreas = focusAreas
        } else {
            Log.i(TAG, "focus areas not supported")
        }
        if (params.maxNumMeteringAreas > 0) {
            val meteringAreas: MutableList<Camera.Area> = ArrayList()
            meteringAreas.add(Camera.Area(meteringRect, 800))
            params.meteringAreas = meteringAreas
        } else {
            Log.i(TAG, "metering areas not supported")
        }
        val currentFocusMode = params.focusMode
        Log.d(TAG, "toby onTouch:$currentFocusMode")
        params.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        camera.parameters = params
        camera.autoFocus { success, camera ->
            val params = camera.parameters
            params.focusMode = currentFocusMode
            camera.parameters = params
        }
    }

    companion object {
        private const val TAG = "LensEnginePreview"
        private fun getFingerSpacing(event: MotionEvent): Float {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            return Math.sqrt((x * x + y * y).toDouble()).toFloat()
        }

        private fun calculateTapArea(
            x: Float,
            y: Float,
            coefficient: Float,
            width: Int,
            height: Int
        ): Rect {
            val focusAreaSize = 300f
            val areaSize = java.lang.Float.valueOf(focusAreaSize * coefficient).toInt()
            val centerX = (x / width * 2000 - 1000).toInt()
            val centerY = (y / height * 2000 - 1000).toInt()
            val halfAreaSize = areaSize / 2
            val rectF = RectF(
                clamp(centerX - halfAreaSize, -1000, 1000).toFloat(),
                clamp(centerY - halfAreaSize, -1000, 1000).toFloat(),
                clamp(centerX + halfAreaSize, -1000, 1000).toFloat(),
                clamp(centerY + halfAreaSize, -1000, 1000).toFloat()
            )
            return Rect(
                Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right),
                Math.round(rectF.bottom)
            )
        }

        private fun clamp(x: Int, min: Int, max: Int): Int {
            if (x > max) {
                return max
            }
            return if (x < min) {
                min
            } else x
        }
    }

    init {
        surfaceView = SurfaceView(context)
        surfaceView.holder.addCallback(SurfaceCallback())
        this.addView(surfaceView)
    }
}