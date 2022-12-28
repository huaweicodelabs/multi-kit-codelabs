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

package com.myapps.hibike.ui.lastRides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapps.hibike.data.model.RideModel
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
class LastRidesViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
) : ViewModel() {

    private val _lastRidesUiState: MutableStateFlow<LastRidesUiState> = MutableStateFlow(LastRidesUiState.initial())
    val lastRidesUiState: StateFlow<LastRidesUiState> = _lastRidesUiState.asStateFlow()

    fun getMyLastRides() = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.getMyLastRides(object : IServiceListener<ArrayList<RideModel>> {
            override fun onSuccess(successResult: ArrayList<RideModel>) {
                checkUnpaidRide(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    fun checkUnpaidRide(rideList: ArrayList<RideModel>) = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.checkUnpaidRide(object : IServiceListener<String> {
            override fun onSuccess(successResult: String) {
                setLastRidesAndUnpaidRideState(rideList, successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    private fun setLastRidesAndUnpaidRideState(rideList: ArrayList<RideModel>, id: String) {
        _lastRidesUiState.update { lastRidesUiState ->
            lastRidesUiState.copy(rideList = rideList, unpaidRide = id, isLoading = false)
        }
    }

    private fun setLoadingState() {
        _lastRidesUiState.update { lastRidesUiState ->
            lastRidesUiState.copy(isLoading = true)
        }
    }

    private fun setErrorState(exception: Exception) {
        _lastRidesUiState.update { lastRidesUiState ->
            lastRidesUiState.copy(error = exception.toString(), isLoading = false)
        }
    }

}