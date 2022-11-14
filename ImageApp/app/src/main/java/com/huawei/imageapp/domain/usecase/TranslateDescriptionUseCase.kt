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


package com.huawei.imageapp.domain.usecase

import com.huawei.imageapp.domain.repository.ImageRepository
import com.huawei.imageapp.core.common.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class TranslateDescriptionUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(
        photoDescription: String?,
        language: String?,
        targetLanguage: String
    ): Flow<Result<String>> = flow {
        try {
            if (!photoDescription.isNullOrEmpty()) {
                emit(Result.Loading)
                val sourceLanguage = language?.lowercase() ?: "en"
                val translatedText = imageRepository.translateDescription(
                    photoDescription,
                    sourceLanguage,
                    targetLanguage
                )
                emit(Result.Success(translatedText))
            }
        } catch (e: IOException) {
            emit(Result.Error("Please check your internet connection."))
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }
}