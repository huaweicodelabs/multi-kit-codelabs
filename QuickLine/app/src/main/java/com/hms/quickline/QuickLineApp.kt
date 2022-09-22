/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hms.quickline

import android.app.Application
import android.util.Log
import com.hms.quickline.domain.repository.CloudDbWrapper
import com.huawei.agconnect.crash.AGConnectCrash
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuickLineApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AGConnectCrash.getInstance()

        CloudDbWrapper.initialize(this) {
            Log.i("Application", it.toString())
        }
    }
}