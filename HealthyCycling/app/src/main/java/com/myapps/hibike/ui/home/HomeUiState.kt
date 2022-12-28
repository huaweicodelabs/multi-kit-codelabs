package com.myapps.hibike.ui.home

import com.huawei.hms.maps.model.LatLng
import com.myapps.hibike.data.model.BikeModel
import com.myapps.hibike.data.model.OnRideModel

data class HomeUiState(
    val bikeList: ArrayList<BikeModel>,
    val lastLocation: LatLng?,
    val onRide: OnRideModel?,
    val startRentingId: String,
    val finishRenting: Boolean,
    val unpaidRide: String,
    var userWeight: Double,
    val isLoading: Boolean,
    val error: String
) {
    companion object {
        fun initial() = HomeUiState(
            bikeList = arrayListOf(),
            lastLocation = null,
            onRide = null,
            startRentingId = "",
            finishRenting = false,
            unpaidRide = "",
            userWeight = 0.1,
            isLoading = false,
            error = ""
        )
    }
}