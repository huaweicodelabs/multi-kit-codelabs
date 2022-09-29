package com.huawei.hms.couriertracking.domain.mapper

import com.huawei.hms.couriertracking.core.common.EntityMapper
import com.huawei.hms.couriertracking.domain.model.Order
import com.huawei.hms.couriertracking.domain.model.StoreLocation
import com.huawei.hms.couriertracking.data.network.model.Order as OrderCloud

object OrderMapper : EntityMapper<Order,OrderCloud> {
    override fun toEntity(model: OrderCloud): Order {
        return Order(
            id = model.id,
            title = model.productTitle,
            description = model.productDescription,
            photoUrl = model.productPhotoUrl,
            price = "${model.productPrice}$",
            status = model.status,
            storeLocation = StoreLocation(model.storeLat,model.storeLng)
        )
    }

    override fun fromEntity(entity: Order): OrderCloud {
        return OrderCloud().apply {
            id = entity.id
            productTitle = entity.title
            productDescription = entity.description
            productPhotoUrl = entity.photoUrl
            productPrice = entity.price.filter { it.isDigit() || it == '.' }.toDouble()
            status = entity.status
            storeLat = entity.storeLocation.latitude
            storeLng = entity.storeLocation.longitude
        }
    }
}