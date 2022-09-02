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

package com.huawei.hms.knowmyboard.dtse.activity.view;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.knowmyboard.dtse.R;
import com.huawei.hms.knowmyboard.dtse.activity.app.MyApplication;
import com.huawei.hms.knowmyboard.dtse.activity.model.UserData;
import com.huawei.hms.knowmyboard.dtse.activity.util.Constants;
import com.huawei.hms.knowmyboard.dtse.activity.util.RequestLocationData;
import com.huawei.hms.knowmyboard.dtse.activity.viewmodel.LoginViewModel;
import com.huawei.hms.knowmyboard.dtse.databinding.ActivityMainBinding;
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
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.result.AuthAccount;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    LoginViewModel loginViewModel;
    private MLTextAnalyzer mTextAnalyzer;
    public Uri imagePath;
    Bitmap bitmap;
    static String TAG = "TAG";
    ArrayList<String> result = new ArrayList<>();
    MLLocalLangDetector myLocalLangDetector;
    MLLocalTranslator myLocalTranslator;
    String textRecognized;
    ProgressDialog progressDialog;
    NavController navController;
    ActivityMainBinding activityMainBinding;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        loginViewModel = new ViewModelProvider(MainActivity.this).get(LoginViewModel.class);

        navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
        MyApplication.setActivity(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        bottomNavigationView = activityMainBinding.bottomNavigation;
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Process the authorization result to obtain the authorization code from AuthAccount.
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 8888) {
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                // The sign-in is successful, and the user's ID information and authorization code are obtained.
                AuthAccount authAccount = authAccountTask.getResult();
                UserData userData = new UserData();
                userData.setAccessToken(authAccount.getAccessToken());
                userData.setCountryCode(authAccount.getCountryCode());
                userData.setDisplayName(authAccount.getDisplayName());
                userData.setEmail(authAccount.getEmail());
                userData.setFamilyName(authAccount.getFamilyName());
                userData.setGivenName(authAccount.getGivenName());
                userData.setIdToken(authAccount.getIdToken());
                userData.setOpenId(authAccount.getOpenId());
                userData.setUid(authAccount.getUid());
                userData.setPhotoUriString(authAccount.getAvatarUri().toString());
                userData.setUnionId(authAccount.getUnionId());

                loginViewModel = new ViewModelProvider(MainActivity.this).get(LoginViewModel.class);
                loginViewModel.sendData(authAccount.getDisplayName());

            } else {
                // The sign-in failed.
                Log.e("TAG", "sign in failed:" + ((ApiException) authAccountTask.getException()).getStatusCode());
            }
        }
        if (requestCode == 2323 && resultCode == RESULT_OK && data != null) {
            progressDialog.setMessage("Initializing text detection..");
            progressDialog.show();

            imagePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imagePath);
                asyncAnalyzeText(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAG", " BITMAP ERROR");
            }
        }
        if (requestCode == 2424 && resultCode == RESULT_OK && data != null) {
            progressDialog.setMessage("Initializing text detection..");
            progressDialog.show();
            try {
                bitmap = (Bitmap) data.getExtras().get("data");
                asyncAnalyzeText(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG", " BITMAP ERROR");
            }
        }
    }

    private void asyncAnalyzeText(Bitmap bitmap) {
        if (mTextAnalyzer == null) {
            createMLTextAnalyzer();
        }

        MLFrame frame = MLFrame.fromBitmap(bitmap);

        Task<MLText> task = mTextAnalyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(
                        new OnSuccessListener<MLText>() {
                            @Override
                            public void onSuccess(MLText text) {
                                progressDialog.setMessage("Initializing language detection..");
                                textRecognized = text.getStringValue().trim();
                                if (!textRecognized.isEmpty()) {
                                    // Create a local language detector.
                                    MLLangDetectorFactory factory = MLLangDetectorFactory.getInstance();
                                    MLLocalLangDetectorSetting setting =
                                            new MLLocalLangDetectorSetting.Factory()
                                                    // Set the minimum confidence threshold for language detection.
                                                    .setTrustedThreshold(0.01f)
                                                    .create();
                                    myLocalLangDetector = factory.getLocalLangDetector(setting);

                                    Task<String> firstBestDetectTask =
                                            myLocalLangDetector.firstBestDetect(textRecognized);

                                    firstBestDetectTask
                                            .addOnSuccessListener(
                                                    new OnSuccessListener<String>() {
                                                        @Override
                                                        public void onSuccess(String languageDetected) {
                                                            progressDialog.setMessage(
                                                                    "Initializing text translation..");
                                                            // Processing logic for detection success.
                                                            textTranslate(languageDetected, textRecognized, bitmap);
                                                        }
                                                    })
                                            .addOnFailureListener(
                                                    new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(Exception e) {
                                                            // Processing logic for detection failure.
                                                            Log.e("TAG", "Lang detect error:" + e.getMessage());
                                                        }
                                                    });
                                } else {
                                    progressDialog.dismiss();
                                    showErrorDialog("Failed to recognize text.");
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Log.e("TAG", "#==>" + e.getMessage());
                            }
                        });
    }

    private void showErrorDialog(String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage(msg);

        alertDialog.setButton(
                AlertDialog.BUTTON_POSITIVE,
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    private void textTranslate(String languageDetected, String textRecognized, Bitmap uri) {
        MLApplication.initialize(getApplication());
        MLApplication.getInstance().setApiKey(Constants.API_KEY);
        Log.d(TAG, "Lang detect : " + languageDetected);
        Log.d(TAG, "Text : " + textRecognized);
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

        myLocalTranslator = MLTranslatorFactory.getInstance().getLocalTranslator(setting);
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
                                final Task<String> task = myLocalTranslator.asyncTranslate(textRecognized);
                                // Before translation, ensure that the models have been successfully downloaded.
                                task.addOnSuccessListener(
                                                new OnSuccessListener<String>() {
                                                    @Override
                                                    public void onSuccess(String translated) {
                                                        // Processing logic for detection success.
                                                        result.clear();
                                                        result.add(languageDetected.trim());
                                                        result.add(textRecognized.trim());
                                                        result.add(translated.trim());
                                                        loginViewModel.setImage(uri);

                                                        loginViewModel.setTextRecognized(result);
                                                        progressDialog.dismiss();
                                                    }
                                                })
                                        .addOnFailureListener(
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        // Processing logic for detection failure.
                                                        progressDialog.dismiss();
                                                    }
                                                });
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                // Called when the model package fails to be downloaded.
                                progressDialog.dismiss();
                            }
                        });
    }

    private void createMLTextAnalyzer() {
        MLLocalTextSetting setting =
                new MLLocalTextSetting.Factory().setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE).create();
        mTextAnalyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting);
    }

    @Override
    protected void onStop() {
        if (myLocalLangDetector != null) {
            myLocalLangDetector.stop();
        }
        if (myLocalTranslator != null) {
            myLocalTranslator.stop();
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        super.onStop();
    }
}
