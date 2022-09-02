package com.huawei.hms.knowmyboard.dtse.activity.fragments

import androidx.navigation.Navigation.findNavController
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.knowmyboard.dtse.activity.viewmodel.LoginViewModel
import android.content.SharedPreferences
import androidx.navigation.NavController
import com.huawei.hms.knowmyboard.dtse.activity.util.RequestLocationData
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.site.api.model.Site
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.huawei.hms.location.LocationResult
import com.huawei.hms.maps.MapsInitializer
import android.annotation.SuppressLint
import android.Manifest.permission
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.huawei.hms.knowmyboard.dtse.R
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.knowmyboard.dtse.databinding.FragmentLoginBinding
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions
import java.lang.Exception

class LoginFragment : Fragment(), OnMapReadyCallback {
    var loginBinding: FragmentLoginBinding? = null
    var loginViewModel: LoginViewModel? = null
    var menu: Menu? = null
    var prefs: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var navController: NavController? = null
    private val MY_PREF_NAME = "my_pref_name"
    private val TAG = "TAG"
    private var siteMarker: Marker? = null
    var locationData: RequestLocationData? = null
    var hMap: HuaweiMap? = null
    var search = 0
    var latLng = LatLng(1.0, 2.0)
    private var site: Site? = null
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loginBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        loginViewModel = ViewModelProvider(requireActivity()).get(
            LoginViewModel::class.java
        )
        loginBinding?.loginViewModel = loginViewModel
        locationData = RequestLocationData(context, activity, loginViewModel)
        locationData!!.loginViewModel = loginViewModel
        locationData!!.initFusionLocationProviderClint()
        locationData!!.checkPermission()
        locationData!!.checkDeviceLocationSettings()
        Log.d(TAG, " Pref $preferenceValue")
        if (preferenceValue != "user_name") {
            enableMenu(menu)
            requireActivity().title = preferenceValue
        }
        initMap(savedInstanceState)
        loginViewModel!!.siteSelected.observeForever { site1 ->
            site = site1
            search = 1
        }
        loginViewModel!!.message.observeForever { message ->
            updateMessage(message)
            if (message != resources.getString(R.string.app_name)) {
                preferenceValue = message
                enableMenu(menu)
            } else {
                disableMenu(menu)
                preferenceValue = "user_name"
            }
        }
        loginViewModel!!.locationResult.observeForever { locationResult ->
            refreshLocation(
                locationResult
            )
        }
        return loginBinding?.root
    }

    private fun initMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle("MapViewBundleKey")
        }
        loginBinding!!.mapview.onCreate(mapViewBundle)
        loginBinding!!.mapview.getMapAsync(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // Initialize the SDK.
        MapsInitializer.initialize(context)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main, menu)
        this.menu = menu
        disableMenu(menu)
    }

    private fun disableMenu(menu: Menu?) {
        try {
            if (menu != null) {
                if (preferenceValue == "user_name") {
                    menu.findItem(R.id.menu_login_logout).isVisible = false
                    menu.findItem(R.id.menu_cancel_auth).isVisible = false
                    menu.findItem(R.id.menu_login).isVisible = true
                    requireActivity().title = resources.getString(R.string.app_name)
                } else {
                    menu.findItem(R.id.menu_login_logout).isVisible = true
                    menu.findItem(R.id.menu_cancel_auth).isVisible = true
                    menu.findItem(R.id.menu_login).isVisible = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun enableMenu(menu: Menu?) {
        try {
            menu!!.findItem(R.id.menu_login_logout).isVisible = true
            menu.findItem(R.id.menu_cancel_auth).isVisible = true
            menu.findItem(R.id.menu_login).isVisible = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_cancel_auth -> {
                preferenceValue = "user_name"
                loginViewModel!!.cancelAuthorization()
                // loginBinding.btnHuaweiIdAuth.setVisibility(View.VISIBLE);
                disableMenu(menu)
                return true
            }
            R.id.menu_login_logout -> {
                preferenceValue = "user_name"
                loginViewModel!!.logoutHuaweiID()
                // loginBinding.btnHuaweiIdAuth.setVisibility(View.VISIBLE);
                disableMenu(menu)
                return true
            }
            R.id.menu_login -> {
                loginViewModel!!.loginClicked()
                return true
            }
            R.id.option_refresh_location -> {
                locationData!!.refreshLocation()
                return true
            }
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    fun updateMessage(msg: String?) {
        requireActivity().title = msg
    }

    var preferenceValue: String?
        get() {
            prefs = requireActivity().getSharedPreferences(MY_PREF_NAME, Context.MODE_PRIVATE)
            return prefs?.getString("user_name", "user_name")
        }
        set(userName) {
            editor = requireActivity().getSharedPreferences(MY_PREF_NAME, Context.MODE_PRIVATE).edit()
            editor!!.putString("user_name", userName)
            editor!!.apply()
        }

    @RequiresPermission(allOf = [permission.ACCESS_FINE_LOCATION, permission.ACCESS_WIFI_STATE])
    override fun onMapReady(huaweiMap: HuaweiMap) {
        hMap = huaweiMap
        if (search == 1) {
            Log.d(TAG, "SEARCH IS ON")
            if (site != null) {
                addMarker(site!!)
            } else {
                Log.d(TAG, "SITE IS NULL")
            }
        } else {
            try {
                Log.d(TAG, "SEARCH IS OFF")
                Log.d(TAG, "Map ready")
                onHuaweiMapReady()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onHuaweiMapReady() {
        if (hMap != null) {
            hMap!!.mapType = HuaweiMap.MAP_TYPE_NORMAL
            hMap!!.isBuildingsEnabled = true
            // Enable the my-location layer.
            hMap!!.isMyLocationEnabled = true
            // Enable the my-location icon.
            hMap!!.uiSettings.isMyLocationButtonEnabled = true
            hMap!!.uiSettings.setGestureScaleByMapCenter(true)
        }
        if (locationData != null) {
            locationData!!.refreshLocation()
        }
    }

    private fun refreshLocation(locationResult: LocationResult) {
        try {
            latLng = LatLng(
                locationResult.lastHWLocation.latitude,
                locationResult.lastHWLocation.longitude
            )
            moveCamera(latLng, 10.1f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun moveCamera(latLng: LatLng, zoomRate: Float) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomRate)
        hMap!!.animateCamera(cameraUpdate)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        loginBinding!!.mapview.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        loginBinding!!.mapview.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        loginBinding!!.mapview.onStop()
        locationData!!.disableLocationData()
    }

    override fun onPause() {
        super.onPause()
        loginBinding!!.mapview.onPause()
    }

    override fun onStart() {
        super.onStart()
        loginBinding!!.mapview.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState")
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        try {
            loginBinding!!.mapview.onSaveInstanceState(mapViewBundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController(view)
        Log.d(TAG, "onViewCreated")
    }

    fun addMarker(site: Site) {
        if (null != siteMarker) {
            siteMarker!!.remove()
        }
        val latLng = LatLng(site.location.lat, site.location.lng)
        val options = MarkerOptions()
            .position(latLng)
            .title(site.name)
            .snippet(site.formatAddress)
        siteMarker = hMap!!.addMarker(options)
        hMap!!.addMarker(options)
        moveCamera(latLng, 14f)
    }

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }
}