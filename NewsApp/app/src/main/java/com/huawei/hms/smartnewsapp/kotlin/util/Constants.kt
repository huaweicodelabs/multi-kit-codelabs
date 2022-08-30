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

/**
 * Contants
 */
object Constants {
    /**
     * For splash screen delay
     */
    const val DELAY_MILLIS = 3000
    const val CONNECT_TIMEOUT = 10000
    const val WRITE_TIMEOUT = 1000
    const val TIMEOUT = 10000
    /**
     * URL to fetch news
     */
    const val BASE_URL = "https://"
    /**
     * status value for news from server
     */
    const val STATUS_FAILED = "FAILED"

    /**
     * status value for news from server
     */
    const val STATUS_OK = "ok"

    /**
     * login request code
     */
    const val REQUEST_CODE = 200

    /**
     * Shared preference name
     */
    const val MY_PREFS_NAME = "smartNews_pref"


    /**
     * country code for location news
     */
    const val COUNTRY_CODE = "in"

    const val GRANT_TYPE = "client_credentials"
    const val CLIENT_SECRET = "e65995697cb3925045ebc162aea194b37aadce0e0503552e3287df936f05643e"
    const val CLIENT_ID = "103207661"
}