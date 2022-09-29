package com.huawei.hms.couriertracking.data.network.model

import com.google.gson.annotations.SerializedName

data class DirectionsNetwork(
    @SerializedName("routes") val routes: List<RoutesNetwork>,
    @SerializedName("returnCode") val returnCode: String,
    @SerializedName("returnDesc") val returnDesc: String
)

data class RoutesNetwork(
    @SerializedName("paths") val paths: List<PathsNetwork>,
    @SerializedName("bounds") val bounds: BoundsNetwork
)

data class PathsNetwork(
    @SerializedName("duration") val duration: Double,
    @SerializedName("durationText") val durationText: String,
    @SerializedName("durationInTraffic") val durationInTraffic: Double,
    @SerializedName("distance") val distance: Double,
    @SerializedName("startLocation") val startLocation: LatLngData,
    @SerializedName("startAddress") val startAddress: String,
    @SerializedName("distanceText") val distanceText: String,
    @SerializedName("steps") val steps: List<StepsNetwork>,
    @SerializedName("endLocation") val endLocation: LatLngData,
    @SerializedName("endAddress") val endAddress: String
)

data class BoundsNetwork(
    @SerializedName("southwest") val southwest: LatLngData,
    @SerializedName("northeast") val northeast: LatLngData
)

data class StepsNetwork(
    @SerializedName("duration") val duration: Double,
    @SerializedName("orientation") val orientation: Double,
    @SerializedName("durationText") val durationText: String,
    @SerializedName("distance") val distance: Double,
    @SerializedName("startLocation") val startLocation: LatLngData,
    @SerializedName("instruction") val instruction: String,
    @SerializedName("action") val action: String,
    @SerializedName("distanceText") val distanceText: String,
    @SerializedName("endLocation") val endLocation: LatLngData,
    @SerializedName("polyline") val polyline: List<LatLngData>,
    @SerializedName("roadName") val roadName: String
)