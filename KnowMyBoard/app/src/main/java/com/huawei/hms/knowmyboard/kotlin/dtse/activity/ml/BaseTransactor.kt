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
 * 2020.2.21-Changed name from VisionProcessorBase to BaseTransactor.
 * 2020.2.21-Deleted method: process(Bitmap bitmap, GraphicOverlay graphicOverlay,
 * String path,boolean flag);
 * process(Bitmap bitmap, GraphicOverlay graphicOverlay,String path);
 * onSuccess(
 * @Nullable Bitmap originalCameraImage,
 * @NonNull T results,
 * @NonNull FrameMetadata frameMetadata,
 * @NonNull GraphicOverlay graphicOverlay, String path, boolean flag);
 * onSuccess(
 * @Nullable Bitmap originalCameraImage,
 * @NonNull T results,
 * @NonNull FrameMetadata frameMetadata,
 * @NonNull GraphicOverlay graphicOverlay, String path);
 * writeFileSdcard(String message);
 * Huawei Technologies Co., Ltd.
 */
package com.huawei.hms.knowmyboard.dtse.activity.ml

import com.huawei.hms.knowmyboard.dtse.activity.ml.ImageTransactor
import com.huawei.hms.knowmyboard.dtse.activity.ml.FrameMetadata
import kotlin.jvm.Synchronized
import com.huawei.hms.knowmyboard.dtse.activity.ml.GraphicOverlay
import android.graphics.Bitmap
import com.huawei.hms.mlsdk.common.MLFrame
import android.graphics.ImageFormat
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.knowmyboard.dtse.activity.ml.CameraConfiguration
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.Task
import java.lang.Exception
import java.nio.ByteBuffer

abstract class BaseTransactor<T> : ImageTransactor {
    // To keep the latest images and its metadata.
    private var latestImage: ByteBuffer? = null
    private var latestImageMetaData: FrameMetadata? = null

    // To keep the images and metadata in process.
    open var transactingImage: ByteBuffer? = null
    open var transactingMetaData: FrameMetadata? = null
    override fun process(
        data: ByteBuffer?,
        frameMetadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay?
    ) {
        //TODO("Not yet implemented")
        latestImage = data
        latestImageMetaData = frameMetadata
        if (transactingImage == null && transactingMetaData == null) {
            processLatestImage(graphicOverlay!!)
        }
    }

    override fun process(bitmap: Bitmap?, graphicOverlay: GraphicOverlay?) {
        val frame = MLFrame.Creator().setBitmap(bitmap).create()
        detectInVisionImage(bitmap, frame, null, graphicOverlay!!)
    }
    /*  @Synchronized
    override fun process(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (transactingImage == null && transactingMetaData == null) {
            processLatestImage(graphicOverlay)
        }
    }

    override fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay) {
        val frame = MLFrame.Creator().setBitmap(bitmap).create()
        detectInVisionImage(bitmap, frame, null, graphicOverlay)
    }*/

    @Synchronized
    private fun processLatestImage(graphicOverlay: GraphicOverlay) {
        transactingImage = latestImage
        transactingMetaData = latestImageMetaData
        latestImage = null
        latestImageMetaData = null
        if (transactingImage != null && transactingMetaData != null) {
            val width: Int
            val height: Int
            width = transactingMetaData!!.width
            height = transactingMetaData!!.height
            val metadata = MLFrame.Property.Creator()
                .setFormatType(ImageFormat.NV21)
                .setWidth(width)
                .setHeight(height)
                .setQuadrant(transactingMetaData!!.rotation)
                .create()
            val bitmap = BitmapUtils.getBitmap(
                transactingImage!!, transactingMetaData!!
            )
            detectInVisionImage(
                bitmap,
                MLFrame.fromByteBuffer(transactingImage, metadata),
                transactingMetaData,
                graphicOverlay
            )
        }
    }

    private fun detectInVisionImage(
        bitmap: Bitmap?, image: MLFrame, metadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay
    ) {
        detectInImage(image)
            .addOnSuccessListener { results ->
                if (metadata == null || metadata.cameraFacing == CameraConfiguration.getCameraFacing()) {
                    this@BaseTransactor.onSuccess(bitmap, results, metadata, graphicOverlay)
                }
                processLatestImage(graphicOverlay)
            }
            .addOnFailureListener { e -> this@BaseTransactor.onFailure(e) }
    }

    override fun stop() {}

    /**
     * detect
     *
     * @param image image
     * @return Task that encapsulate results
     */
    protected abstract fun detectInImage(image: MLFrame?): Task<T>

    /**
     * Callback that executes with a successful detection result.
     *
     * @param originalCameraImage hold the original image from camera, used to draw the background
     * image.
     * @param results             results
     * @param frameMetadata       metadata
     * @param graphicOverlay      graphicOverlay
     */
    protected abstract fun onSuccess(
        originalCameraImage: Bitmap?,
        results: T,
        frameMetadata: FrameMetadata?, graphicOverlay: GraphicOverlay?
    )

    /**
     * Callback that executes with a failed detection result.
     *
     * @param e exception
     */
    protected abstract fun onFailure(e: Exception?)
}