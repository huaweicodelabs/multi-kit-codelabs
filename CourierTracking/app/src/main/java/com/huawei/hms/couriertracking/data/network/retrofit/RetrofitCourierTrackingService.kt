package com.huawei.hms.couriertracking.data.network.retrofit

import com.huawei.hms.couriertracking.data.network.model.DirectionsNetwork
import com.huawei.hms.couriertracking.data.network.model.DirectionsRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitCourierTrackingService {

    @POST("{type}")
    suspend fun getDirections(
        @Path(value = "type",encoded = true) type : String,
        @Body directionRequest: DirectionsRequest,
        @Query("key") api_key: String,
    ): DirectionsNetwork
}