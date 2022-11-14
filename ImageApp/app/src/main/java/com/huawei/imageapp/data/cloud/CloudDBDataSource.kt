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


package com.huawei.imageapp.data.cloud

import com.huawei.imageapp.core.common.Result
import com.huawei.imageapp.data.model.Image

class CloudDbNotInitializedException : Exception()

interface CloudDBDatasource {
    suspend fun initialize(): Result<Unit>
    suspend fun searchImage(
        query: String
    ): List<Image>
}