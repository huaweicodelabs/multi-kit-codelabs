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
@file:Suppress("NewLineAtEndOfFile","WildcardImport")
package com.huawei.hms.maps.sample.utils

import android.util.Log
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Fetching root path
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

class NetClient private constructor() {

    companion object {
        private val TAG = NetClient::class.qualifiedName
        private val mDefaultKey = AppConstants.API_KEY
        private val mDrivingRoutePlanningURL = AppConstants.DRIVE_PLAN_KEY
        private var client: OkHttpClient? = null
        private val JSON = MediaType.parse("application/json; charset=utf-8")

        @JvmStatic
        var netClient: NetClient? = null
            get() {
                if (field == null) {
                    field = NetClient()
                }
                return field
            }
            private set
    }

    init {
        client = initOkHttpClient()
    }

    /**
     * To set the read timeout.
     * To set the connect timeout.
     */
    private fun initOkHttpClient(): OkHttpClient? {
        if (client == null) {
            client = OkHttpClient.Builder()
                    .readTimeout(AppConstants.LOCATION_REQ_INTERVAL, TimeUnit.MILLISECONDS)
                    .connectTimeout(AppConstants.LOCATION_REQ_INTERVAL, TimeUnit.MILLISECONDS)
                    .build()
        }
        return client
    }
    @Suppress("ComplexMethod")
    /**
     * @param sourceLatLng origin latitude and longitude
     * @param destinatinLatLng destination latitude and longitude
     * @param needEncode dose the api key need to be encoded
     * @return Response returns API response
     */
    fun getDrivingRoutePlanningResult(
            sourceLatLng: LatLng,
            destinatinLatLng: LatLng,
            needEncode: Boolean
    ): Response? {
        var key = mDefaultKey
        if (needEncode) {
            try {
                key = URLEncoder.encode(mDefaultKey, AppConstants.ENCODE_FORMAT)
            } catch (e: UnsupportedEncodingException) {
                Log.w(TAG, "UnsupportedFileException")
            }
        }

        val url = "$mDrivingRoutePlanningURL?key=$key"
        var response: Response? = null
        val origin = JSONObject()
        val destination = JSONObject()
        val json = JSONObject()
        try {
            origin.apply {
                put(AppConstants.LATITUDE_KEY, sourceLatLng.latitude)
                put(AppConstants.LONGITUDE_KEY, sourceLatLng.longitude)
            }
            destination.apply {
                put(AppConstants.LATITUDE_KEY, destinatinLatLng.latitude)
                put(AppConstants.LONGITUDE_KEY, destinatinLatLng.longitude)
            }
            json.apply {
                put(AppConstants.ORIGIN_LOC_KEY, origin)
                put(AppConstants.DESTINATION_LOC_KEY, destination)
            }

            val requestBody = RequestBody.create(JSON, json.toString())
            val request = Request.Builder().url(url).post(requestBody).build()
            response = netClient?.initOkHttpClient()?.newCall(request)?.execute()
        } catch (e: JSONException) {
            Log.w(TAG, "JSONException")
        } catch (e: IOException) {
            Log.w(TAG, "IOException")
        }
        return response
    }
}