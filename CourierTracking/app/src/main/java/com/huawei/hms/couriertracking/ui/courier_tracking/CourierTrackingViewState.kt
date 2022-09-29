package com.huawei.hms.couriertracking.ui.courier_tracking

import com.huawei.hms.couriertracking.domain.model.Route

sealed interface CourierTrackingViewState {
    data class Success(
        val route: Route
    ): CourierTrackingViewState
    object Loading : CourierTrackingViewState
    data class Error(val errorMessage: String) : CourierTrackingViewState
    object None : CourierTrackingViewState
}