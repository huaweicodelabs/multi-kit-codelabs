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

package com.huawei.hms.couriertracking.data.repository

import com.huawei.hms.couriertracking.core.common.Result
import com.huawei.hms.couriertracking.data.network.CloudDBDatasource
import com.huawei.hms.couriertracking.data.network.NetworkDatasource
import com.huawei.hms.couriertracking.data.network.model.DirectionsNetwork
import com.huawei.hms.couriertracking.data.network.model.DirectionsRequest
import com.huawei.hms.couriertracking.data.network.model.Order
import com.huawei.hms.couriertracking.domain.model.DirectionType
import com.huawei.hms.couriertracking.domain.repository.OrderRepository
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val cloudDBDatasource: CloudDBDatasource,
    private val networkDatasource: NetworkDatasource
) : OrderRepository {
    override suspend fun initializeCloudDB(): Result<Unit> {
        return cloudDBDatasource.initialize()
    }

    override suspend fun getOrdersFromCloudDB(): List<Order> {
        return cloudDBDatasource.getOrders()
    }

    override suspend fun createRoute(
        directionsRequest: DirectionsRequest,
        directionType: DirectionType
    ): DirectionsNetwork {
        return networkDatasource.getDirections(
            directionsRequest,
            directionType.type
        )
    }
}