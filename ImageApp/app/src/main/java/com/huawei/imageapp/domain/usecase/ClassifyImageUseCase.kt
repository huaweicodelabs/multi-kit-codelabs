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

import android.graphics.Bitmap
import com.huawei.imageapp.domain.model.ImageClassification
import com.huawei.imageapp.domain.repository.ImageRepository
import com.huawei.imageapp.core.common.Result
import com.huawei.imageapp.domain.model.toExtarnalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import java.io.IOException
import javax.inject.Inject

class ClassifyImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(bitmap: Bitmap?): Flow<Result<List<ImageClassification>>> = flow {
        try {
            bitmap?.let {
                val mlImageClassifications = imageRepository.classifyImage(it)
                emit(
                    Result.Success(
                        mlImageClassifications
                            .sortedByDescending { it.possibility }
                            .map { it.toExtarnalModel() }
                    )
                )
            }?: run { emit(Result.Error("An error occurred while selecting a photo.")) }
        } catch (e: IOException) {
            emit(Result.Error("Please check your internet connection."))
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }.onStart { emit(Result.Loading) }
}