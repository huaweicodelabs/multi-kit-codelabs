package com.huawei.hms.couriertracking.ui.splash

sealed interface SplashViewState {
    object Success: SplashViewState
    object Loading : SplashViewState
    data class Error(
        val errorMessage: String
    ): SplashViewState
    object None : SplashViewState
}