package com.myapps.hibike.data.network

import com.myapps.hibike.data.model.NotifMessage
import com.myapps.hibike.data.model.NotifMessageBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Call

interface NotifMessageInterface {

    @Headers("Content-Type:application/json; charset=UTF-8")
    @POST("v1/107055885/messages:send")
    fun createNotification(
        @Header("Authorization") authorization: String?,
        @Body notifMessageBody: NotifMessageBody
    ) : Call<NotifMessage>
}