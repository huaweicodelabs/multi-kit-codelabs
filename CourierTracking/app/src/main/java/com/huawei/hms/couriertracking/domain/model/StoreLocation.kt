package com.huawei.hms.couriertracking.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoreLocation(
    val latitude: Double,
    val longitude: Double
): Parcelable
