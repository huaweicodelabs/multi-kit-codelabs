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


package com.huawei.imageapp.data

import android.graphics.Bitmap
import com.huawei.hms.mlsdk.classification.MLImageClassification
import com.huawei.imageapp.core.common.Result
import com.huawei.imageapp.data.cloud.CloudDBDatasource
import com.huawei.imageapp.data.hms.MLKitDatasource
import com.huawei.imageapp.data.model.Image
import com.huawei.imageapp.domain.repository.ImageRepository
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val cloudDBDatasource: CloudDBDatasource,
    private val mlKitDatasource: MLKitDatasource
): ImageRepository {
    override suspend fun initializeCloudDB(): Result<Unit> {
        return cloudDBDatasource.initialize()
    }

    override suspend fun searchImages(query: String): List<Image> {
        return cloudDBDatasource.searchImage(query)
    }

    override suspend fun classifyImage(bitmap: Bitmap): List<MLImageClassification> {
        return mlKitDatasource.classifyImage(bitmap)
    }

    override suspend fun detectDescriptionLanguage(description: String): String {
        return mlKitDatasource.detectLanguage(description)
    }

    override suspend fun translateDescription(
        description: String,
        sourceLanguage: String,
        targetLanguage: String
    ): String {
        return mlKitDatasource.translateText(description,sourceLanguage,targetLanguage)
    }

    override suspend fun getSupportedLanguages(): Set<String> {
        return mlKitDatasource.getSupportedLanguages()
    }
}