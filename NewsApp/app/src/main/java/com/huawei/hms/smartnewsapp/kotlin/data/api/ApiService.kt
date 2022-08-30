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
package com.huawei.hms.smartnewsapp.kotlin.data.api

import com.huawei.hms.network.httpclient.Submit
import com.huawei.hms.network.restclient.anno.*



/**
 * Interface to get Access Token for search kit
 */
interface ApiService {

    @POST("oauth-login.cloud.huawei.com/oauth2/v3/token")
    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded\", \"charset:UTF-8")
    fun getAccessToken(
        @Field("grant_type") grant_type: String?,
        @Field("client_secret") client_secret: String?,
        @Field("client_id") client_id: String?
    ): Submit<String>?

    /**
     * Declare a request API.
     **/
    @GET("newsapi.org/v2/top-headlines")
    fun getNews(@Query("country") country: String?, @Query("apiKey") apiKey: String?): Submit<String>
}

