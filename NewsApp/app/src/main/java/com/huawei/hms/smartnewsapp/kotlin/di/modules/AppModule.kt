/*
 *
 *  * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package com.huawei.hms.smartnewsapp.kotlin.di.modules

import android.content.Context
import android.content.res.Resources
import com.huawei.hms.network.httpclient.HttpClient
import com.huawei.hms.network.restclient.RestClient
import com.huawei.hms.smartnewsapp.kotlin.app.App
import com.huawei.hms.smartnewsapp.kotlin.data.api.ApiService
import com.huawei.hms.smartnewsapp.kotlin.util.Constants
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AcitivityModule::class, ViewModelModule::class])
class AppModule {

    /**
     * Provides a rest client service from Network kit.
     */
    @Singleton
    @Provides
    fun provideNetworkService(): ApiService {
        return RestClient
                .Builder()
                .baseUrl(Constants.BASE_URL)
                .httpClient(getHttpClient())
                .build()
                .create(ApiService::class.java)
    }


    private fun getHttpClient(): HttpClient? {
        return HttpClient.Builder()
                .connectTimeout(Constants.CONNECT_TIMEOUT)
                .readTimeout(Constants.TIMEOUT)
                .writeTimeout(Constants.WRITE_TIMEOUT)
                .enableQuic(false)
                .build()
    }

    /**
     * Application application level context.
     */
    @Singleton
    @Provides
    fun provideContext(application: App): Context = application.applicationContext


    /**
     * Application resource provider, so that we can get the Drawable, Color, String etc at runtime
     */
    @Provides
    @Singleton
    fun providesResources(application: App): Resources = application.resources
}