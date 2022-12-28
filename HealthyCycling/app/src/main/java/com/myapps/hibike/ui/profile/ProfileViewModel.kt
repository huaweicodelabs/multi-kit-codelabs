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

package com.myapps.hibike.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class ProfileViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val _profileUiState: MutableStateFlow<ProfileUiState> =
        MutableStateFlow(ProfileUiState.initial())
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()


    fun checkUserWeight() = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.checkUserWeight(object : IServiceListener<Double> {
            override fun onSuccess(successResult: Double) {
                setUserWeightState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    fun updateUserWeight(weight: Double) = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.updateUserWeight(weight, object : IServiceListener<Boolean> {
            override fun onSuccess(successResult: Boolean) {
                setUpdateUserWeightState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })

    }

    private fun setUpdateUserWeightState(result: Boolean) {
        _profileUiState.update { profileUiState ->
            profileUiState.copy(userWeightStatus = result, isLoading = false)
        }
    }

    private fun setUserWeightState(result: Double) {
        _profileUiState.update { profileUiState ->
            profileUiState.copy(userWeight = result, isLoading = false)
        }
    }

    private fun setLoadingState() {
        _profileUiState.update { profileUiState ->
            profileUiState.copy(isLoading = true)
        }
    }

    private fun setErrorState(exception: Exception) {
        _profileUiState.update { profileUiState ->
            profileUiState.copy(error = exception.toString(), isLoading = false)
        }
    }
}