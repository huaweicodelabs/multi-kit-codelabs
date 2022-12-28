package com.myapps.hibike.utils

import android.annotation.SuppressLint
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

object CalculationHelper {

    @SuppressLint("SimpleDateFormat")
    fun convertTimeMillisToTimeAndDate(startTime: Long): String {
        val simpleDateFormat = SimpleDateFormat(Constants.DATE_TIME_PATTERN)
        return simpleDateFormat.format(startTime)
    }

    @SuppressLint("SimpleDateFormat")
    fun convertTimeMillisToDate(startTime: Long): String {
        val simpleDateFormat = SimpleDateFormat(Constants.DATE_PATTERN)
        return simpleDateFormat.format(startTime)
    }

    fun decideAmountByDuration(minutes: Long, hours: Long): Map<String, String> {
        var amount = ""
        var productId = ""
        if (minutes < 30){
            amount = Constants.TRY5
            productId = Constants.PRODUCT1
        }
        if (minutes in 30..59){
            amount = Constants.TRY8
            productId = Constants.PRODUCT2
        }
        if (hours == 1L && minutes < 30){
            amount = Constants.TRY10
            productId = Constants.PRODUCT3
        }
        if (hours == 1L && minutes in 31..59){
            amount = Constants.TRY12
            productId = Constants.PRODUCT4
        }
        if (hours == 2L && minutes in 1..59){
            amount = Constants.TRY15
            productId = Constants.PRODUCT5
        }
        if (hours >= 3L && minutes in 1..59){
            amount = Constants.TRY20
            productId = Constants.PRODUCT6
        }

        return mapOf(
            Constants.AMOUNT to amount,
            Constants.PRODUCT_ID to productId
        )
    }

    fun calculateDuration(diff: Long): Map<String, Long> {
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return mapOf(
            Constants.SECONDS to seconds % 60,
            Constants.MINUTES to minutes % 60,
            Constants.HOURS to hours
        )
    }

    fun calculateTotalDistance(locationList: ArrayList<GeoPoint>): Double{
        var i = 1
        var total = 0.0
        locationList.forEach{ item ->
            if (item != locationList.last()){
                val nextItem=locationList[i++]
                val dist = distanceBetweenTwoLocation(item.latitude, item.longitude, nextItem.latitude, nextItem.longitude)
                total += dist
            }
        }

        return total
    }

    private fun distanceBetweenTwoLocation(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = 0.0
        if (theta != 0.0){
            dist = (sin(deg2rad(lat1))
                    * sin(deg2rad(lat2))
                    + (cos(deg2rad(lat1))
                    * cos(deg2rad(lat2))
                    * cos(deg2rad(theta))))
            dist = acos(dist)
            dist = rad2deg(dist)
            dist *= 60 * 1.1515
        }
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }
}