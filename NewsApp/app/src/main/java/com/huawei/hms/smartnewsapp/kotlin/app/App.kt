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
package com.huawei.hms.smartnewsapp.kotlin.app

import android.util.Log
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.network.NetworkKit
import com.huawei.hms.searchkit.SearchKitInstance
import com.huawei.hms.smartnewsapp.kotlin.di.components.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication


class App : DaggerApplication() {
    private val API_KEY = "client/api_key"
    private val TAG = "Application"
    private val applicationInjector = DaggerAppComponent.builder().application(this).build()

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = applicationInjector
    override fun onCreate() {
        initNetworkKit()
        initSearchKit()
        initMLKit()
        super.onCreate()

    }

    /**
     * Initialize the SearchKit instance.
     */
    fun initSearchKit() {
        SearchKitInstance.init(this, "103207661")
    }

    /**
     * Initialize the Ml Kit.
     */
    private fun initMLKit() {
        val config = AGConnectServicesConfig.fromContext(this)
        MLApplication.getInstance().apiKey = config.getString(API_KEY)
    }

    /**
     * Asynchronously load the NetworkKit object.
     */
    fun initNetworkKit() {
        NetworkKit.init(this, object : NetworkKit.Callback() {
            override fun onResult(result: Boolean) {
                if (result) {
                    Log.i(TAG, "Networkkit init success")
                } else {
                    Log.i(TAG, "Networkkit init failed")
                }
            }
        })
    }
}