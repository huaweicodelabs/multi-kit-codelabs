/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myapps.hibike.data.repository

import com.myapps.hibike.data.model.AccessToken
import com.myapps.hibike.data.model.NotifMessage
import com.myapps.hibike.data.model.NotifMessageBody
import com.myapps.hibike.data.network.AccessTokenInterface
import com.myapps.hibike.data.network.NotifMessageInterface
import com.myapps.hibike.utils.IPushNotifServiceListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class PushNotifRepository @Inject constructor(
    private val retrofitAccessToken: AccessTokenInterface,
    private val retrofitNotification: NotifMessageInterface,
){

    companion object{
        const val GRANT_TYPE = "client_credentials"
        const val CLIENT_SECRET = "a0f33161c797ca53edc9729c61947dd35e0a606d6f060a2b9873718bde7b4bc9"
        const val CLIENT_ID = "107055885"
    }

    fun getAccessToken(serviceListener: IPushNotifServiceListener<String>){
        val result = retrofitAccessToken.createAccessToken(
            GRANT_TYPE,
            CLIENT_SECRET,
            CLIENT_ID
        )
        result.enqueue(object : Callback<AccessToken> {
            override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {
                serviceListener.onSuccess(response.body()?.access_token.toString())
            }

            override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                serviceListener.onError(t)
            }

        })
    }

    fun sendNotification(accessToken: String, pushToken: String, title:String, body:String, serviceListener: IPushNotifServiceListener<Boolean>) {

        val notifMessageBody: NotifMessageBody = NotifMessageBody.Builder(
            title, body, arrayOf(pushToken)
        ).build()

        retrofitNotification
            .createNotification(
                "Bearer $accessToken",
                notifMessageBody
            )
            .enqueue(object : Callback<NotifMessage> {
                override fun onFailure(call: Call<NotifMessage>, t: Throwable) {
                    serviceListener.onError(t)
                }

                override fun onResponse(
                    call: Call<NotifMessage>,
                    response: Response<NotifMessage>
                ) {
                    if (response.isSuccessful) {
                        serviceListener.onSuccess(true)
                    }
                }
            })
    }
}