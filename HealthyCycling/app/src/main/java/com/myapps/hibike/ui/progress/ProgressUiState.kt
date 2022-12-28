package com.myapps.hibike.ui.progress

import com.myapps.hibike.data.model.RideModel

data class ProgressUiState(
    val rideList: ArrayList<RideModel>?,
    val userWeight: Double,
    val accessToken: String,
    val isNotifSuccess: Boolean,
    val totalCalorie: Float?,
    val time: String,
    val totalDistance: Double?,
    val totalRide: Int,
    val avgSpeed: Double?,
    val userInformedStatus: Boolean?,
    val isLoading: Boolean,
    val error: String,
    val throwable: String
) {
    companion object {
        fun initial() = ProgressUiState(
            rideList = null,
            userWeight = 0.0,
            accessToken = "",
            isNotifSuccess = false,
            totalCalorie = null,
            time = "",
            totalDistance = 0.0,
            totalRide = 0,
            avgSpeed = 0.0,
            userInformedStatus = null,
            isLoading = false,
            throwable = "",
            error = ""
        )
    }
}