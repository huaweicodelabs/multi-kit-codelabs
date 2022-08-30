package com.huawei.hms.smartnewsapp.java.data.api;

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

import com.huawei.hms.network.httpclient.Submit;
import com.huawei.hms.network.restclient.anno.Field;
import com.huawei.hms.network.restclient.anno.FormUrlEncoded;
import com.huawei.hms.network.restclient.anno.Headers;
import com.huawei.hms.network.restclient.anno.POST;

/**
 * Interface to get Access Token for search kit
 */
public interface AccessTokenService {
    @POST("oauth-login.cloud.huawei.com/oauth2/v3/token")
    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded\", \"charset:UTF-8")
    Submit<String> createAccessToken(
            @Field("grant_type") String grant_type,
            @Field("client_secret") String client_secret,
            @Field("client_id") String client_id);
}
