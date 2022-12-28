/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myapps.hibike.ui.login

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huawei.agconnect.auth.AGConnectUser
import com.myapps.hibike.data.repository.AuthServiceRepository
import com.myapps.hibike.data.repository.FirebaseRepository
import com.myapps.hibike.utils.IServiceListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authServiceRepository: AuthServiceRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val _authState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState.initial())
    val authState: StateFlow<LoginUiState> = _authState.asStateFlow()

    fun signIn(intent: Intent?) = viewModelScope.launch {
        setLoadingState()
        authServiceRepository.userSignIn(intent, object : IServiceListener<AGConnectUser> {
            override fun onSuccess(successResult: AGConnectUser) {
                setUserSignedState()
                createUser()
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    private fun setUserSignedState() {
        _authState.update { loginUiState ->
            loginUiState.copy(isUserSignedIn = true, isLoading = false)
        }
    }

    private fun createUser() = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.createUser(object : IServiceListener<Boolean> {
            override fun onSuccess(successResult: Boolean) {
                setCreateUserState()
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })

    }

    private fun setCreateUserState() {
        _authState.update { loginUiState ->
            loginUiState.copy(isUserCreated = true, isLoading = false)
        }
    }

    private fun setLoadingState() {
        _authState.update { loginUiState ->
            loginUiState.copy(isLoading = true)
        }
    }

    private fun setErrorState(exception: Exception) {
        _authState.update { loginUiState ->
            loginUiState.copy(error = exception.toString(), isLoading = false)
        }
    }


}