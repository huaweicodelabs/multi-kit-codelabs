/*
 *
 *  * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */
@file:Suppress("NewLineAtEndOfFile","TooGenericExceptionCaught")
package com.huawei.hms.urbanhomeservices.kotlin.utils

import android.util.Log
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.sample.utils.NetClient.Companion.netClient
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * NetworkRequestManager
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

object NetworkRequestManager {
    private val TAG = NetworkRequestManager::class.qualifiedName

    /**
     * @param latLng1 origin latitude and longitude
     * @param latLng2 destination latitude and longitude
     * @param listener network listener
     */
    fun getDrivingRoutePlanningResult(
            latLng1: LatLng,
            latLng2: LatLng,
            listener: OnNetworkListener?
    ) {
        getDrivingRoutePlanningResult(latLng1, latLng2, listener, 0, false)
    }
    @Suppress("ComplexMethod","TooGenericExceptionCaught")
    /**
     * @param latLng1 origin latitude and longitude
     * @param latLng2 destination latitude and longitude
     * @param listener network listener
     * @param count last number of retries
     * @param needEncode dose the api key need to be encoded
     */
    private fun getDrivingRoutePlanningResult(
            latLng1: LatLng,
            latLng2: LatLng,
            listener: OnNetworkListener?,
            count: Int,
            needEncode: Boolean
    ) {
        var countRoute = count
        val curCount = ++countRoute
        Log.e(TAG, "current count")
        Thread(Runnable {
            val response = netClient?.getDrivingRoutePlanningResult(latLng1, latLng2, needEncode)
            if (response?.body() != null && response.isSuccessful) {
                try {
                    val result = response.body()?.string()
                    listener?.requestSuccess(result)
                    return@Runnable
                } catch (e: IOException) {
                    Log.e(TAG, "IOException")
                }
            }
            var returnCode = ""
            var returnDesc: String
            var need = needEncode
            try {
                val result = response?.body()?.string() ?: ""
                val jsonObject = JSONObject(result)
                returnCode = jsonObject.optString("returnCode")
                returnDesc = jsonObject.optString("returnDesc")
            } catch (e: NullPointerException) {
                returnDesc = "Request Fail: NullPointerException"
            } catch (e: IOException) {
                returnDesc = "Request Fail: IOException "
            } catch (e: JSONException) {
                returnDesc = "Request Fail: JSONException "
            }
            if (curCount >= AppConstants.MAX_TIMES) {
                listener?.requestFail(returnDesc)
                return@Runnable
            }
            if (returnCode == "6") {
                need = true
            }
            getDrivingRoutePlanningResult(latLng1, latLng2, listener, curCount, need)
        }).start()
    }

    interface OnNetworkListener {
        fun requestSuccess(result: String?)
        fun requestFail(errorMsg: String?)
    }
}
