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

package com.myapps.hibike.data.repository

import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq
import com.huawei.hms.iap.entity.InAppPurchaseData
import com.huawei.hms.iap.entity.PurchaseIntentReq
import com.huawei.hms.support.api.client.Status
import com.myapps.hibike.utils.IServiceListener
import javax.inject.Inject

class IAPRepository @Inject constructor(
    private val iapClient: IapClient
) {

    companion object{
        const val TEST = "test"
    }

    fun gotoPay(productId: String?, serviceListener: IServiceListener<Status>) {
        val req = PurchaseIntentReq()
        req.let { productDetails ->
            productDetails.productId = productId
            productDetails.priceType = IapClient.PriceType.IN_APP_CONSUMABLE
            productDetails.developerPayload = TEST
        }
        val task = iapClient.createPurchaseIntent(req)
        task.addOnSuccessListener{ result ->
            serviceListener.onSuccess(result.status)
        }.addOnFailureListener { e ->
            serviceListener.onError(e)
        }
    }

    fun consumeOwnedPurchase(purchaseData: String, serviceListener: IServiceListener<Boolean>) {
        val req = ConsumeOwnedPurchaseReq()
        val inAppPurchaseData = InAppPurchaseData(purchaseData)
        req.purchaseToken = inAppPurchaseData.purchaseToken
        val task = iapClient.consumeOwnedPurchase(req)
        task.addOnSuccessListener {
            serviceListener.onSuccess(true)
        }.addOnFailureListener { e ->
            serviceListener.onError(e)
        }
    }

}