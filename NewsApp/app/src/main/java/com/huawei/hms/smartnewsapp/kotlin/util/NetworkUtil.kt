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
package com.huawei.hms.smartnewsapp.kotlin.util

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Class to check for network connection
 */
class NetworkUtil(  // connected to the internet
        var application: Application) {
    /**
     * check for network status
     *
     * @return boolean value describing the netwoek status
     */
    val isNetworkConnected: Boolean
        get() {
            var result = false
            val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (cm != null) {
                    val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
                    if (capabilities != null) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            result = true
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            result = true
                        }
                    }
                }
            } else {
                if (cm != null) {
                    val activeNetwork = cm.activeNetworkInfo
                    if (activeNetwork != null) {
                        // connected to the internet
                        if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                            result = true
                        } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                            result = true
                        }
                    }
                }
            }
            return result
        }
}