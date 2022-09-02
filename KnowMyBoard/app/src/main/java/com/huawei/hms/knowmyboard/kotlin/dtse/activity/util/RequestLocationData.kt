package com.huawei.hms.knowmyboard.dtse.activity.util

import android.Manifest
import com.huawei.hms.knowmyboard.dtse.activity.viewmodel.LoginViewModel
import android.app.Activity
import android.content.Context
import android.os.Build
import android.content.pm.PackageManager
import android.os.Looper
import android.location.Geocoder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.huawei.hms.location.*
import java.io.IOException
import java.lang.StringBuilder
import java.util.*

class RequestLocationData(
    context: Context?,
    activity: FragmentActivity?,
    loginViewModel: LoginViewModel?
) {
    private var settingsClient: SettingsClient? = null
    private var isLocationSettingSuccess = 0
    private var myLocationRequest: LocationRequest? = null

    // Define a fusedLocationProviderClient object.
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var myLocationCallback: LocationCallback? = null
    var context: Context? = null
    var activity: Activity? = null
    private var locationResult: LocationResult? = null
    var loginViewModel: LoginViewModel? = null
    fun initFusionLocationProviderClint() {
        // Instantiate the fusedLocationProviderClient object.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        this.settingsClient = LocationServices.getSettingsClient(activity)
    }

    fun checkDeviceLocationSettings() {
        val builder = LocationSettingsRequest.Builder()
        myLocationRequest = LocationRequest()
        builder.addLocationRequest(myLocationRequest)
        val locationSettingsRequest = builder.build()
        // Check the device location settings.
        settingsClient!!.checkLocationSettings(locationSettingsRequest) // Define the listener for success in calling the API for checking device location settings.
            .addOnSuccessListener { locationSettingsResponse: LocationSettingsResponse ->
                val locationSettingsStates = locationSettingsResponse.locationSettingsStates
                val stringBuilder = StringBuilder()
                // Check whether the location function is enabled.
                stringBuilder.append(",\nisLocationUsable=")
                    .append(locationSettingsStates.isLocationUsable)
                // Check whether HMS Core (APK) is available.
                stringBuilder.append(",\nisHMSLocationUsable=")
                    .append(locationSettingsStates.isHMSLocationUsable)
                Log.i(TAG, "checkLocationSetting onComplete:$stringBuilder")
                // Set the location type.
                myLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                // Set the number of location updates to 1.
                myLocationRequest!!.numUpdates = 1
                isLocationSettingSuccess = 1
            } // Define callback for failure in checking the device location settings.
            .addOnFailureListener { e -> Log.i(TAG, "checkLocationSetting onFailure:" + e.message) }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun checkPermission() {
        // Dynamically apply for required permissions if the API level is 28 or lower.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(TAG, "android sdk <= 28 Q")
            if (ActivityCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.MANAGE_MEDIA,
                    Manifest.permission.MEDIA_CONTENT_CONTROL,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                ActivityCompat.requestPermissions(activity!!, strings, 1)
            }
        } else {
            // Dynamically apply for the android.permission.ACCESS_BACKGROUND_LOCATION permission in addition to the preceding permissions if the API level is higher than 28.
            if (ActivityCompat.checkSelfPermission(
                    activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context!!,
                    "android.permission.ACCESS_BACKGROUND_LOCATION"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.MEDIA_CONTENT_CONTROL,
                    Manifest.permission.MANAGE_MEDIA,
                    "android.permission.ACCESS_BACKGROUND_LOCATION"
                )
                ActivityCompat.requestPermissions(activity!!, strings, 2)
            }
        }
    }

    fun refreshLocation(): LocationResult? {
        Log.d(TAG, "Refreshing location")
        if (isLocationSettingSuccess == 1) {
            myLocationCallback = object : LocationCallback() {
                private var locationResult: LocationResult? = null
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult != null) {
                        // Gson gson = new Gson();
                        //Log.d(TAG, " Location data :" + locationResult.getLastLocation().getLatitude() + " : " + locationResult.getLastLocation().getLongitude());
                        //Log.d(TAG, " Location data :" + gson.toJson(locationResult.getLastHWLocation()));
                        //Log.d(TAG, " Location data :" + locationResult.getLastHWLocation().getCountryName());
                        Log.d(TAG, " Location data :" + locationResult.lastHWLocation.latitude)
                        Log.d(TAG, " Location data :" + locationResult.lastHWLocation.longitude)
                        // binding.textDetected.setText("Latitude " + locationResult.getLastHWLocation().getLatitude() + " Longitude " + locationResult.getLastHWLocation().getLongitude());
                        //getGeoCoderValues(locationResult.getLastHWLocation().getLatitude(),locationResult.getLastHWLocation().getLongitude());
                        this.locationResult = locationResult
                        loginViewModel!!.setLocationResult(locationResult)
                    }
                }
            }
            fusedLocationProviderClient!!.requestLocationUpdates(
                myLocationRequest,
                myLocationCallback,
                Looper.getMainLooper()
            )
        } else {
            Log.d(TAG, "Failed to get location settings")
        }
        return locationResult
    }

    fun disableLocationData() {
        fusedLocationProviderClient!!.disableBackgroundLocation()
        fusedLocationProviderClient!!.removeLocationUpdates(myLocationCallback)
    }

    private fun getGeoCoderValues(latitude: Double, longitude: Double) {
        getAddress(context, latitude, longitude)
        /*  Geocoder geocoder;
        List<Address> addresses;
        Locale locale = new Locale("en", "IN");
        geocoder = new Geocoder(getContext(), locale);

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            Gson gson=new Gson();
            Log.d(TAG,"Geo coder :"+gson.toJson(addresses));
        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Error while fetching Geo coder :"+e.getMessage());
        }*/
        /* Locale locale = new Locale("en", "IN");
        GeocoderService geocoderService =
                LocationServices.getGeocoderService(getActivity().getBaseContext(), locale);
        // Request reverse geocoding.
        GetFromLocationRequest getFromLocationRequest = new GetFromLocationRequest(latitude, longitude, 5);
        // Initiate reverse geocoding.
        geocoderService.getFromLocation(getFromLocationRequest)
                .addOnSuccessListener(hwLocation -> {
                    Gson gson=new Gson();
                    Log.d(TAG,"Geo coder :"+gson.toJson(hwLocation));

                })
                .addOnFailureListener(e -> {

                    Log.e(TAG,"Error while fetching Geo coder :"+e.getMessage());
                });*/
    }

    companion object {
        var TAG = "TAG"
        fun getAddress(context: Context?, LATITUDE: Double, LONGITUDE: Double) {
            //Set Address
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
                if (addresses != null && addresses.size > 0) {
                    val address =
                        addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    val city = addresses[0].locality
                    val state = addresses[0].adminArea
                    val country = addresses[0].countryName
                    val postalCode = addresses[0].postalCode
                    val knownName = addresses[0].featureName // Only if available else return NULL
                    Log.d(TAG, "getAddress:  address$address")
                    Log.d(TAG, "getAddress:  city$city")
                    Log.d(TAG, "getAddress:  state$state")
                    Log.d(TAG, "getAddress:  postalCode$postalCode")
                    Log.d(TAG, "getAddress:  knownName$knownName")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Error while fetching Geo coder :" + e.message)
            }
        }
    }

    init {
        this@RequestLocationData.context = context
        this@RequestLocationData.activity = activity
        this@RequestLocationData.loginViewModel = loginViewModel
    }
}