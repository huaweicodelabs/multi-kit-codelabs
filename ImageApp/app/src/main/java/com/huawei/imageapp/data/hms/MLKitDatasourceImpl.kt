/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.huawei.imageapp.data.hms

import android.graphics.Bitmap
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.classification.MLImageClassification
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.langdetect.MLLangDetectorFactory
import com.huawei.hms.mlsdk.translate.MLTranslateLanguage
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslateSetting
import com.huawei.imageapp.core.common.await

class MLKitDatasourceImpl: MLKitDatasource {

    /**
     * Step 1: Create an image classification analyzer.
     *  Use customized parameter settings or default parameter settings for on-device recognition
     *      Customized -> var setting = MLLocalClassificationAnalyzerSetting.Factory().setMinAcceptablePossibility(0.8f).create()
     *                    var analyzer = MLAnalyzerFactory.getInstance().getLocalImageClassificationAnalyzer(setting)
     *
     *      Default -> var analyzer = MLAnalyzerFactory.getInstance().localImageClassificationAnalyzer
     *
     * Step 2: Create an MLFrame object using android.graphics.Bitmap. JPG, JPEG, PNG, and BMP images are supported.
     * Step 3: Call the asyncAnalyseFrame method to classify images.
     * Step 4: After the recognition is complete, stop the analyzer to release recognition resources.
    */
    override suspend fun classifyImage(bitmap: Bitmap): List<MLImageClassification> {
        val analyzer = MLAnalyzerFactory.getInstance().localImageClassificationAnalyzer //Step 1
        val frame = MLFrame.fromBitmap(bitmap) //Step 2
        val task = analyzer.asyncAnalyseFrame(frame) //Step 3
        val classificationResult = task.await()
        analyzer.stop() //Step 4

        return classificationResult
    }

    /**
     * Step 1: Create a language detector.
     *  Use customized parameter settings or default parameter settings
     *  to create a language detector.
     *
     *      Customized -> val setting = MLRemoteLangDetectorSetting
     *                                  .Factory()
     *                                  .setTrustedThreshold(0.01f)
     *                                  .create()
     *                    val mlRemoteLangDetector = MLLangDetectorFactory
     *                                            .getInstance()
     *                                            .getRemoteLangDetector(setting)
     *
     *      Default -> val mlRemoteLangDetector = MLLangDetectorFactory
     *                                            .getInstance()
     *                                            .remoteLangDetector
     *
     * Step 2: Implement language detection.
     *
     * Step 3: Release resources after the detection is complete.
     */
    override suspend fun detectLanguage(text: String): String {
        val mlRemoteLangDetector = MLLangDetectorFactory  //Step 1
            .getInstance()
            .remoteLangDetector

        val firstBestDetectTask = mlRemoteLangDetector //Step 2
            .firstBestDetect(text)
        val detectResult = firstBestDetectTask.await()

        mlRemoteLangDetector.stop() //Step 3

        return detectResult
    }

    /**
     * Step 1: Create a real-time text translator.
     * Language Code -> The BCP-47 standard is used for Traditional Chinese,
     * and the ISO 639-1 standard is used for other languages.
     *
     * Step 2: Implement real-time translation.
     *
     * Step 3: Release resources after the translation is complete.
     */
    override suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): String {
        val setting = MLRemoteTranslateSetting //Step 1
            .Factory()
            .setSourceLangCode(sourceLanguage)
            .setTargetLangCode(targetLanguage)
            .create()
        val mlRemoteTranslator = MLTranslatorFactory
            .getInstance()
            .getRemoteTranslator(setting)

        val translatorTask = mlRemoteTranslator //Step 2
            .asyncTranslate(text)
        val translationResult = translatorTask.await()

        mlRemoteTranslator.stop() //Step 3

        return translationResult
    }

    override suspend fun getSupportedLanguages(): Set<String> {
        val languagesTask = MLTranslateLanguage.getCloudAllLanguages()
        return languagesTask.await()
    }
}