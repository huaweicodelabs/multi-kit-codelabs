/*
 *
 *  * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */
@file:Suppress("TooManyFunctions","MagicNumber","WildcardImport","TooGenericExceptionCaught")
package com.huawei.hms.urbanhomeservices.kotlin.fragments.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapsInitializer
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.*
import com.huawei.hms.maps.model.CameraPosition
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.ActivityNearByStoresLocationBinding
import com.huawei.hms.urbanhomeservices.kotlin.fragments.home.HomeViewModel
import com.huawei.hms.urbanhomeservices.kotlin.listener.ActivityUpdateListner
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import com.huawei.hms.urbanhomeservices.kotlin.utils.NetworkRequestManager
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 *This class displays route  between Consumer and Service Provider.
 *
 * @author: Huawei
 * @since 20-01-21
 */

class NearByStoresLocationFragment : Fragment(), OnMapReadyCallback {
    companion object {
        private const val TAG: String = "NearByStoresLocationFragment"
    }
    private var hMap: HuaweiMap? = null
    private var lat: Double? = 0.0
    private var lng: Double? = 0.0
    private var storeAddress: String? = null
    private var storeName: String? = null
    private lateinit var homeViewModel: HomeViewModel
    private val mPaths: MutableList<List<LatLng>> = ArrayList()
    private var mLatLngBounds: LatLngBounds? = null
    private lateinit var currentLocLatLng: LatLng
    private var destLocLatLng: LatLng? = null
    private val mPolylines: MutableList<Polyline> = ArrayList()
    private var mMarkerOrigin: Marker? = null
    private var mMarkerDestination: Marker? = null
    private lateinit var activityUpdateListner: ActivityUpdateListner

    private var _bindingNearByStoreLoc : ActivityNearByStoresLocationBinding?=null
    private val bindingNearByStoreLoc get() = _bindingNearByStoreLoc!!

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                0 -> renderRoute(mPaths, mLatLngBounds)
                1 -> {
                    val bundle = msg.data
                    val errorMsg = bundle.getString("errorMsg")
                    Utils.showToast(activity, "$errorMsg")
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityUpdateListner = context as ActivityUpdateListner
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View?{
        _bindingNearByStoreLoc = ActivityNearByStoresLocationBinding.inflate(inflater, container, false)
        return bindingNearByStoreLoc.root
    }
    @SuppressLint("SetTextI18n")
    @ExperimentalStdlibApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(AppConstants.MAPVIEW_BUNDLE_KEY)
        }
        MapsInitializer.setApiKey(AppConstants.API_KEY)
        bindingNearByStoreLoc.mapView.onCreate(mapViewBundle)
        bindingNearByStoreLoc.mapView.getMapAsync(this)
        arguments?.apply {
            lat = getDouble(AppConstants.SERVICE_LAT_KEY)
            lng = getDouble(AppConstants.SERVICE_LNG_KEY)
            storeName = getString(AppConstants.SERVICE_STORE_NAME_KEY)
            storeAddress = getString(AppConstants.SERVICE_ADDR_KEY)
        }
        initViewModel()
    }

    /**
     * Initialize HomeViewModel class
     */
    private fun initViewModel() {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        bindingNearByStoreLoc.mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        bindingNearByStoreLoc.mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        bindingNearByStoreLoc.mapView.onResume()
        activityUpdateListner.hideShowNavBar(true, getString(R.string.nearby_search_services_providers))
    }

    override fun onStop() {
        super.onStop()
        bindingNearByStoreLoc.mapView.onStop()
    }
    @SuppressWarnings("checkstyle:magicnumber")
    override fun onMapReady(huaweiMap: HuaweiMap) {
        currentLocLatLng = LatLng(Utils.curentLatitude, Utils.currentLongitude)
        destLocLatLng = lat?.let { lng?.let { it1 -> LatLng(it, it1) } }
        hMap = huaweiMap
        hMap?.isMyLocationEnabled = true
        val build = CameraPosition.Builder().target(currentLocLatLng).zoom(2f).tilt(45f).build()
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(build)
        hMap?.apply {
            animateCamera(cameraUpdate)
            moveCamera(cameraUpdate)
        }
        addOriginMarker(currentLocLatLng)
        destLocLatLng?.let {
            addDestinationMarker(it)
        }
        removePolylines()
        hMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocLatLng, 13f))
        mMarkerOrigin?.showInfoWindow()
        hMap?.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(destLocLatLng, 13f))
            resetMinMaxZoomPreference()
        }
        mMarkerDestination?.showInfoWindow()
        drivingRouteResult()
    }

    /**
     * To get driving route
     */
    private fun drivingRouteResult() {
        currentLocLatLng.let {
            destLocLatLng?.let { it1 ->
                NetworkRequestManager.getDrivingRoutePlanningResult(
                        it, it1,
                        object : NetworkRequestManager.OnNetworkListener {
                            override fun requestSuccess(result: String?) {
                                result?.let {
                                    generateRoute(result)
                                }
                            }

                            override fun requestFail(errorMsg: String?) {
                                val msg = Message.obtain()
                                val bundle = Bundle()
                                bundle.putString(AppConstants.ERROR_MSG_KEY, errorMsg)
                                msg.what = AppConstants.INTIAL_VALUE
                                msg.data = bundle
                                mHandler.sendMessage(msg)
                            }
                        })
            }
        }
    }
    @Suppress("ComplexMethod","NestedBlockDepth")
    /**
     * To generate Route
     *
     * @param json route json string
     */
    private fun generateRoute(json: String) {
        try {
            val jsonObject = JSONObject(json)
            val routes = jsonObject.optJSONArray(AppConstants.ROUTES_KEY)
            if (null == routes || routes.length() == AppConstants.INTIAL_VALUE) {
                return
            }
            val route = routes.getJSONObject(AppConstants.INTIAL_VALUE)
            val bounds = route.optJSONObject(AppConstants.BOUNDS_KEY)
            if (null != bounds && bounds.has(AppConstants.BOUNDS_SOUTHWEST_KEY)
                    && bounds.has(AppConstants.BOUNDS_NORTHEAST_KEY)) {
                val southwest: JSONObject? = bounds.optJSONObject(AppConstants.BOUNDS_SOUTHWEST_KEY)
                val northeast: JSONObject? = bounds.optJSONObject(AppConstants.BOUNDS_NORTHEAST_KEY)
                val sw = southwest?.optDouble(AppConstants.LATITUDE_KEY)?.let {
                    LatLng(it, southwest.optDouble(AppConstants.LONGITUDE_KEY))
                }
                val ne = northeast?.optDouble(AppConstants.LATITUDE_KEY)?.let {
                    LatLng(it, northeast.optDouble(AppConstants.LONGITUDE_KEY))
                }
                mLatLngBounds = LatLngBounds(sw, ne)
            }
            val paths: JSONArray? = route.optJSONArray(AppConstants.PATHS_KEY)
            if (paths != null) {
                for (i in 0 until paths.length()) {
                    val path = paths.optJSONObject(i)
                    val mPath: MutableList<LatLng> = ArrayList()
                    val steps: JSONArray? = path.optJSONArray(AppConstants.STEPS_KEY)
                    if (steps != null) {
                        for (j in 0 until steps.length()) {
                            val step = steps.optJSONObject(j)
                            val polyline: JSONArray? = step.optJSONArray(AppConstants.POLYLINE_KEY)
                            if (polyline != null) {
                                for (k in 0 until polyline.length()) {
                                    if (j > 0 && k == 0) {
                                        continue
                                    }
                                    val line = polyline.getJSONObject(k)
                                    val lat = line.optDouble(AppConstants.LATITUDE_KEY)
                                    val lng = line.optDouble(AppConstants.LONGITUDE_KEY)
                                    val latLng = LatLng(lat, lng)
                                    mPath.add(latLng)
                                }
                            }
                        }
                    }
                    mPaths.add(i, mPath)
                }
            }
            mHandler.sendEmptyMessage(AppConstants.INTIAL_VALUE)
        } catch (e: JSONException) {
            Log.e(tag, "JSONException")
        }
    }
    @SuppressWarnings("checkstyle:magicnumber")
    /**
     * Render the route planning result
     * @param paths path of the route
     * @param latLngBounds latlng bounds of path
     */
    private fun renderRoute(paths: List<List<LatLng>>?, latLngBounds: LatLngBounds?) {
        if (null == paths || paths.isEmpty() || paths[0].isEmpty()) {
            return
        }
        for (i in paths.indices) {
            val path = paths[i]
            val options = PolylineOptions().color(Color.BLUE).width(5f)
            for (latLng in path) {
                options.add(latLng)
            }
            val polyline = hMap?.addPolyline(options)
            if (polyline != null) {
                mPolylines.add(i, polyline)
            }
        }
        addOriginMarker(paths[0][0])
        addDestinationMarker(paths[0][paths[0].size - 1])
        if (null != latLngBounds) {
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 5)
            hMap?.moveCamera(cameraUpdate)
        } else {
            hMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(paths[0][0], 11f))
        }
    }
    @SuppressWarnings("checkstyle:magicnumber")
    /**
     * This method shows marker for Consumer location on HMS Map
     * @param latLng latitude and longitude of origin marker
     */
    private fun addOriginMarker(latLng: LatLng) {
        if (null != mMarkerOrigin) {
            mMarkerOrigin?.remove()
        }
        val address = getCompleteAddressString(Utils.curentLatitude, Utils.currentLongitude)
        mMarkerOrigin = hMap?.addMarker(
                MarkerOptions().position(latLng)
                        .anchorMarker(0.5f, 0.9f)
                        .title(getString(R.string.title_current_location))
                        .snippet(address)
        )
    }
    @SuppressWarnings("checkstyle:magicnumber")
    /**
     * This method shows marker for Service Provider location on HMS Map
     * @param latLng latitude and longitude of marker
     */
    private fun addDestinationMarker(latLng: LatLng) {
        if (null != mMarkerDestination) {
            mMarkerDestination?.remove()
        }
        mMarkerDestination = hMap?.addMarker(
                MarkerOptions().position(latLng).anchorMarker(0.5f, 0.9f).title(storeName)
                        .snippet(storeAddress)
        )
    }

    /**
     * This method removes road map between Consumer and Service Provider
     */
    private fun removePolylines() {
        for (polyline in mPolylines) {
            polyline.remove()
        }
        mPolylines.clear()
        mPaths.clear()
        mLatLngBounds = null
    }
    @Suppress("TooGenericExceptionCaught","FunctionParameterNaming")
    /**
     * This method fetches the complete address by using Lat and Long
     *
     * @param LATITUDE latitude of address
     * @param LONGITUDE longitude of address
     * @return String address of given latitude and longitude
     */
    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(activity, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            addresses?.let {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = java.lang.StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(AppConstants.NEW_LINE)
                }
                strAdd = strReturnedAddress.toString()
                Log.w(tag, "Address fetched successfully")
            }
        } catch (e: Exception) {
            Log.w(tag, "Can not get address")
        }
        return strAdd
    }

    override fun onLowMemory() {
        super.onLowMemory()
        bindingNearByStoreLoc.mapView.onLowMemory()
    }

}
