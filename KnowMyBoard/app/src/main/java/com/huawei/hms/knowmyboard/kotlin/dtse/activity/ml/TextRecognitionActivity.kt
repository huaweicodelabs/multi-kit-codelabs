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

import android.app.Dialog
import android.content.res.Configuration
import com.huawei.hms.knowmyboard.dtse.activity.util.MySharedPreferences.Companion.getInstance
import com.huawei.hms.knowmyboard.dtse.activity.ml.BitmapUtils.getBitmap
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.Camera
import android.widget.TextView
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.WindowManager
import android.view.Gravity
import android.view.View
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import android.widget.Toast
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.langdetect.MLLangDetectorFactory
import com.huawei.hms.mlsdk.langdetect.local.MLLocalLangDetectorSetting
import com.huawei.hms.knowmyboard.dtse.R
import com.huawei.hms.knowmyboard.dtse.activity.util.Constant
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslateSetting
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.model.download.MLModelDownloadStrategy
import com.huawei.hms.mlsdk.model.download.MLModelDownloadListener
import java.io.IOException
import java.lang.Exception
import java.lang.ref.WeakReference

class TextRecognitionActivity : BaseActivity(), OnRequestPermissionsResultCallback,
    View.OnClickListener {
    private var lensEngine: LensEngine? = null
    private var preview: LensEnginePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var takePicture: ImageButton? = null
    private var imageSwitch: ImageButton? = null
    private var zoomImageLayout: RelativeLayout? = null
    private var zoomImageView: ZoomImageView? = null
    private var zoomImageClose: ImageButton? = null
    var cameraConfiguration: CameraConfiguration? = null
    private var facing = CameraConfiguration.CAMERA_FACING_BACK
    private var mCamera: Camera? = null
    private var isLandScape = false
    private var bitmap: Bitmap? = null
    private var bitmapCopy: Bitmap? = null
    private var localTextTransactor: LocalTextTransactor? = null
    private val mHandler: Handler = MsgHandler(this)
    private var languageDialog: Dialog? = null
    private var textCN: TextView? = null
    private var textEN: TextView? = null
    private var textJN: TextView? = null
    private var textKN: TextView? = null
    private var textLN: TextView? = null
    private var tv_translated_txt: TextView? = null
    private var textType: String? = Constant.POSITION_CN
    private var isInitialization = false
    var analyzer: MLTextAnalyzer? = null

    private class MsgHandler(mainActivity: TextRecognitionActivity) : Handler() {
        var mMainActivityWeakReference: WeakReference<TextRecognitionActivity>
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val mainActivity = mMainActivityWeakReference.get() ?: return
            if (msg.what == Constant.SHOW_TAKE_PHOTO_BUTTON) {
                mainActivity.setVisible()
            } else if (msg.what == Constant.HIDE_TAKE_PHOTO_BUTTON) {
                mainActivity.setGone()
            }
        }

        init {
            mMainActivityWeakReference = WeakReference(mainActivity)
        }
    }

    private fun setVisible() {
        if (takePicture!!.visibility == View.GONE) {
            takePicture!!.visibility = View.VISIBLE
        }
    }

    private fun setGone() {
        if (takePicture!!.visibility == View.VISIBLE) {
            takePicture!!.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.layout_text_recognize)
        if (savedInstanceState != null) {
            facing = savedInstanceState.getInt(Constant.CAMERA_FACING)
        }
        tv_translated_txt = findViewById(R.id.tv_translated_txt)
        preview = findViewById(R.id.live_preview)
        graphicOverlay = findViewById(R.id.live_overlay)
        cameraConfiguration = CameraConfiguration()
        cameraConfiguration!!.setCameraFacing(facing)
        initViews()
        isLandScape =
            this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        createLensEngine()
        setStatusBar()
    }

    private fun initViews() {
        takePicture = findViewById(R.id.takePicture)
        takePicture?.setOnClickListener(this)
        imageSwitch = findViewById(R.id.text_imageSwitch)
        imageSwitch?.setOnClickListener(this)
        zoomImageLayout = findViewById(R.id.zoomImageLayout)
        zoomImageView = findViewById(R.id.take_picture_overlay)
        zoomImageClose = findViewById(R.id.zoomImageClose)
        zoomImageClose?.setOnClickListener(this)
        findViewById<View>(R.id.back).setOnClickListener(this)
        findViewById<View>(R.id.language_setting).setOnClickListener(this)
        createLanguageDialog()
    }

    override fun onClick(view: View) {
        if (view.id == R.id.takePicture) {
            takePicture()
        } else if (view.id == R.id.zoomImageClose) {
            tv_translated_txt!!.text = ""
            zoomImageLayout!!.visibility = View.GONE
            recycleBitmap()
        } else if (view.id == R.id.language_setting) {
            showLanguageDialog()
        } else if (view.id == R.id.simple_cn) {
            getInstance(this)
                ?.putStringValue(Constant.POSITION_KEY, Constant.POSITION_CN)
            languageDialog!!.dismiss()
            restartLensEngine(Constant.POSITION_CN)
        } else if (view.id == R.id.english) {
            getInstance(this)
                ?.putStringValue(Constant.POSITION_KEY, Constant.POSITION_EN)
            languageDialog!!.dismiss()
            preview!!.release()
            restartLensEngine(Constant.POSITION_EN)
        } else if (view.id == R.id.japanese) {
            getInstance(this)
                ?.putStringValue(Constant.POSITION_KEY, Constant.POSITION_JA)
            languageDialog!!.dismiss()
            preview!!.release()
            restartLensEngine(Constant.POSITION_JA)
        } else if (view.id == R.id.korean) {
            getInstance(this)
                ?.putStringValue(Constant.POSITION_KEY, Constant.POSITION_KO)
            languageDialog!!.dismiss()
            preview!!.release()
            restartLensEngine(Constant.POSITION_KO)
        } else if (view.id == R.id.latin) {
            getInstance(this)
                ?.putStringValue(Constant.POSITION_KEY, Constant.POSITION_LA)
            languageDialog!!.dismiss()
            preview!!.release()
            restartLensEngine(Constant.POSITION_LA)
        } else if (view.id == R.id.back) {
            releaseLensEngine()
            finish()
        }
    }

    private fun restartLensEngine(type: String) {
        if (textType == type) {
            return
        }
        lensEngine!!.release()
        lensEngine = null
        createLensEngine()
        startLensEngine()
        if (lensEngine == null || lensEngine?.camera == null) {
            return
        }
        mCamera = lensEngine?.camera
        try {
            mCamera!!.setPreviewDisplay(preview!!.surfaceHolder)
        } catch (e: IOException) {
            Log.d(TAG, "initViews IOException")
        }
    }

    override fun onBackPressed() {
        if (zoomImageLayout!!.visibility == View.VISIBLE) {
            zoomImageLayout!!.visibility = View.GONE
            recycleBitmap()
        } else {
            super.onBackPressed()
            releaseLensEngine()
        }
    }

    private fun createLanguageDialog() {
        languageDialog = Dialog(this, R.style.MyDialogStyle)
        val view = View.inflate(this, R.layout.dialog_language_setting, null)
        // Set up a custom layout
        languageDialog!!.setContentView(view)
        textCN = view.findViewById(R.id.simple_cn)
        textCN?.setOnClickListener(this)
        textEN = view.findViewById(R.id.english)
        textEN?.setOnClickListener(this)
        textJN = view.findViewById(R.id.japanese)
        textJN?.setOnClickListener(this)
        textKN = view.findViewById(R.id.korean)
        textKN?.setOnClickListener(this)
        textLN = view.findViewById(R.id.latin)
        textLN?.setOnClickListener(this)
        languageDialog!!.setCanceledOnTouchOutside(true)
        // Set the size of the dialog
        val dialogWindow = languageDialog!!.window
        val layoutParams = dialogWindow!!.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.BOTTOM
        dialogWindow.attributes = layoutParams
    }

    private fun showLanguageDialog() {
        initDialogViews()
        languageDialog!!.show()
    }

    private fun initDialogViews() {
        val position = getInstance(this)!!.getStringValue(Constant.POSITION_KEY)
        textType = position
        textCN!!.isSelected = false
        textEN!!.isSelected = false
        textJN!!.isSelected = false
        textLN!!.isSelected = false
        textKN!!.isSelected = false
        when (position) {
            Constant.POSITION_CN -> textCN!!.isSelected = true
            Constant.POSITION_EN -> textEN!!.isSelected = true
            Constant.POSITION_LA -> textLN!!.isSelected = true
            Constant.POSITION_JA -> textJN!!.isSelected = true
            Constant.POSITION_KO -> textKN!!.isSelected = true
            else -> {}
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(Constant.CAMERA_FACING, facing)
        super.onSaveInstanceState(outState)
    }

    private fun createLensEngine() {
        val setting = MLLocalTextSetting.Factory()
            .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE) // Specify languages that can be recognized.
            .setLanguage("ko")
            .create()
        analyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting)
        //analyzer = new MLTextAnalyzer.Factory(this).create();
        if (lensEngine == null) {
            lensEngine =
                LensEngine(this@TextRecognitionActivity, cameraConfiguration, graphicOverlay)
        }
        try {
            localTextTransactor = LocalTextTransactor(mHandler, this)
            lensEngine!!.setMachineLearningFrameTransactor(localTextTransactor as ImageTransactor?)
            // this.lensEngine.setMachineLearningFrameTransactor((ImageTransactor) new ObjectAnalyzerTransactor());
            isInitialization = true
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Can not create image transactor: " + e.message,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    private fun startLensEngine() {
        if (lensEngine != null) {
            try {
                preview!!.start(lensEngine, false)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start lensEngine.", e)
                lensEngine!!.release()
                lensEngine = null
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if (!isInitialization) {
            createLensEngine()
        }
        startLensEngine()
    }

    override fun onStop() {
        super.onStop()
        preview!!.stop()
    }

    private fun releaseLensEngine() {
        if (lensEngine != null) {
            lensEngine!!.release()
            lensEngine = null
        }
        recycleBitmap()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseLensEngine()
        if (analyzer != null) {
            try {
                analyzer!!.stop()
            } catch (e: IOException) {
                // Exception handling.
                Log.e(TAG, "Error while releasing analyzer")
            }
        }
    }

    private fun recycleBitmap() {
        if (bitmap != null && !bitmap!!.isRecycled) {
            bitmap!!.recycle()
            bitmap = null
        }
        if (bitmapCopy != null && !bitmapCopy!!.isRecycled) {
            bitmapCopy!!.recycle()
            bitmapCopy = null
        }
    }

    private fun takePicture() {
        zoomImageLayout!!.visibility = View.VISIBLE
        val localDataProcessor = LocalDataProcessor()
        localDataProcessor.setLandScape(isLandScape)
        bitmap = getBitmap(
            localTextTransactor!!.transactingImage!!, localTextTransactor!!.transactingMetaData!!
        )
        var previewWidth =
            localDataProcessor.getMaxWidthOfImage(localTextTransactor!!.transactingMetaData!!)!!
                .toFloat()
        var previewHeight =
            localDataProcessor.getMaxHeightOfImage(localTextTransactor!!.transactingMetaData!!)!!
                .toFloat()
        if (isLandScape) {
            previewWidth =
                localDataProcessor.getMaxHeightOfImage(localTextTransactor!!.transactingMetaData!!)!!
                    .toFloat()
            previewHeight =
                localDataProcessor.getMaxWidthOfImage(localTextTransactor!!.transactingMetaData!!)!!
                    .toFloat()
        }
        bitmapCopy = Bitmap.createBitmap(bitmap!!).copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmapCopy!!)
        val min = Math.min(previewWidth, previewHeight)
        val max = Math.max(previewWidth, previewHeight)
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            localDataProcessor.setCameraInfo(graphicOverlay!!, canvas, min, max)
        } else {
            localDataProcessor.setCameraInfo(graphicOverlay!!, canvas, max, min)
        }
        localDataProcessor.drawHmsMLVisionText(canvas, localTextTransactor!!.lastResults!!.blocks)
        zoomImageView!!.setImageBitmap(bitmapCopy!!)
        // Create an MLFrame object using the bitmap, which is the image data in bitmap format.
        val frame = MLFrame.fromBitmap(bitmap)
        val task = analyzer!!.asyncAnalyseFrame(frame)
        task.addOnSuccessListener { text ->
            val detectText = text.stringValue
            // Processing for successful recognition.
            // Create a local language detector.
            val factory = MLLangDetectorFactory.getInstance()
            val setting =
                MLLocalLangDetectorSetting.Factory() // Set the minimum confidence threshold for language detection.
                    .setTrustedThreshold(0.01f)
                    .create()
            val myLocalLangDetector = factory.getLocalLangDetector(setting)
            val firstBestDetectTask = myLocalLangDetector.firstBestDetect(detectText)
            firstBestDetectTask.addOnSuccessListener { languageDetected -> // Processing logic for detection success.
                Log.d("TAG", "Lang detect :$languageDetected")
                Log.d("TAG", " detectText :$detectText")
                translate(languageDetected, detectText)
            }.addOnFailureListener { e -> // Processing logic for detection failure.
                Log.e("TAG", "Lang detect error:" + e.message)
                Toast.makeText(
                    this@TextRecognitionActivity,
                    "Language detection error.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener { // Processing logic for recognition failure.
            Log.e("TAG", " Text : Processing logic for recognition failure")
        }
    }

    private fun translate(languageDetected: String, detectText: String) {
        MLApplication.initialize(application)
        MLApplication.getInstance().apiKey =
            "DAEDAF48ZIMI4ettQdTfCKlXgaln/E+TO/PrsX+LpP2BubkmED/iC0iVEps5vfx1ol27rHvuwiq64YphpPkGYWbf9La8XjnvC9qhwQ=="

        // Create an offline translator.
        val setting =
            MLLocalTranslateSetting.Factory() // Set the source language code. The ISO 639-1 standard is used. This parameter is mandatory. If this parameter is not set, an error may occur.
                .setSourceLangCode(languageDetected) // Set the target language code. The ISO 639-1 standard is used. This parameter is mandatory. If this parameter is not set, an error may occur.
                .setTargetLangCode("en")
                .create()
        val myLocalTranslator = MLTranslatorFactory.getInstance().getLocalTranslator(setting)
        // Set the model download policy.
        val downloadStrategy = MLModelDownloadStrategy.Factory()
            .needWifi() // It is recommended that you download the package in a Wi-Fi environment.
            .create()
        // Create a download progress listener.
        val modelDownloadListener = MLModelDownloadListener { alreadyDownLength, totalLength ->
            runOnUiThread {
                // Display the download progress or perform other operations.
            }
        }
        myLocalTranslator.preparedModel(downloadStrategy, modelDownloadListener)
            .addOnSuccessListener {
                // Called when the model package is successfully downloaded.
                // input is a string of less than 5000 characters.
                val task = myLocalTranslator.asyncTranslate(detectText)
                // Before translation, ensure that the models have been successfully downloaded.
                task.addOnSuccessListener { translated -> // Processing logic for detection success.
                    Log.e("TAG", " Translated Text : $translated")
                    tv_translated_txt!!.text = translated
                }.addOnFailureListener { e -> // Processing logic for detection failure.
                    Log.e("TAG", " Translation failed " + e.message)
                    Toast.makeText(
                        this@TextRecognitionActivity,
                        "Please check internet connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { e -> // Called when the model package fails to be downloaded.
            Log.e("TAG", " Translation failed onFailure " + e.message)
        }
    }

    private fun getStringResourceByName(aString: String): String {
        return try {
            val packageName = packageName
            val resId = resources
                .getIdentifier(aString, "string", packageName)
            if (resId == 0) {
                aString
            } else {
                getString(resId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            aString
        }
    }

    companion object {
        private const val TAG = "TextRecognitionActivity"
    }
}