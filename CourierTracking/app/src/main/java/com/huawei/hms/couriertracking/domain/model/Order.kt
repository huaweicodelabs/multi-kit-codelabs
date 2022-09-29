package com.huawei.hms.couriertracking.domain.model

data class Order(
    val id:String,
    val title:String,
    val description:String,
    val photoUrl:String,
    val price:String,
    val status:Int,
    val storeLocation: StoreLocation
)
