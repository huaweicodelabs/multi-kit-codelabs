package com.huawei.hms.couriertracking.domain.model

import com.huawei.hms.maps.model.LatLng

data class Route(
    val pathPoints: List<LatLng>?,
    val startDestination: LatLng,
    val endDestination: LatLng,
    val distance: Double
)
