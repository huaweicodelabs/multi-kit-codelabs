package com.huawei.hms.couriertracking.data.network.model

import com.google.gson.annotations.SerializedName

data class DirectionsRequest(
    @SerializedName("origin") val origin: LatLngData,
    @SerializedName("destination") val destination: LatLngData
)

data class LatLngData(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)