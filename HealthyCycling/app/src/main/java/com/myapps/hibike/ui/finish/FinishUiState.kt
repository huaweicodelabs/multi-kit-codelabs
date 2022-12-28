package com.myapps.hibike.ui.finish

import com.huawei.hms.support.api.client.Status
import com.myapps.hibike.data.model.RideModel

data class FinishUiState(
    val lastRide: RideModel?,
    val date: String,
    val amount: String,
    val second: Long,
    val minute: Long,
    val hour: Long,
    val productId: String,
    val totalDistance: Double,
    val cancelledPay: Boolean,
    val lastRideInfoState: Boolean,
    var iapStatus: Status?,
    val purchaseStatus: Boolean,
    val activityRecordStatus: Boolean,
    val userWeight: Double,
    val isLoading: Boolean,
    val error: String
) {
    companion object {
        fun initial() = FinishUiState(
            lastRide = null,
            date = "",
            amount = "",
            productId = "",
            second = 0,
            minute = 0,
            hour = 0,
            totalDistance = 0.0,
            cancelledPay = false,
            lastRideInfoState = false,
            iapStatus = null,
            purchaseStatus = false,
            activityRecordStatus = false,
            userWeight = 0.1,
            isLoading = false,
            error = ""
        )
    }
}