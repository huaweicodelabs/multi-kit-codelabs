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

package com.huawei.hms.couriertracking.domain.usecase

import android.location.Location
import com.huawei.hms.couriertracking.core.common.Result
import com.huawei.hms.couriertracking.data.network.model.DirectionsRequest
import com.huawei.hms.couriertracking.data.network.model.LatLngData
import com.huawei.hms.couriertracking.domain.mapper.DirectionNetworkMapper
import com.huawei.hms.couriertracking.domain.model.DirectionType
import com.huawei.hms.couriertracking.domain.model.Route
import com.huawei.hms.couriertracking.domain.model.StoreLocation
import com.huawei.hms.couriertracking.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class CreateRouteUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        storeLocation: StoreLocation,
        lastLocation: Location?,
        directionType: DirectionType
    ): Flow<Result<Route>> = flow {
        try {
            lastLocation?.let { currentLocation ->
                val paths = orderRepository.createRoute(
                    DirectionsRequest(
                        LatLngData(storeLocation.latitude, storeLocation.longitude),
                        LatLngData(currentLocation.latitude, currentLocation.longitude)
                    ),
                    directionType
                )
                val route = DirectionNetworkMapper.networkModelToExternalModel(
                    paths,
                    currentLocation,
                    storeLocation
                )
                emit(Result.Success(route))
            } ?: run { emit(Result.Error("Couldn't get last position")) }
        } catch (e: IOException) {
            emit(Result.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}