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

package com.myapps.hibike.ui.finish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huawei.hms.support.api.client.Status
import com.myapps.hibike.data.model.RideModel
import com.myapps.hibike.data.repository.FirebaseRepository
import com.myapps.hibike.data.repository.HealthRepository
import com.myapps.hibike.data.repository.IAPRepository
import com.myapps.hibike.utils.CalculationHelper
import com.myapps.hibike.utils.Constants
import com.myapps.hibike.utils.IServiceListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.pow

@HiltViewModel
class FinishViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val iapRepository: IAPRepository,
    private val healthRepository: HealthRepository
) : ViewModel() {

    private val _finishUiState: MutableStateFlow<FinishUiState> =
        MutableStateFlow(FinishUiState.initial())
    val finishUiState: StateFlow<FinishUiState> = _finishUiState.asStateFlow()

    fun getLastRide(rideId: String) = viewModelScope.launch {
        setLoadingState()
        checkUserWeight()
        firebaseRepository.getLastRide(rideId, object : IServiceListener<RideModel?> {
            override fun onSuccess(successResult: RideModel?) {
                successResult?.let {
                    val date = it.startTime?.let { startTime ->
                        CalculationHelper.convertTimeMillisToTimeAndDate(startTime)
                    }
                    val duration = it.startTime?.let { startTime ->
                        it.finishTime?.minus(startTime)
                    }
                    val minHour = duration?.let { it1 -> CalculationHelper.calculateDuration(it1) }
                    val seconds = minHour?.get(Constants.SECONDS)
                    val minutes = minHour?.get(Constants.MINUTES)
                    val hours = minHour?.get(Constants.HOURS)
                    val result =
                        minutes?.let { minute ->
                            hours?.let { hours ->
                                CalculationHelper.decideAmountByDuration(
                                    minute,
                                    hours
                                )
                            }
                        }
                    val amount = result?.get(Constants.AMOUNT)
                    val productId = result?.get(Constants.PRODUCT_ID)
                    val totalDist = it.locationList?.let { locationList ->
                        CalculationHelper.calculateTotalDistance(
                            locationList
                        )
                    }?.times(1.60934)
                    val avgSpeed = duration?.let { it1 -> totalDist?.div(it1/1000) }
                    val calorie = if(_finishUiState.value.userWeight != 0.0){
                         duration?.let { _duration ->
                            avgSpeed?.let { avgSpeed ->
                                calculateCalories(
                                    _duration,
                                    _finishUiState.value.userWeight,
                                    avgSpeed
                                )
                            }
                        }
                    } else 0.0
                    if (it.finishTime != null && amount != null && productId != null && date != null && seconds != null && hours != null && totalDist != null && calorie != null && avgSpeed != null) {
                        setLastRideState(it, date, amount, seconds, minutes, hours, productId, totalDist)
                        updateLastRideInfo(rideId, date, amount, hours, minutes, seconds, totalDist, calorie, avgSpeed, duration)
                        addActivityRecord(rideId, it.startTime, it.finishTime, totalDist.toFloat(), avgSpeed.toFloat(), calorie.toFloat())
                    }
                }
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    fun updateLastRideInfo(rideId: String, date: String, amount: String, hour: Long, minute: Long, second: Long, distance: Double, calorie: Double, avgSpeed: Double, duration: Long) = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.updateLastRideInfo(rideId,date, amount, hour, minute, second, distance, calorie, avgSpeed, duration, object : IServiceListener<Boolean> {
            override fun onSuccess(successResult: Boolean) {
                setLastRideInfoState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    fun updateUnpaidRide(rideId: String) = viewModelScope.launch {
        setLoadingState()
        firebaseRepository.updateUnpaidRide(rideId, object : IServiceListener<Boolean> {
            override fun onSuccess(successResult: Boolean) {
                setCancelledPaymentState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    fun gotoPay(productId: String) = viewModelScope.launch {
        setLoadingState()
        iapRepository.gotoPay(productId, object : IServiceListener<Status> {
            override fun onSuccess(successResult: Status) {
                setIAPState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    fun consumeOwnedPurchase(purchaseData: String) = viewModelScope.launch {
        setLoadingState()
        iapRepository.consumeOwnedPurchase(purchaseData, object : IServiceListener<Boolean> {
            override fun onSuccess(successResult: Boolean) {
                setPurchaseState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    private fun addActivityRecord(uniqueId: String, startTime: Long, endTime: Long, distance: Float, avgSpeed: Float, calorie: Float) = viewModelScope.launch {
        setLoadingState()
        healthRepository.addActivityRecord(uniqueId, startTime, endTime, distance, avgSpeed, calorie, object : IServiceListener<Boolean> {
            override fun onSuccess(successResult: Boolean) {
                setActivityRecordState(successResult)
            }

            override fun onError(exception: Exception) {
                setErrorState(exception)
            }
        })
    }

    private fun checkUserWeight() = viewModelScope.launch {
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

    private fun setUserWeightState(weight: Double) {
        _finishUiState.update { finishUiState ->
            finishUiState.copy(userWeight = weight, isLoading = false)
        }
    }

    private fun calculateCalories(
        duration: Long,
        weight: Double,
        avgSpeed: Double
    ): Double {
        val minutes = (duration / 1000).toDouble() / 60
        return (minutes * (getMET(avgSpeed) * 3.5 * weight) / 200)
    }

    private fun getMET(avgSpeed: Double): Double {
        val speedInKmh: Double = avgSpeed * 3.6
        return max(
                3.5,
                0.00818 * speedInKmh.pow(2.0) + 0.1925 * speedInKmh + 1.13
            )
    }


    private fun setLastRideState(
        lastRide: RideModel,
        date: String,
        amount: String,
        seconds: Long,
        minutes: Long,
        hours: Long,
        productId: String,
        totalDist: Double,
    ) {
        _finishUiState.update { finishUiState ->
            finishUiState.copy(
                lastRide = lastRide,
                date = date,
                amount = amount,
                second = seconds,
                minute = minutes,
                hour = hours,
                productId = productId,
                totalDistance = totalDist,
                iapStatus = null,
                isLoading = false
            )
        }
    }

    private fun setLastRideInfoState(status: Boolean) {
        _finishUiState.update { finishUiState ->
            finishUiState.copy(lastRide = null, lastRideInfoState = status, isLoading = false)
        }
    }

    private fun setCancelledPaymentState(canceledPayment: Boolean) {
        _finishUiState.update { finishUiState ->
            finishUiState.copy(lastRide = null, cancelledPay = canceledPayment, isLoading = false)
        }
    }

    private fun setIAPState(status: Status) {
        _finishUiState.update { finishUiState ->
            finishUiState.copy(lastRide = null, iapStatus = status, isLoading = false)
        }
    }

    private fun setPurchaseState(status: Boolean) {
        _finishUiState.update { finishUiState ->
            finishUiState.copy(lastRide = null, purchaseStatus = status, isLoading = false)
        }
    }

    private fun setActivityRecordState(status: Boolean) {
        _finishUiState.update { finishUiState ->
            finishUiState.copy(activityRecordStatus = status, isLoading = false)
        }
    }

    private fun setLoadingState() {
        _finishUiState.update { finishUiState ->
            finishUiState.copy(lastRide = null, isLoading = true)
        }
    }

    private fun setErrorState(exception: Exception) {
        _finishUiState.update { finishUiState ->
            finishUiState.copy(lastRide = null, error = exception.toString(), isLoading = false)
        }
    }
}