/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
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

package com.huawei.hms.knowmyboard.dtse.activity.ml;

import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.knowmyboard.dtse.R;
import com.huawei.hms.knowmyboard.dtse.activity.util.Constant;
import com.huawei.hms.knowmyboard.dtse.activity.util.MySharedPreferences;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.langdetect.MLLangDetectorFactory;
import com.huawei.hms.mlsdk.langdetect.local.MLLocalLangDetector;
import com.huawei.hms.mlsdk.langdetect.local.MLLocalLangDetectorSetting;
import com.huawei.hms.mlsdk.model.download.MLModelDownloadListener;
import com.huawei.hms.mlsdk.model.download.MLModelDownloadStrategy;
import com.huawei.hms.mlsdk.text.MLLocalTextSetting;
import com.huawei.hms.mlsdk.text.MLText;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory;
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslateSetting;
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslator;

import java.io.IOException;
import java.lang.ref.WeakReference;

public final class TextRecognitionActivity extends BaseActivity
        implements OnRequestPermissionsResultCallback, View.OnClickListener {
    private static final String TAG = "TextRecognitionActivity";
    private LensEngine lensEngine = null;
    private LensEnginePreview preview;
    private GraphicOverlay graphicOverlay;
    private ImageButton takePicture;
    private ImageButton imageSwitch;
    private RelativeLayout zoomImageLayout;
    private ZoomImageView zoomImageView;
    private ImageButton zoomImageClose;
    CameraConfiguration cameraConfiguration = null;
    private int facing = CameraConfiguration.CAMERA_FACING_BACK;
    private Camera mCamera;
    private boolean isLandScape;
    private Bitmap bitmap;
    private Bitmap bitmapCopy;
    private LocalTextTransactor localTextTransactor;
    private Handler mHandler = new MsgHandler(this);
    private Dialog languageDialog;

    private TextView textCN;
    private TextView textEN;
    private TextView textJN;
    private TextView textKN;
    private TextView textLN;
    private TextView tv_translated_txt;

    private String textType = Constant.POSITION_CN;
    private boolean isInitialization = false;
    MLTextAnalyzer analyzer;

    private static class MsgHandler extends Handler {
        WeakReference<TextRecognitionActivity> mMainActivityWeakReference;

        public MsgHandler(TextRecognitionActivity mainActivity) {
            this.mMainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TextRecognitionActivity mainActivity = this.mMainActivityWeakReference.get();
            if (mainActivity == null) {
                return;
            }

            if (msg.what == Constant.SHOW_TAKE_PHOTO_BUTTON) {
                mainActivity.setVisible();

            } else if (msg.what == Constant.HIDE_TAKE_PHOTO_BUTTON) {
                mainActivity.setGone();
            }
        }
    }

    private void setVisible() {
        if (this.takePicture.getVisibility() == View.GONE) {
            this.takePicture.setVisibility(View.VISIBLE);
        }
    }

    private void setGone() {
        if (this.takePicture.getVisibility() == View.VISIBLE) {
            this.takePicture.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.layout_text_recognize);
        if (savedInstanceState != null) {
            this.facing = savedInstanceState.getInt(Constant.CAMERA_FACING);
        }

        this.tv_translated_txt = this.findViewById(R.id.tv_translated_txt);
        this.preview = this.findViewById(R.id.live_preview);
        this.graphicOverlay = this.findViewById(R.id.live_overlay);
        this.cameraConfiguration = new CameraConfiguration();
        this.cameraConfiguration.setCameraFacing(this.facing);
        this.initViews();
        this.isLandScape = (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        this.createLensEngine();
        this.setStatusBar();
    }

    private void initViews() {
        this.takePicture = this.findViewById(R.id.takePicture);
        this.takePicture.setOnClickListener(this);
        this.imageSwitch = this.findViewById(R.id.text_imageSwitch);
        this.imageSwitch.setOnClickListener(this);
        this.zoomImageLayout = this.findViewById(R.id.zoomImageLayout);
        this.zoomImageView = this.findViewById(R.id.take_picture_overlay);
        this.zoomImageClose = this.findViewById(R.id.zoomImageClose);
        this.zoomImageClose.setOnClickListener(this);
        this.findViewById(R.id.back).setOnClickListener(this);
        this.findViewById(R.id.language_setting).setOnClickListener(this);
        this.createLanguageDialog();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.takePicture) {
            this.takePicture();
        } else if (view.getId() == R.id.zoomImageClose) {
            tv_translated_txt.setText("");
            this.zoomImageLayout.setVisibility(View.GONE);
            this.recycleBitmap();
        } else if (view.getId() == R.id.language_setting) {
            this.showLanguageDialog();
        } else if (view.getId() == R.id.simple_cn) {
            MySharedPreferences.getInstance(this).putStringValue(Constant.POSITION_KEY, Constant.POSITION_CN);
            this.languageDialog.dismiss();
            this.restartLensEngine(Constant.POSITION_CN);
        } else if (view.getId() == R.id.english) {
            MySharedPreferences.getInstance(this).putStringValue(Constant.POSITION_KEY, Constant.POSITION_EN);
            this.languageDialog.dismiss();
            this.preview.release();
            this.restartLensEngine(Constant.POSITION_EN);
        } else if (view.getId() == R.id.japanese) {
            MySharedPreferences.getInstance(this).putStringValue(Constant.POSITION_KEY, Constant.POSITION_JA);
            this.languageDialog.dismiss();
            this.preview.release();
            this.restartLensEngine(Constant.POSITION_JA);
        } else if (view.getId() == R.id.korean) {
            MySharedPreferences.getInstance(this).putStringValue(Constant.POSITION_KEY, Constant.POSITION_KO);
            this.languageDialog.dismiss();
            this.preview.release();
            this.restartLensEngine(Constant.POSITION_KO);
        } else if (view.getId() == R.id.latin) {
            MySharedPreferences.getInstance(this).putStringValue(Constant.POSITION_KEY, Constant.POSITION_LA);
            this.languageDialog.dismiss();
            this.preview.release();
            this.restartLensEngine(Constant.POSITION_LA);
        } else if (view.getId() == R.id.back) {
            releaseLensEngine();
            this.finish();
        }
    }

    private void restartLensEngine(String type) {
        if (this.textType.equals(type)) {
            return;
        }
        this.lensEngine.release();
        this.lensEngine = null;
        this.createLensEngine();
        this.startLensEngine();
        if (this.lensEngine == null || this.lensEngine.getCamera() == null) {
            return;
        }
        this.mCamera = this.lensEngine.getCamera();
        try {
            this.mCamera.setPreviewDisplay(this.preview.getSurfaceHolder());
        } catch (IOException e) {
            Log.d(TextRecognitionActivity.TAG, "initViews IOException");
        }
    }

    @Override
    public void onBackPressed() {
        if (this.zoomImageLayout.getVisibility() == View.VISIBLE) {
            this.zoomImageLayout.setVisibility(View.GONE);
            this.recycleBitmap();
        } else {
            super.onBackPressed();
            releaseLensEngine();
        }
    }

    private void createLanguageDialog() {
        this.languageDialog = new Dialog(this, R.style.MyDialogStyle);
        View view = View.inflate(this, R.layout.dialog_language_setting, null);
        // Set up a custom layout
        this.languageDialog.setContentView(view);
        this.textCN = view.findViewById(R.id.simple_cn);
        this.textCN.setOnClickListener(this);
        this.textEN = view.findViewById(R.id.english);
        this.textEN.setOnClickListener(this);
        this.textJN = view.findViewById(R.id.japanese);
        this.textJN.setOnClickListener(this);
        this.textKN = view.findViewById(R.id.korean);
        this.textKN.setOnClickListener(this);
        this.textLN = view.findViewById(R.id.latin);
        this.textLN.setOnClickListener(this);
        this.languageDialog.setCanceledOnTouchOutside(true);
        // Set the size of the dialog
        Window dialogWindow = this.languageDialog.getWindow();
        WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        dialogWindow.setAttributes(layoutParams);
    }

    private void showLanguageDialog() {
        this.initDialogViews();
        this.languageDialog.show();
    }

    private void initDialogViews() {
        String position = MySharedPreferences.getInstance(this).getStringValue(Constant.POSITION_KEY);
        this.textType = position;
        this.textCN.setSelected(false);
        this.textEN.setSelected(false);
        this.textJN.setSelected(false);
        this.textLN.setSelected(false);
        this.textKN.setSelected(false);
        switch (position) {
            case Constant.POSITION_CN:
                this.textCN.setSelected(true);
                break;
            case Constant.POSITION_EN:
                this.textEN.setSelected(true);
                break;
            case Constant.POSITION_LA:
                this.textLN.setSelected(true);
                break;
            case Constant.POSITION_JA:
                this.textJN.setSelected(true);
                break;
            case Constant.POSITION_KO:
                this.textKN.setSelected(true);
                break;
            default:
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constant.CAMERA_FACING, this.facing);
        super.onSaveInstanceState(outState);
    }

    private void createLensEngine() {
        MLLocalTextSetting setting =
                new MLLocalTextSetting.Factory()
                        .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
                        // Specify languages that can be recognized.
                        .setLanguage("ko")
                        .create();
        analyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting);

        if (this.lensEngine == null) {
            this.lensEngine =
                    new LensEngine(TextRecognitionActivity.this, this.cameraConfiguration, this.graphicOverlay);
        }
        try {
            this.localTextTransactor = new LocalTextTransactor(this.mHandler, this);
            this.lensEngine.setMachineLearningFrameTransactor((ImageTransactor) this.localTextTransactor);
            isInitialization = true;
        } catch (Exception e) {
            Toast.makeText(this, "Can not create image transactor: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startLensEngine() {
        if (this.lensEngine != null) {
            try {
                this.preview.start(this.lensEngine, false);
            } catch (IOException e) {
                Log.e(TextRecognitionActivity.TAG, "Unable to start lensEngine.", e);
                this.lensEngine.release();
                this.lensEngine = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isInitialization) {
            createLensEngine();
        }
        this.startLensEngine();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.preview.stop();
    }

    private void releaseLensEngine() {
        if (this.lensEngine != null) {
            this.lensEngine.release();
            this.lensEngine = null;
        }
        recycleBitmap();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseLensEngine();
        if (analyzer != null) {
            try {
                analyzer.stop();
            } catch (IOException e) {
                // Exception handling.
                Log.e(TAG, "Error while releasing analyzer");
            }
        }
    }

    private void recycleBitmap() {
        if (this.bitmap != null && !this.bitmap.isRecycled()) {
            this.bitmap.recycle();
            this.bitmap = null;
        }
        if (this.bitmapCopy != null && !this.bitmapCopy.isRecycled()) {
            this.bitmapCopy.recycle();
            this.bitmapCopy = null;
        }
    }

    private void takePicture() {
        this.zoomImageLayout.setVisibility(View.VISIBLE);
        LocalDataProcessor localDataProcessor = new LocalDataProcessor();
        localDataProcessor.setLandScape(this.isLandScape);
        this.bitmap =
                BitmapUtils.getBitmap(
                        this.localTextTransactor.getTransactingImage(),
                        this.localTextTransactor.getTransactingMetaData());

        float previewWidth = localDataProcessor.getMaxWidthOfImage(this.localTextTransactor.getTransactingMetaData());
        float previewHeight = localDataProcessor.getMaxHeightOfImage(this.localTextTransactor.getTransactingMetaData());
        if (this.isLandScape) {
            previewWidth = localDataProcessor.getMaxHeightOfImage(this.localTextTransactor.getTransactingMetaData());
            previewHeight = localDataProcessor.getMaxWidthOfImage(this.localTextTransactor.getTransactingMetaData());
        }
        this.bitmapCopy = Bitmap.createBitmap(this.bitmap).copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(this.bitmapCopy);
        float min = Math.min(previewWidth, previewHeight);
        float max = Math.max(previewWidth, previewHeight);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            localDataProcessor.setCameraInfo(this.graphicOverlay, canvas, min, max);
        } else {
            localDataProcessor.setCameraInfo(this.graphicOverlay, canvas, max, min);
        }
        localDataProcessor.drawHmsMLVisionText(canvas, this.localTextTransactor.getLastResults().getBlocks());
        this.zoomImageView.setImageBitmap(this.bitmapCopy);
        // Create an MLFrame object using the bitmap, which is the image data in bitmap format.
        MLFrame frame = MLFrame.fromBitmap(bitmap);
        Task<MLText> task = analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(
                        new OnSuccessListener<MLText>() {
                            @Override
                            public void onSuccess(MLText text) {
                                String detectText = text.getStringValue();
                                // Processing for successful recognition.
                                // Create a local language detector.
                                MLLangDetectorFactory factory = MLLangDetectorFactory.getInstance();
                                MLLocalLangDetectorSetting setting =
                                        new MLLocalLangDetectorSetting.Factory()
                                                // Set the minimum confidence threshold for language detection.
                                                .setTrustedThreshold(0.01f)
                                                .create();
                                MLLocalLangDetector myLocalLangDetector = factory.getLocalLangDetector(setting);
                                Task<String> firstBestDetectTask = myLocalLangDetector.firstBestDetect(detectText);
                                firstBestDetectTask
                                        .addOnSuccessListener(
                                                new OnSuccessListener<String>() {
                                                    @Override
                                                    public void onSuccess(String languageDetected) {
                                                        // Processing logic for detection success.
                                                        Log.d("TAG", "Lang detect :" + languageDetected);
                                                        Log.d("TAG", " detectText :" + detectText);

                                                        translate(languageDetected, detectText);
                                                    }
                                                })
                                        .addOnFailureListener(
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        // Processing logic for detection failure.
                                                        Log.e("TAG", "Lang detect error:" + e.getMessage());
                                                        Toast.makeText(
                                                                        TextRecognitionActivity.this,
                                                                        "Language detection error.",
                                                                        Toast.LENGTH_SHORT)
                                                                .show();
                                                    }
                                                });
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                // Processing logic for recognition failure.
                                Log.e("TAG", " Text : Processing logic for recognition failure");
                            }
                        });
    }

    private void translate(String languageDetected, String detectText) {
        MLApplication.initialize(getApplication());
        MLApplication.getInstance()
                .setApiKey(
                        "DAEDAF48ZIMI4ettQdTfCKlXgaln/E+TO/PrsX+LpP2BubkmED/iC0iVEps5vfx1ol27rHvuwiq64YphpPkGYWbf9La8XjnvC9qhwQ==");

        // Create an offline translator.
        MLLocalTranslateSetting setting =
                new MLLocalTranslateSetting.Factory()
                        // Set the source language code. The ISO 639-1 standard is used. This parameter is mandatory. If
                        // this parameter is not set, an error may occur.
                        .setSourceLangCode(languageDetected)
                        // Set the target language code. The ISO 639-1 standard is used. This parameter is mandatory. If
                        // this parameter is not set, an error may occur.
                        .setTargetLangCode("en")
                        .create();

        MLLocalTranslator myLocalTranslator = MLTranslatorFactory.getInstance().getLocalTranslator(setting);
        // Set the model download policy.
        MLModelDownloadStrategy downloadStrategy =
                new MLModelDownloadStrategy.Factory()
                        .needWifi() // It is recommended that you download the package in a Wi-Fi environment.
                        .create();
        // Create a download progress listener.
        MLModelDownloadListener modelDownloadListener =
                new MLModelDownloadListener() {
                    @Override
                    public void onProcess(long alreadyDownLength, long totalLength) {
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        // Display the download progress or perform other operations.
                                    }
                                });
                    }
                };

        myLocalTranslator
                .preparedModel(downloadStrategy, modelDownloadListener)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Called when the model package is successfully downloaded.
                                // input is a string of less than 5000 characters.
                                final Task<String> task = myLocalTranslator.asyncTranslate(detectText);
                                // Before translation, ensure that the models have been successfully downloaded.
                                task.addOnSuccessListener(
                                                new OnSuccessListener<String>() {
                                                    @Override
                                                    public void onSuccess(String translated) {
                                                        // Processing logic for detection success.
                                                        Log.e("TAG", " Translated Text : " + translated);
                                                        tv_translated_txt.setText(translated);
                                                    }
                                                })
                                        .addOnFailureListener(
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        // Processing logic for detection failure.
                                                        Log.e("TAG", " Translation failed " + e.getMessage());
                                                        Toast.makeText(
                                                                        TextRecognitionActivity.this,
                                                                        "Please check internet connection.",
                                                                        Toast.LENGTH_SHORT)
                                                                .show();
                                                    }
                                                });
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                // Called when the model package fails to be downloaded.
                                Log.e("TAG", " Translation failed onFailure " + e.getMessage());
                            }
                        });
    }

    private String getStringResourceByName(String aString) {
        try {
            String packageName = getPackageName();
            int resId = getResources().getIdentifier(aString, "string", packageName);
            if (resId == 0) {
                return aString;
            } else {
                return getString(resId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return aString;
        }
    }
}
