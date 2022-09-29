package com.huawei.hms.couriertracking.domain.mapper

import android.location.Location
import com.huawei.hms.couriertracking.data.network.model.DirectionsNetwork
import com.huawei.hms.couriertracking.domain.model.Route
import com.huawei.hms.couriertracking.domain.model.StoreLocation
import com.huawei.hms.maps.model.LatLng

object DirectionNetworkMapper {
    fun networkModelToExternalModel(
        directionsNetwork: DirectionsNetwork,
        currentLocation:Location,
        storeLocation: StoreLocation
    ): Route {
        val storeDestination = Location("storeLocation")
        storeDestination.latitude = storeLocation.latitude
        storeDestination.longitude = storeLocation.longitude

        val pathPoints = mutableListOf<LatLng>()

        val startLocation = LatLng(currentLocation.latitude,currentLocation.longitude)
        val endLocation = LatLng(storeLocation.latitude,storeLocation.longitude)
        val distance = currentLocation.distanceTo(storeDestination).toDouble()

        directionsNetwork.routes[0].paths.map { pathsNetwork ->
            pathsNetwork.steps.map { stepsNetwork ->
                stepsNetwork.polyline.map { latLngData ->
                    pathPoints.add(LatLng(latLngData.lat, latLngData.lng))
                }
            }
        }
        return Route(
            pathPoints = pathPoints,
            startDestination =  startLocation,
            endDestination = endLocation,
            distance = distance
        )
    }
}