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


package com.huawei.imageapp.data.di

import android.content.Context
import com.huawei.imageapp.data.ImageRepositoryImpl
import com.huawei.imageapp.data.cloud.CloudDBDatasource
import com.huawei.imageapp.data.cloud.CloudDBDatasourceImpl
import com.huawei.imageapp.data.hms.MLKitDatasource
import com.huawei.imageapp.data.hms.MLKitDatasourceImpl
import com.huawei.imageapp.domain.repository.ImageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideImageRepository(
        cloudDBDatasource: CloudDBDatasource,
        mlKitDatasource: MLKitDatasource
    ): ImageRepository {
        return ImageRepositoryImpl(
            cloudDBDatasource,
            mlKitDatasource
        )
    }

    @Provides
    @Singleton
    fun provideCloudDBDatasource(
        @ApplicationContext context: Context
    ): CloudDBDatasource {
        return CloudDBDatasourceImpl(context)
    }

    @Provides
    @Singleton
    fun provideMLKitDatasource(): MLKitDatasource {
        return MLKitDatasourceImpl()
    }
}