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

package com.myapps.hibike.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.huawei.hms.maps.model.LatLng
import com.myapps.hibike.data.model.BikeModel
import com.myapps.hibike.data.model.OnRideModel
import com.myapps.hibike.data.repository.FirebaseRepository
import com.myapps.hibike.data.repository.LastLocationRepository
import com.myapps.hibike.utils.IServiceListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val lastLocationRepository: LastLocationRepository
) : ViewModel() {

    private val _homeUiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState.initial())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    fun getBikes() = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.getBikes(object : IServiceListener<ArrayList<BikeModel>> {
            override fun onSuccess(successResult: ArrayList<BikeModel>) {
                setGettingBikeState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    fun getLastLocation() = viewModelScope.launch {
        setLoadingState()
        lastLocationRepository.getLastLocation(object : IServiceListener<LatLng> {
            override fun onSuccess(successResult: LatLng) {
                setLastLocationState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    fun startRenting(bikeId: String) = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.startRenting(bikeId, object : IServiceListener<String> {
            override fun onSuccess(successResult: String) {
                setStartRentingState(successResult)
                getBikes()
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    fun finishRenting(locationList: ArrayList<GeoPoint>, rideId: String) = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.finishRenting(locationList, rideId, object : IServiceListener<Boolean> {
            override fun onSuccess(successResult: Boolean) {
                setFinishRentingState(successResult)
                getBikes()
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    fun checkUserOnRide() = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.checkUserOnRide(object : IServiceListener<OnRideModel> {
            override fun onSuccess(successResult: OnRideModel) {
                setOnRideState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    fun checkUnpaidRide() = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.checkUnpaidRide(object : IServiceListener<String> {
            override fun onSuccess(successResult: String) {
                setUnpaidRideState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

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

    private fun setGettingBikeState(bikeList: ArrayList<BikeModel>) {
        _homeUiState.update { homeUiState ->
            homeUiState.copy(bikeList = bikeList, lastLocation = null, startRentingId = "", finishRenting = false, unpaidRide = "", userWeight = 0.1, error = "", isLoading = false)
        }
    }

    private fun setLastLocationState(lastLocation: LatLng) {
        _homeUiState.update { homeUiState ->
            homeUiState.copy(bikeList = arrayListOf(), lastLocation = lastLocation, startRentingId = "", finishRenting = false, unpaidRide = "", userWeight = 0.1, error = "", isLoading = false)
        }
    }

    private fun setOnRideState(onRide: OnRideModel) {
        _homeUiState.update { homeUiState ->
            homeUiState.copy(bikeList = arrayListOf(), lastLocation = null, onRide = onRide, startRentingId = "", finishRenting = false, unpaidRide = "", userWeight = 0.1, error = "", isLoading = false)
        }
    }

    private fun setStartRentingState(status: String) {
        _homeUiState.update { homeUiState ->
            homeUiState.copy(bikeList = arrayListOf(), lastLocation = null, startRentingId = status, finishRenting = false, unpaidRide = "", userWeight = 0.1, error = "", isLoading = false)
        }
    }

    private fun setFinishRentingState(status: Boolean) {
        _homeUiState.update { homeUiState ->
            homeUiState.copy(bikeList = arrayListOf(), lastLocation = null, startRentingId = "", finishRenting = status, unpaidRide = "", userWeight = 0.1, error = "", isLoading = false)
        }
    }

    private fun setUnpaidRideState(status: String) {
        _homeUiState.update { homeUiState ->
            homeUiState.copy(bikeList = arrayListOf(), lastLocation = null, startRentingId = "", finishRenting = false, unpaidRide = status, userWeight = 0.1, error = "", isLoading = false)
        }
    }

    private fun setUserWeightState(weight: Double) {
        _homeUiState.update { homeUiState ->
            homeUiState.copy(bikeList = arrayListOf(), lastLocation = null, startRentingId = "", finishRenting = false, unpaidRide = "", userWeight = weight, error = "", isLoading = false)
        }
    }

    private fun setLoadingState() {
        _homeUiState.update { homeUiState ->
            homeUiState.copy(bikeList = arrayListOf(), lastLocation = null, startRentingId = "", finishRenting = false, unpaidRide = "", userWeight = 0.1, error = "", isLoading = true)
        }
    }

    private fun setErrorState(exception: Exception) {
        _homeUiState.update { homeUiState ->
            homeUiState.copy(bikeList = arrayListOf(), lastLocation = null, startRentingId = "", finishRenting = false, unpaidRide = "", userWeight = 0.1, error = exception.toString(), isLoading = false)
        }
    }
}