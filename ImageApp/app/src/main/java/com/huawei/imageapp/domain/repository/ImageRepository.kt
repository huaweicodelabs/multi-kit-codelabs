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


package com.huawei.imageapp.domain.repository

import android.graphics.Bitmap
import com.huawei.hms.mlsdk.classification.MLImageClassification
import com.huawei.imageapp.core.common.Result
import com.huawei.imageapp.data.model.Image

interface ImageRepository {
    suspend fun initializeCloudDB():Result<Unit>
    suspend fun searchImages(
        query: String
    ): List<Image>
    suspend fun classifyImage(bitmap: Bitmap): List<MLImageClassification>
    suspend fun detectDescriptionLanguage(description: String): String
    suspend fun translateDescription(
        description: String,
        sourceLanguage: String,
        targetLanguage: String
    ): String
    suspend fun getSupportedLanguages(): Set<String>
}