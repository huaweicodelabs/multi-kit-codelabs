/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.huawei.hms.couriertracking.ui.courier_tracking

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huawei.hms.couriertracking.core.common.Result
import com.huawei.hms.couriertracking.domain.model.DirectionType
import com.huawei.hms.couriertracking.domain.model.StoreLocation
import com.huawei.hms.couriertracking.domain.usecase.CreateRouteUseCase
import com.huawei.hms.couriertracking.ui.courier_tracking.CourierTrackingViewState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourierTrackingViewModel @Inject constructor(
    private val createRouteUseCase: CreateRouteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CourierTrackingViewState>(None)
    val uiState = _uiState.asStateFlow()

    fun createRoute(
        storeLocation: StoreLocation,
        lastLocation: Location?,
        directionType: DirectionType
    ) = viewModelScope.launch {
        _uiState.value = Loading
        createRouteUseCase(
            storeLocation,
            lastLocation,
            directionType
        ).collect { result ->
            when (result) {
                is Result.Success -> {
                    _uiState.value = Success(result.data)
                }
                is Result.Error -> { _uiState.value = Error(result.error?:"An error occurred") }
            }
        }
    }
}