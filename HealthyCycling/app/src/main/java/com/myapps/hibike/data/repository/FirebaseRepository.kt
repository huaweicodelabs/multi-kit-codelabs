package com.myapps.hibike.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.huawei.agconnect.auth.AGConnectAuth
import com.myapps.hibike.data.model.BikeModel
import com.myapps.hibike.data.model.OnRideModel
import com.myapps.hibike.data.model.RideModel
import com.myapps.hibike.utils.IServiceListener
import java.util.*
import javax.inject.Inject

class FirebaseRepository @Inject constructor(
    firebaseFirestore: FirebaseFirestore,
    private val agConnectAuth: AGConnectAuth,
    private val calendar: Calendar
) {

    companion object {
        const val USERS = "Users"
        const val BIKES = "Bikes"
        const val USER_NAME = "UserName"
        const val IS_RENTED = "isRented"
        const val LOCATION = "location"
        const val ON_RIDE = "onRide"
        const val ON_RIDE_ID = "onRideId"
        const val START_TIME = "startTime"
        const val FINISH_TIME = "finishTime"
        const val BIKE_ID = "bikeId"
        const val MY_RIDES = "MyRides"
        const val LOCATION_LIST = "locationList"
        const val UNPAID_RIDE = "unpaidRide"
        const val DOC_ID = "id"
        const val AMOUNT = "amount"
        const val DATE = "date"
        const val HOUR = "hour"
        const val MINUTE = "minute"
        const val SECOND = "second"
        const val DISTANCE = "distance"
        const val CALORIE = "calorie"
        const val AVG_SPEED = "avgSpeed"
        const val DURATION = "duration"
        const val WEIGHT = "weight"
        const val IS_INFORMED = "isUserInformed"
    }

    private val usersCollection = firebaseFirestore.collection(USERS)
    private val bikesCollection = firebaseFirestore.collection(BIKES)

    fun createUser(serviceListener: IServiceListener<Boolean>) {
        val user = hashMapOf(
            USER_NAME to agConnectAuth.currentUser.displayName,
            WEIGHT to 0.0,
            IS_INFORMED to false,
            ON_RIDE to false,
            UNPAID_RIDE to ""
        )
        var isCreated = false

        usersCollection.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.id == agConnectAuth.currentUser.uid)
                        isCreated = true
                }
                if (!isCreated) {
                    usersCollection.document(agConnectAuth.currentUser.uid).set(user)
                }
                serviceListener.onSuccess(true)
            }.addOnFailureListener { exception ->
                serviceListener.onError(exception)
            }

    }

    fun getBikes(serviceListener: IServiceListener<ArrayList<BikeModel>>) {
        val bikeList: ArrayList<BikeModel> = arrayListOf()

        bikesCollection.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    bikeList.add(
                        BikeModel(
                            document.id,
                            document.getBoolean(IS_RENTED),
                            document.getGeoPoint(LOCATION)
                        )
                    )
                }
                serviceListener.onSuccess(bikeList)
            }
            .addOnFailureListener { exception ->
                serviceListener.onError(exception)
            }
    }

    fun finishRenting(
        locationList: ArrayList<GeoPoint>,
        rideId: String,
        serviceListener: IServiceListener<Boolean>,
    ) {
        val currentTime = System.currentTimeMillis()
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)

        currentUserDocument.update(
            mapOf(
                ON_RIDE to false
            )
        ).addOnSuccessListener {
            currentUserDocument.collection(MY_RIDES)
                .document(rideId).get()
                .addOnSuccessListener {
                    val lastRide = it.data
                    bikesCollection.document(lastRide?.get(BIKE_ID) as String).update(
                        mapOf(
                            IS_RENTED to false,
                            LOCATION to locationList.last()
                        )
                    ).addOnSuccessListener {
                        currentUserDocument.collection(MY_RIDES)
                            .document(lastRide[DOC_ID] as String)
                            .update(
                                mapOf(
                                    FINISH_TIME to currentTime,
                                    LOCATION_LIST to locationList
                                )
                            ).addOnSuccessListener {
                                serviceListener.onSuccess(true)
                            }.addOnFailureListener { exception ->
                                serviceListener.onError(exception)
                            }
                    }.addOnFailureListener { exception ->
                        serviceListener.onError(exception)
                    }
                }.addOnFailureListener { exception ->
                    serviceListener.onError(exception)
                }
        }.addOnFailureListener { exception ->
            serviceListener.onError(exception)
        }
    }

    fun startRenting(
        bikeId: String,
        serviceListener: IServiceListener<String>
    ) {
        val currentTime = System.currentTimeMillis()
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)

        bikesCollection.document(bikeId).update(
            mapOf(
                IS_RENTED to true
            )
        ).addOnSuccessListener {
            currentUserDocument.update(
                mapOf(
                    ON_RIDE to true
                )
            ).addOnSuccessListener {
                val rideRef = currentUserDocument.collection(MY_RIDES).document()
                rideRef.set(
                    mapOf(
                        DOC_ID to rideRef.id,
                        BIKE_ID to bikeId,
                        START_TIME to currentTime
                    )
                ).addOnSuccessListener {
                    currentUserDocument.update(
                        mapOf(
                            ON_RIDE_ID to rideRef.id
                        )
                    ).addOnSuccessListener {
                        serviceListener.onSuccess(rideRef.id)
                    }.addOnFailureListener { exception ->
                        serviceListener.onError(exception)
                    }
                }.addOnFailureListener { exception ->
                    serviceListener.onError(exception)
                }
            }.addOnFailureListener { exception ->
                serviceListener.onError(exception)
            }
        }.addOnFailureListener { exception ->
            serviceListener.onError(exception)
        }
    }

    fun checkUserOnRide(serviceListener: IServiceListener<OnRideModel>) {
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)

        currentUserDocument.get()
            .addOnSuccessListener {
                serviceListener.onSuccess(
                    OnRideModel(
                        onRide = it.getBoolean(ON_RIDE),
                        rideId = it.getString(ON_RIDE_ID)
                    )
                )
            }
            .addOnFailureListener { exception ->
                serviceListener.onError(exception)
            }
    }

    fun getLastRide(rideId: String, serviceListener: IServiceListener<RideModel?>) {
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)

        currentUserDocument.collection(MY_RIDES)
            .document(rideId)
            .get()
            .addOnSuccessListener { doc ->
                val lastRide = doc.toObject(RideModel::class.java)
                serviceListener.onSuccess(lastRide)
            }.addOnFailureListener { exception ->
                serviceListener.onError(exception)
            }

    }

    fun updateUnpaidRide(rideId: String, serviceListener: IServiceListener<Boolean>) {
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)

        currentUserDocument.update(
            mapOf(
                UNPAID_RIDE to rideId
            )
        ).addOnSuccessListener {
            serviceListener.onSuccess(true)
        }.addOnFailureListener { exception ->
            serviceListener.onError(exception)
        }
    }

    fun checkUnpaidRide(serviceListener: IServiceListener<String>) {
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)

        currentUserDocument.get()
            .addOnSuccessListener {
                it.getString(UNPAID_RIDE)?.let { it1 -> serviceListener.onSuccess(it1) }
            }
            .addOnFailureListener { exception ->
                serviceListener.onError(exception)
            }
    }

    fun updateLastRideInfo(
        rideId: String,
        date: String,
        amount: String,
        hour: Long,
        minute: Long,
        second: Long,
        distance: Double,
        calorie: Double,
        avgSpeed: Double,
        duration: Long,
        serviceListener: IServiceListener<Boolean>
    ) {
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)

        currentUserDocument.collection(MY_RIDES)
            .document(rideId)
            .update(
                mapOf(
                    AMOUNT to amount,
                    DATE to date,
                    HOUR to hour,
                    MINUTE to minute,
                    SECOND to second,
                    DISTANCE to distance,
                    CALORIE to calorie,
                    AVG_SPEED to avgSpeed,
                    DURATION to duration
                )
            ).addOnSuccessListener {
                serviceListener.onSuccess(true)
            }.addOnFailureListener { exception ->
                serviceListener.onError(exception)
            }
    }

    fun getMyLastRides(serviceListener: IServiceListener<ArrayList<RideModel>>) {
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)
        val rideList: ArrayList<RideModel> = arrayListOf()
        val currentTime = System.currentTimeMillis()

        currentUserDocument.collection(MY_RIDES)
            .whereLessThan(START_TIME, currentTime)
            .orderBy(START_TIME, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    rideList.add(
                        RideModel(
                            docId = document.getString(DOC_ID),
                            date = document.getString(DATE),
                            amount = document.getString(AMOUNT),
                            hour = document.getLong(HOUR),
                            minute = document.getLong(MINUTE),
                            second = document.getLong(SECOND),
                            distance = document.getDouble(DISTANCE)
                        )
                    )
                }
                serviceListener.onSuccess(rideList)
            }.addOnFailureListener { exception ->
                serviceListener.onError(exception)
            }

    }

    fun getWeeklyRides(serviceListener: IServiceListener<ArrayList<RideModel>>) {
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)
        val rideList: ArrayList<RideModel> = arrayListOf()
        val currentTime = System.currentTimeMillis()

        currentUserDocument.collection(MY_RIDES)
            .whereLessThan(START_TIME, currentTime)
            .orderBy(START_TIME, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val startTime = document.getLong(START_TIME)
                    if (startTime != null) {
                        if (startTime > calendar.timeInMillis && startTime < calendar.timeInMillis.plus(
                                604800000
                            )
                        )
                            rideList.add(
                                document.toObject(RideModel::class.java)
                            )
                    }
                }
                serviceListener.onSuccess(rideList)
            }.addOnFailureListener { exception ->
                serviceListener.onError(exception)
            }
    }

    fun updateUserWeight(weight: Double, serviceListener: IServiceListener<Boolean>) {
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)

        currentUserDocument.update(
            mapOf(
                WEIGHT to weight
            )
        ).addOnSuccessListener {
            serviceListener.onSuccess(true)
        }.addOnFailureListener { exception ->
            serviceListener.onError(exception)
        }
    }

    fun checkUserWeight(serviceListener: IServiceListener<Double>) {
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)

        currentUserDocument.get()
            .addOnSuccessListener {
                it.getDouble(WEIGHT)?.let { it1 -> serviceListener.onSuccess(it1) }
            }
            .addOnFailureListener { exception ->
                serviceListener.onError(exception)
            }
    }

    fun updateUserInformed(status: Boolean, serviceListener: IServiceListener<Boolean>) {
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)

        currentUserDocument.update(
            mapOf(
                IS_INFORMED to status
            )
        ).addOnSuccessListener {
            serviceListener.onSuccess(status)
        }.addOnFailureListener { exception ->
            serviceListener.onError(exception)
        }
    }

    fun checkUserInformed(serviceListener: IServiceListener<Boolean>) {
        val currentUserDocument = usersCollection.document(agConnectAuth.currentUser.uid)

        currentUserDocument.get()
            .addOnSuccessListener {
                it.getBoolean(IS_INFORMED)?.let { it1 -> serviceListener.onSuccess(it1) }
            }
            .addOnFailureListener { exception ->
                serviceListener.onError(exception)
            }
    }

}