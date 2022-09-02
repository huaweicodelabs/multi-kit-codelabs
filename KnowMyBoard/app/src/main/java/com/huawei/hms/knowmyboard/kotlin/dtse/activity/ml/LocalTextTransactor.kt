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

import android.content.Context
import com.huawei.hms.knowmyboard.dtse.activity.util.MySharedPreferences.Companion.getInstance
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import com.huawei.hms.mlsdk.common.MLFrame
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.util.Log
import com.huawei.hmf.tasks.Task
import com.huawei.hms.knowmyboard.dtse.activity.util.Constant
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import java.io.IOException
import java.lang.Exception
import java.nio.ByteBuffer

class LocalTextTransactor(handler: Handler, private val mContext: Context) :
    BaseTransactor<MLText?>() {
    private val detector: MLTextAnalyzer
    var lastResults: MLText? = null
        private set
    override var transactingMetaData: FrameMetadata? = null
        set(transactingMetaData) {
            super.transactingMetaData = transactingMetaData
            field = transactingMetaData
        }
    override var transactingImage: ByteBuffer? = null
        set(transactingImage) {
            super.transactingImage = transactingImage
            field = transactingImage
        }




    private val mHandler: Handler
    var mCount = 0
    private val language: String
        private get() {
            val position = getInstance(mContext)!!.getStringValue(Constant.POSITION_KEY)
            Log.d(TAG, "position: $position")
            var language = ""
            when (position) {
                Constant.POSITION_CN -> language = "zh"
                Constant.POSITION_EN, Constant.POSITION_LA -> language = "en"
                Constant.POSITION_JA, Constant.POSITION_KO -> language = "ja"
                else -> {
                    language = if (Constant.IS_CHINESE) {
                        "zh"
                    } else {
                        "en"
                    }
                    Log.d(TAG, "default value!")
                }
            }
            return language
        }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(
                TAG,
                "Exception thrown while trying to close text transactor: " + e.message
            )
        }
    }

    override fun detectInImage(image: MLFrame?): Task<MLText?> {
        transactingImage = image!!.byteBuffer
        return detector.asyncAnalyseFrame(image)
    }

  /*  protected override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: MLText,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        lastResults = results
        transactingMetaData = frameMetadata
        graphicOverlay.clear()
        val blocks = results.blocks
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M && originalCameraImage != null) {
            val imageGraphic = CameraImageGraphic(graphicOverlay, originalCameraImage)
            graphicOverlay.addGraphic(imageGraphic)
        }
        if (blocks.size > 0) {
            mCount = 0
            mHandler.sendEmptyMessage(Constant.SHOW_TAKE_PHOTO_BUTTON)
        } else {
            mCount++
            if (mCount > 1) {
                mHandler.sendEmptyMessage(Constant.HIDE_TAKE_PHOTO_BUTTON)
            }
        }
        for (i in blocks.indices) {
            val lines = blocks[i].contents
            for (j in lines.indices) {
                // Display by line, without displaying empty lines.
                if (lines[j].stringValue != null && lines[j].stringValue.trim { it <= ' ' }.length != 0) {
                    val textGraphic: BaseGraphic = LocalTextGraphic(
                        graphicOverlay,
                        lines[j]
                    )
                    graphicOverlay.addGraphic(textGraphic)
                }
            }
        }
        graphicOverlay.postInvalidate()
    }*/


    companion object {
        private const val TAG = "TextRecProc"
    }

    init {
        val language = language
        Log.d(TAG, "language:$language")
        val options = MLLocalTextSetting.Factory()
            .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
            .setLanguage(language)
            .create()
        detector = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(options)
        mHandler = handler
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: MLText?,
        frameMetadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay?
    ) {
        //TODO("Not yet implemented")
        lastResults = results
        transactingMetaData = frameMetadata
        graphicOverlay!!.clear()
        val blocks = results?.blocks
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M && originalCameraImage != null) {
            val imageGraphic = CameraImageGraphic(graphicOverlay, originalCameraImage)
            graphicOverlay.addGraphic(imageGraphic)
        }
        if (blocks?.size!! > 0) {
            mCount = 0
            mHandler.sendEmptyMessage(Constant.SHOW_TAKE_PHOTO_BUTTON)
        } else {
            mCount++
            if (mCount > 1) {
                mHandler.sendEmptyMessage(Constant.HIDE_TAKE_PHOTO_BUTTON)
            }
        }
        for (i in blocks.indices) {
            val lines = blocks[i].contents
            for (j in lines.indices) {
                // Display by line, without displaying empty lines.
                if (lines[j].stringValue != null && lines[j].stringValue.trim { it <= ' ' }
                        .isNotEmpty()) {
                    val textGraphic: BaseGraphic = LocalTextGraphic(
                        graphicOverlay,
                        lines[j]
                    )
                    graphicOverlay!!.addGraphic(textGraphic)
                }
            }
        }
        graphicOverlay!!.postInvalidate()
    }

    override fun onFailure(e: Exception?) {
        //TODO("Not yet implemented")
    }
}