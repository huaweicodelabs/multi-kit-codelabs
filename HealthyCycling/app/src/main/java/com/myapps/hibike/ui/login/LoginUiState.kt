package com.myapps.hibike.ui.login

data class LoginUiState(
    val isUserSignedIn: Boolean,
    val isUserCreated: Boolean,
    val isLoading: Boolean,
    val error: String
) {
    companion object {
        fun initial() = LoginUiState(
            isUserSignedIn = false,
            isUserCreated = false,
            isLoading = false,
            error = ""
        )
    }
}