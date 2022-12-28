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

package com.myapps.hibike.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapps.hibike.data.model.RideModel
import com.myapps.hibike.data.repository.FirebaseRepository
import com.myapps.hibike.data.repository.HealthRepository
import com.myapps.hibike.data.repository.PushNotifRepository
import com.myapps.hibike.utils.CalculationHelper
import com.myapps.hibike.utils.Constants
import com.myapps.hibike.utils.IPushNotifServiceListener
import com.myapps.hibike.utils.IServiceListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val pushNotifRepository: PushNotifRepository,
    private val healthRepository: HealthRepository
) : ViewModel() {

    private val _progressUiState: MutableStateFlow<ProgressUiState> = MutableStateFlow(
        ProgressUiState.initial()
    )
    val progressUiState: StateFlow<ProgressUiState> = _progressUiState.asStateFlow()

    fun getWeeklyRides() = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.getWeeklyRides(object : IServiceListener<ArrayList<RideModel>> {
            override fun onSuccess(successResult: ArrayList<RideModel>) {
                getAccessToken(successResult)
                getTotalCalorie()
                checkUserInformed()
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

    private fun getAccessToken(rideList: ArrayList<RideModel>) = viewModelScope.launch {
        setLoadingState()
        pushNotifRepository.getAccessToken(object : IPushNotifServiceListener<String> {
            override fun onSuccess(successResult: String) {

                var totalTime: Long? = 0
                var time = "0:0:0"
                var totalDistance: Double? = 0.0
                var totalRide = 0
                var avgSpeed: Double? = 0.0

                if(rideList.isNotEmpty()){
                    totalRide = rideList.size
                    rideList.forEach { ride ->
                        totalDistance = ride.distance?.let { totalDistance?.plus(it) }
                        avgSpeed = ride.avgSpeed?.let { avgSpeed?.plus(it) }
                        totalTime = ride.duration?.let { totalTime?.plus(it) }
                    }
                    val minHour = totalTime?.let { CalculationHelper.calculateDuration(it) }
                    val seconds = minHour?.get(Constants.SECONDS)
                    val minutes = minHour?.get(Constants.MINUTES)
                    val hours = minHour?.get(Constants.HOURS)
                    time = "$hours:$minutes:$seconds"

                    avgSpeed = avgSpeed?.div(totalRide.toDouble())
                }

                setSuccessState(rideList, time, totalDistance, totalRide, avgSpeed, successResult)
            }

            override fun onError(throwable: Throwable) {
                setThrowableState(throwable)
            }
        })
    }

    fun sendNotification(accessToken: String, pushToken: String, title: String, body: String) = viewModelScope.launch {
        setLoadingState()
        pushNotifRepository.sendNotification(accessToken, pushToken, title, body, object : IPushNotifServiceListener<Boolean> {
            override fun onSuccess(successResult: Boolean) {
                setNotificationState(successResult)
            }

            override fun onError(throwable: Throwable) {
                setThrowableState(throwable)
            }
        })
    }

    fun getTotalCalorie() = viewModelScope.launch {
        setLoadingState()
        healthRepository.getActivityRecordsForTotalCalorie(object : IServiceListener<Float> {
            override fun onSuccess(successResult: Float) {
                setTotalCalorieState(successResult)
            }

            override fun onError(exception: Exception) {
                setThrowableState(exception)
            }
        })
    }

    fun updateUserInformed(status: Boolean) = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.updateUserInformed(status, object : IServiceListener<Boolean> {
            override fun onSuccess(successResult: Boolean) {
                setUserInformedState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    fun checkUserInformed() = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.checkUserInformed(object : IServiceListener<Boolean> {
            override fun onSuccess(successResult: Boolean) {
                setUserInformedState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    private fun setUserInformedState(status: Boolean) {
        _progressUiState.update { progressUiState ->
            progressUiState.copy(userWeight = 0.1, userInformedStatus = status, isLoading = false)
        }
    }

    private fun setNotificationState(status: Boolean) {
        _progressUiState.update { progressUiState ->
            progressUiState.copy(userInformedStatus = true, userWeight = 0.1, isNotifSuccess = status, isLoading = false)
        }
    }

    private fun setUserWeightState(weight: Double) {
        _progressUiState.update { progressUiState ->
            progressUiState.copy( userWeight = weight, isLoading = false)
        }
    }

    private fun setSuccessState(rideList: ArrayList<RideModel>, time: String, totalDistance: Double?, totalRide: Int, avgSpeed: Double?, accessToken: String) {
        _progressUiState.update { progressUiState ->
            progressUiState.copy(rideList = rideList, time = time, totalDistance = totalDistance, totalRide = totalRide, avgSpeed = avgSpeed, userWeight = 0.1, accessToken = accessToken, isLoading = false)
        }
    }

    private fun setTotalCalorieState(totalCalorie: Float) {
        _progressUiState.update { progressUiState ->
            progressUiState.copy(totalCalorie = totalCalorie, userWeight = 0.1, isLoading = false)
        }
    }

    private fun setLoadingState() {
        _progressUiState.update { progressUiState ->
            progressUiState.copy(rideList = null, userInformedStatus = null, userWeight = 0.1, isLoading = true)
        }
    }

    private fun setErrorState(exception: Exception) {
        _progressUiState.update { progressUiState ->
            progressUiState.copy(error = exception.toString(),  isLoading = false)
        }
    }

    private fun setThrowableState(throwable: Throwable) {
        _progressUiState.update { progressUiState ->
            progressUiState.copy(throwable = throwable.message.toString(), isLoading = false)
        }
    }
}