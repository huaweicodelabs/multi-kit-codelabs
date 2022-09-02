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

import com.huawei.hms.mlsdk.common.MLFrame
import android.hardware.Camera.CameraInfo
import android.app.Activity
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.nio.ByteBuffer

object BitmapUtils {
    private const val TAG = "BitmapUtils"

    /**
     * Convert nv21 format byte buffer to bitmap
     *
     * @param data data
     * @param metadata metadata
     * @return Bitmap object
     */
    @JvmStatic
    fun getBitmap(data: ByteBuffer, metadata: FrameMetadata): Bitmap? {
        data.rewind()
        val imageBuffer = ByteArray(data.limit())
        data[imageBuffer, 0, imageBuffer.size]
        try {
            val yuvImage = YuvImage(
                imageBuffer, ImageFormat.NV21, metadata.width,
                metadata.height, null
            )
            val stream = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, metadata.width, metadata.height), 80, stream)
            val bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
            stream.close()
            return rotateBitmap(bitmap, metadata.rotation, metadata.cameraFacing)
        } catch (e: Exception) {
            Log.e(TAG, "Error: " + e.message)
        }
        return null
    }

    fun rotateBitmap(bitmap: Bitmap, rotation: Int, facing: Int): Bitmap {
        val matrix = Matrix()
        var rotationDegree = 0
        if (rotation == MLFrame.SCREEN_SECOND_QUADRANT) {
            rotationDegree = 90
        } else if (rotation == MLFrame.SCREEN_THIRD_QUADRANT) {
            rotationDegree = 180
        } else if (rotation == MLFrame.SCREEN_FOURTH_QUADRANT) {
            rotationDegree = 270
        }
        matrix.postRotate(rotationDegree.toFloat())
        if (facing != CameraInfo.CAMERA_FACING_BACK) {
            matrix.postScale(-1.0f, 1.0f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

/*    fun recycleBitmap(vararg bitmaps: Bitmap?) {
        for (bitmap in bitmaps) {
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
                bitmap = null
            }
        }
    }*/

    private fun getImagePath(activity: Activity, uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity.managedQuery(uri, projection, null, null, null)
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }

    fun loadFromPath(activity: Activity, uri: Uri, width: Int, height: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val path = getImagePath(activity, uri)
        BitmapFactory.decodeFile(path, options)
        val sampleSize = calculateInSampleSize(options, width, height)
        options.inSampleSize = sampleSize
        options.inJustDecodeBounds = false
        val bitmap = zoomImage(BitmapFactory.decodeFile(path, options), width, height)
        return rotateBitmap(bitmap, getRotationAngle(path))
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            // Calculate height and required height scale.
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            // Calculate width and required width scale.
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            // Take the larger of the values.
            inSampleSize = if (heightRatio > widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    // Scale pictures to screen width.
    private fun zoomImage(imageBitmap: Bitmap, targetWidth: Int, maxHeight: Int): Bitmap {
        val scaleFactor = Math.max(
            imageBitmap.width.toFloat() / targetWidth.toFloat(),
            imageBitmap.height.toFloat() / maxHeight.toFloat()
        )
        return Bitmap.createScaledBitmap(
            imageBitmap,
            (imageBitmap.width / scaleFactor).toInt(),
            (imageBitmap.height / scaleFactor).toInt(),
            true
        )
    }

    /**
     * Get the rotation angle of the photo.
     *
     * @param path photo path.
     * @return angle.
     */
    fun getRotationAngle(path: String?): Int {
        var rotation = 0
        try {
            val exifInterface = ExifInterface(path!!)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to get rotation: " + e.message)
        }
        return rotation
    }

    fun rotateBitmap(bitmap: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Failed to rotate bitmap: " + e.message)
        }
        return result ?: bitmap
    }
}