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


package com.huawei.imageapp.domain.model

import android.os.Parcelable
import com.huawei.imageapp.data.model.Image
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalImage(
    val id: String,
    val likeCount: String,
    val description: String,
    val imageUrl: String,
    val profileImageUrl: String,
    val name: String,
    val username: String,
    val key: String
): Parcelable

fun Image.toExternalModel() = ExternalImage(
    id = id,
    likeCount = likes.toString(),
    description = description,
    imageUrl = imageUrl,
    profileImageUrl = profileImageUrl,
    name = name,
    username = username,
    key = key
)
