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

package com.huawei.hms.couriertracking.data.network.cloud

import android.content.Context
import com.huawei.agconnect.AGCRoutePolicy
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.hms.couriertracking.core.common.Result
import com.huawei.hms.couriertracking.data.network.CloudDBDatasource
import com.huawei.hms.couriertracking.data.network.model.ObjectTypeInfoHelper
import com.huawei.hms.couriertracking.data.network.model.Order
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

class CloudDBDatasourceImpl @Inject constructor(
    private val context: Context
) : CloudDBDatasource {

    private lateinit var mCloudDB: AGConnectCloudDB
    private var handler: CompletableDeferred<Result<Unit>>? = null
    private var cloudDBZone: CloudDBZone? = null

    override suspend fun initialize(): Result<Unit> {
        handler = CompletableDeferred()
        AGConnectCloudDB.initialize(context)
        initializeCloudDB()
        initializeZone()
        handler?.let { return it.await() }
            ?: run { return Result.Error() }
    }

    private fun initializeCloudDB() {
        val instance = AGConnectInstance.buildInstance(
            AGConnectOptionsBuilder().setRoutePolicy(
                AGCRoutePolicy.GERMANY
            ).build(context)
        )
        mCloudDB = AGConnectCloudDB.getInstance(instance, AGConnectAuth.getInstance())
        mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo())
    }

    private fun initializeZone() {
        val mConfig = CloudDBZoneConfig(
            "CourierDbZone",
            CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
            CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
        )
        mConfig.persistenceEnabled = true
        val task = mCloudDB.openCloudDBZone2(mConfig, true)
        task.addOnSuccessListener {
            cloudDBZone = it
            handler?.complete(Result.Success(Unit))
        }.addOnFailureListener {
            handler?.complete(Result.Error(it.message))
        }
    }

    override suspend fun getOrders(): List<Order> {
        val result = CompletableDeferred<List<Order>>()
        cloudDBZone?.let { dbZone ->
            dbZone.executeQuery(
                CloudDBZoneQuery.where(Order::class.java),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_DEFAULT
            ).addOnCompleteListener{
                if(it.isSuccessful) {
                    val cursor = it.result.snapshotObjects
                    val orders = mutableListOf<Order>()
                    while(cursor.hasNext()) {
                        orders.add(cursor.next())
                    }
                    result.complete(orders)
                }else {
                    throw it.exception
                }
            }
        }?: run {
            throw Exception("Cloud DB not initialized.")
        }
        return result.await()
    }
}