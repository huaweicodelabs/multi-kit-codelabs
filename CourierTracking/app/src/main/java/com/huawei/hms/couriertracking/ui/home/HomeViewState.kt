package com.huawei.hms.couriertracking.ui.home

import com.huawei.hms.couriertracking.domain.model.Order

sealed interface HomeViewState {
    data class Success(
        val orders: List<Order>
    ) : HomeViewState
    object Loading : HomeViewState
    data class Error(
        val errorMessage: String
    ) : HomeViewState
    object None : HomeViewState
}
