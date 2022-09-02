/**
 * Copyright 2018 Google LLC
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
 * 2020.2.21-Changed the name from CameraSource to CameraSelector, CameraSelector was part of the original CameraSource.
 * 2020.2.21-Changed method CameraSelector, change SizePair to CameraSize.
 * Huawei Technologies Co., Ltd.
 */
package com.huawei.hms.knowmyboard.dtse.activity.ml

import com.huawei.hms.knowmyboard.dtse.activity.ml.CameraConfiguration.Companion.getCameraFacing
import android.app.Activity
import android.content.Context
import kotlin.Throws
import android.graphics.ImageFormat
import android.hardware.Camera
import android.view.WindowManager
import android.hardware.Camera.CameraInfo
import android.util.Log
import android.view.Surface
import com.huawei.hms.common.size.Size
import java.io.IOException
import java.util.ArrayList

class CameraSelector(
    protected var activity: Activity,
    private val configuration: CameraConfiguration
) {
    var previewSize: Size? = null
        private set
    var rotation = 0
        private set

    /**
     * Opens the camera and applies the user settings.
     *
     * @throws IOException if camera cannot be found or preview cannot be processed
     */
    @Throws(IOException::class)
    fun createCamera(): Camera {
        val cameraId = getIdForRequestedCamera(getCameraFacing())
        if (cameraId == -1) {
            throw IOException("Could not find the requested camera.")
        }
        val camera = Camera.open(cameraId)
        val cameraSize = selectSizePair(
            camera,
            configuration.previewWidth,
            configuration.previewHeight
        )
            ?: throw IOException("Could not find suitable preview size.")
        val pictureSize = cameraSize.pictureSize
        previewSize = cameraSize.previewSize
        val previewFpsRange = selectPreviewFpsRange(camera, configuration.fps)
            ?: throw IOException("Could not find suitable preview frames per second range.")
        val parameters = camera.parameters
        parameters.setPreviewSize(previewSize!!.width, previewSize!!.height)
        if (pictureSize != null) {
            parameters.setPictureSize(pictureSize.width, pictureSize.height)
        }
        parameters.setPreviewFpsRange(
            previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
            previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]
        )
        parameters.previewFormat = ImageFormat.NV21
        setRotation(camera, parameters, cameraId)
        if (configuration.isAutoFocus) {
            if (parameters
                    .supportedFocusModes
                    .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)
            ) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            } else {
                Log.i(TAG, "Camera auto focus is not supported on this device.")
            }
        }
        camera.parameters = parameters
        return camera
    }

    val facing: Int
        get() = getCameraFacing()

    /**
     * Calculates the correct rotation for the given camera id and sets the rotation in the
     * parameters. It also sets the camera's display orientation and rotation.
     *
     * @param parameters the camera parameters for which to set the rotation
     * @param cameraId the camera id to set rotation based on
     */
    private fun setRotation(camera: Camera, parameters: Camera.Parameters, cameraId: Int) {
        val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotations = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotations) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
            else -> Log.e(TAG, "Rotation value invaild: " + rotation)
        }
        val cameraInfo = CameraInfo()
        Camera.getCameraInfo(cameraId, cameraInfo)
        val angle: Int
        val displayAngle: Int
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            angle = (cameraInfo.orientation + degrees) % 360
            displayAngle = (360 - angle) % 360
        } else {
            angle = (cameraInfo.orientation - degrees + 360) % 360
            displayAngle = angle
        }
        rotation = angle / 90
        camera.setDisplayOrientation(displayAngle)
        parameters.setRotation(angle)
    }

    private class CameraSize internal constructor(preview: Camera.Size, picture: Camera.Size?) {
        val previewSize: Size
        var pictureSize: Size? = null

        init {
            previewSize = Size(preview.width, preview.height)
            if (picture != null) {
                pictureSize = Size(picture.width, picture.height)
            }
        }
    }

    companion object {
        private const val TAG = "CameraSelector"
        private const val ASPECT_RATIO_TOLERANCE = 0.01f

        /**
         * Selects the most suitable preview and picture size, given the desired width and height.
         *
         *
         * Even though we only need to find the preview size, it's necessary to find both the preview
         * size and the picture size of the camera together, because these need to have the same aspect
         * ratio. On some hardware, if you would only set the preview size, you will get a distorted
         * image.
         *
         * @param camera the camera to select a preview size from
         * @param desiredWidth the desired width of the camera preview frames
         * @param desiredHeight the desired height of the camera preview frames
         * @return the selected preview and picture size pair
         */
        private fun selectSizePair(
            camera: Camera,
            desiredWidth: Int,
            desiredHeight: Int
        ): CameraSize? {
            val validPreviewSizes = generateValidPreviewSizeList(camera)
            var selectedPair: CameraSize? = null
            var minDiff = Int.MAX_VALUE
            for (cameraSize in validPreviewSizes) {
                val size = cameraSize.previewSize
                val diff =
                    Math.abs(size.width - desiredWidth) + Math.abs(size.height - desiredHeight)
                if (diff < minDiff) {
                    selectedPair = cameraSize
                    minDiff = diff
                }
            }
            return selectedPair
        }

        /**
         * Selects the most suitable preview frames per second range, given the desired frames per second.
         *
         * @param camera the camera to select a frames per second range from
         * @param desiredPreviewFps the desired frames per second for the camera preview frames
         * @return the selected preview frames per second range
         */
        private fun selectPreviewFpsRange(camera: Camera, desiredPreviewFps: Float): IntArray? {
            // The camera API uses integers scaled by a factor of 1000 instead of floating-point frame
            // rates.
            val desiredPreviewFpsScaled = (desiredPreviewFps * 1000.0f).toInt()

            // The method for selecting the best range is to minimize the sum of the differences between
            // the desired value and the upper and lower bounds of the range.  This may select a range
            // that the desired value is outside of, but this is often preferred.  For example, if the
            // desired frame rate is 29.97, the range (30, 30) is probably more desirable than the
            // range (15, 30).
            var selectedFpsRange: IntArray? = null
            var minDiff = Int.MAX_VALUE
            val previewFpsRangeList = camera.parameters.supportedPreviewFpsRange
            for (range in previewFpsRangeList) {
                val deltaMin =
                    desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]
                val deltaMax =
                    desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]
                val diff = Math.abs(deltaMin) + Math.abs(deltaMax)
                if (diff < minDiff) {
                    selectedFpsRange = range
                    minDiff = diff
                }
            }
            return selectedFpsRange
        }

        /**
         * Generates a list of acceptable preview sizes. Preview sizes are not acceptable if there is not
         * a corresponding picture size of the same aspect ratio. If there is a corresponding picture size
         * of the same aspect ratio, the picture size is paired up with the preview size.
         *
         *
         * This is necessary because even if we don't use still pictures, the still picture size must
         * be set to a size that is the same aspect ratio as the preview size we choose. Otherwise, the
         * preview images may be distorted on some devices.
         */
        private fun generateValidPreviewSizeList(camera: Camera): List<CameraSize> {
            val parameters = camera.parameters
            val supportedPreviewSizes = parameters.supportedPreviewSizes
            val supportedPictureSizes = parameters.supportedPictureSizes
            val validPreviewSizes: MutableList<CameraSize> = ArrayList()
            for (previewSize in supportedPreviewSizes) {
                val previewAspectRatio = previewSize.width.toFloat() / previewSize.height.toFloat()

                // By looping through the picture sizes in order, we favor the higher resolutions.
                // We choose the highest resolution in order to support taking the full resolution
                // picture later.
                for (pictureSize in supportedPictureSizes) {
                    val pictureAspectRatio =
                        pictureSize.width.toFloat() / pictureSize.height.toFloat()
                    if (Math.abs(previewAspectRatio - pictureAspectRatio) < ASPECT_RATIO_TOLERANCE) {
                        validPreviewSizes.add(CameraSize(previewSize, pictureSize))
                        break
                    }
                }
            }
            if (validPreviewSizes.size == 0) {
                Log.w(TAG, "No preview sizes have a corresponding same-aspect-ratio picture size")
                for (previewSize in supportedPreviewSizes) {
                    // The null picture size will let us know that we shouldn't set a picture size.
                    validPreviewSizes.add(CameraSize(previewSize, null))
                }
            }
            return validPreviewSizes
        }

        /**
         * Gets the id for the camera specified by the direction it is facing. Returns -1 if no such
         * camera was found.
         *
         * @param facing the desired camera (front-facing or rear-facing)
         */
        private fun getIdForRequestedCamera(facing: Int): Int {
            val cameraInfo = CameraInfo()
            for (i in 0 until Camera.getNumberOfCameras()) {
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo.facing == facing) {
                    return i
                }
            }
            return -1
        }
    }
}