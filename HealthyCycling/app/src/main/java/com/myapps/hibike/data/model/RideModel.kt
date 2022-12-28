package com.myapps.hibike.data.model

import com.google.firebase.firestore.GeoPoint

data class RideModel (
    val bikeId: String? = "",
    val startTime: Long? = 0,
    val finishTime: Long? = 0,
    val locationList: ArrayList<GeoPoint>? = null,
    val docId: String? = "",
    val date: String? = "",
    val amount: String? = "",
    val hour: Long? = 0,
    val minute: Long? = 0,
    val second: Long? = 0,
    val duration: Long? = 0,
    val distance: Double? = 0.0,
    val calorie: Double? = 0.0,
    val avgSpeed: Double? = 0.0
)