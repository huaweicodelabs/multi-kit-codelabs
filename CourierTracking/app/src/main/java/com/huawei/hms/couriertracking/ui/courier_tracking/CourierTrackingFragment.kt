/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.huawei.hms.couriertracking.ui.courier_tracking

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.huawei.agconnect.function.AGConnectFunction
import com.huawei.hms.couriertracking.R
import com.huawei.hms.couriertracking.core.utils.PermissionHelper.hasLocationPermissions
import com.huawei.hms.couriertracking.core.utils.PermissionHelper.requestLocationPermissions
import com.huawei.hms.couriertracking.databinding.FragmentCourierTrackingBinding
import com.huawei.hms.couriertracking.domain.model.DirectionType
import com.huawei.hms.couriertracking.domain.model.Route
import com.huawei.hms.couriertracking.ui.courier_tracking.CourierTrackingViewState.*
import com.huawei.hms.location.*
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CourierTrackingFragment : Fragment() {

    private var _binding: FragmentCourierTrackingBinding? = null
    private val binding get() = _binding!!

    private val courierTrackingViewModel: CourierTrackingViewModel by viewModels()
    private val args by navArgs<CourierTrackingFragmentArgs>()

    private var hMap: HuaweiMap? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCourierTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        initHuaweiMap(savedInstanceState)
        checkPermission()
        collectUIState()
    }

    private fun collectUIState() {
        lifecycleScope.launch {
            courierTrackingViewModel.uiState.collect { uiState ->
                when (uiState) {
                    is Success -> {
                        addMarker(
                            uiState.route.startDestination,
                            "Customer",
                            R.drawable.ic_customer
                        )
                        addMarker(
                            uiState.route.endDestination,
                            "Store",
                            R.drawable.ic_store
                        )
                        addPolylines(uiState.route)
                        binding.progressBarCourierTracking.visibility = View.GONE
                        lifecycleScope.launch {
                            starSimulation(uiState.route.pathPoints)
                        }
                    }
                    Loading -> {
                        binding.progressBarCourierTracking.visibility = View.VISIBLE
                    }
                    is Error -> {
                        Toast.makeText(requireContext(), uiState.errorMessage, Toast.LENGTH_SHORT)
                            .show()
                        binding.progressBarCourierTracking.visibility = View.GONE
                    }
                }
            }
        }
    }

    private suspend fun starSimulation(pathPoints: List<LatLng>?) {
        var marker: Marker?
        pathPoints?.let { paths ->
            paths.forEach { latLng ->
                marker = addMarker(latLng,"Courier",R.drawable.scooter_icon_128)
                animateCamera(latLng)
                delay(100)
                marker?.remove()
            }
        }
        sendNotification()
    }

    private fun sendNotification() {
        val pushToken = activity?.getSharedPreferences(
            "device_token", Context.MODE_PRIVATE
        )?.getString("device_token",null)
        pushToken?.let { token ->
            val parameterMap: HashMap<String, String> = HashMap()
            parameterMap["deviceToken"] = token
            AGConnectFunction.getInstance()
                .wrap("courier-tracking-notification-\$latest")
                .call(parameterMap)
        }
        findNavController().popBackStack()
    }

    private fun animateCamera(latLng: LatLng){
        hMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng,
                15f
            )
        )
    }

    private fun addMarker(latLng: LatLng, title: String, icon: Int): Marker? {
        val options = MarkerOptions()
            .position(latLng)
            .title(title)
            .icon(BitmapDescriptorFactory.fromResource(icon))
        return hMap?.addMarker(options)
    }

    private fun addPolylines(route: Route) {
        route.pathPoints?.let { pathPoints ->
            val options = PolylineOptions()
            pathPoints.forEach { latLng ->
                options.add(latLng)
            }
            options.color(
                ContextCompat.getColor(
                    requireContext(), R.color.yellow_700
                )
            )
            options.width(5f)
            hMap?.addPolyline(options)
        }
    }

    private fun checkPermission() {
        if (hasLocationPermissions(requireContext())) {
            getLastLastLocation()
        } else requestLocationPermissions(requireActivity())
    }

    private fun getLastLocation() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.numUpdates = 1
        fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.getMainLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            courierTrackingViewModel.createRoute(
                args.storeLocation,
                locationResult.lastLocation,
                DirectionType.DRIVING
            )
        }
    }

    private fun initHuaweiMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle("MapViewBundleKey")
        }
        binding.mapView.apply {
            onCreate(mapViewBundle)
            getMapAsync {
                hMap = it
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }
}