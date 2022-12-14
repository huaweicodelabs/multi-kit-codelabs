/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
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
package com.huawei.discovertourismapp.network;


import com.huawei.discovertourismapp.bean.TokenResponse;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface QueryService {
    @FormUrlEncoded
    @POST(UrlHelper.REQUEST_TOKEN)
    Observable<TokenResponse> getRequestToken(
            @Field("grant_type") String grantType,
            @Field("client_id") String ClientId,
            @Field("client_secret") String clientSecret);
}
