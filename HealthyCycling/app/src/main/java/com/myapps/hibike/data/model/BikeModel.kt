package com.myapps.hibike.data.model

import com.google.firebase.firestore.GeoPoint

data class BikeModel (
    val id: String?,
    val isRented: Boolean?,
    val location: GeoPoint?
)