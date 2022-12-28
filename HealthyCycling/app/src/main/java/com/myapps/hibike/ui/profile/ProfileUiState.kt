package com.myapps.hibike.ui.profile

data class ProfileUiState(
    val userWeight: Double?,
    var userWeightStatus: Boolean,
    val isLoading: Boolean,
    val error: String
) {
    companion object {
        fun initial() = ProfileUiState(
            userWeight = null,
            userWeightStatus = false,
            isLoading = false,
            error = ""
        )
    }
}