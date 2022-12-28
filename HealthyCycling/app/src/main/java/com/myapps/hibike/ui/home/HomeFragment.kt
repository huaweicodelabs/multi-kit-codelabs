/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myapps.hibike.ui.home

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.framework.common.ContextCompat.startService
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.ml.scan.HmsScan
import com.myapps.hibike.R
import com.myapps.hibike.databinding.FragmentHomeBinding
import com.myapps.hibike.service.LocationService
import com.myapps.hibike.ui.home.paymentInfoDialog.PaymentInfoDialog
import com.myapps.hibike.ui.scan.ScanActivity
import com.myapps.hibike.utils.Constants
import com.myapps.hibike.utils.extension.hide
import com.myapps.hibike.utils.extension.show
import com.myapps.hibike.utils.extension.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(), OnMapReadyCallback {

    companion object {
        const val DIALOG_TAG = "PaymentInfoDialog"
        const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }

    private lateinit var binding: FragmentHomeBinding
    private var hMap: HuaweiMap? = null
    private lateinit var mMapView: MapView
    private var mapViewBundle: Bundle? = null
    private val homeViewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var agConnectAuth: AGConnectAuth

    private lateinit var rideId: String
    private lateinit var unpaidRideId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)

        initView()
        initListeners()
        onBackPressed()

        initHuaweiMap(savedInstanceState)
        setupObservable()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel.checkUserOnRide()
        homeViewModel.checkUnpaidRide()
        homeViewModel.checkUserWeight()
    }

    private fun initHuaweiMap(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMapView.apply {
            onCreate(mapViewBundle)
            getMapAsync(this@HomeFragment)
        }
    }

    private fun initView() {
        val currentUser = agConnectAuth.currentUser
        with(binding) {
            mMapView = mapView
            tvTitleHome.text =
                requireContext().getString(R.string.welcome) + currentUser.displayName + requireContext().getString(
                    R.string.exclamation_mark
                )
        }
    }

    private fun initListeners() {
        with(binding) {
            ivPaymentInfo.setOnClickListener {
                PaymentInfoDialog().show(
                    parentFragmentManager,
                    DIALOG_TAG
                )
            }
            btnStart.setOnClickListener {
                val intent = Intent(requireContext(), ScanActivity::class.java)
                getResult.launch(intent)
            }
            btnFinish.setOnClickListener {
                finishRenting()
            }
            ivAttention.setOnClickListener {
                val alertDialog = AlertDialog.Builder(requireActivity())
                    .setMessage(getString(R.string.alert_message))
                    .setNegativeButton(getString(R.string.alert_neg)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.alert_pos)) { _, _ ->
                        val bundle = bundleOf(Constants.RIDE_ID to unpaidRideId)
                        findNavController().navigate(R.id.finishFragment, bundle)
                    }.create()
                alertDialog.show()
            }
        }
    }

    private var getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (result.data != null) {
                    (result.data?.getParcelableExtra(ScanActivity.SCAN_RESULT) as HmsScan?).let { hmsScan ->
                        val bikeId = hmsScan?.getOriginalValue()
                        if (bikeId != null) {
                            startRenting(bikeId)
                        }
                    }
                }
            }
        }

    private fun setupObservable() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.homeUiState.collect { homeUiState ->
                    if (homeUiState.isLoading) {
                        binding.homeProgress.show()
                    }
                    if (homeUiState.error.isNotEmpty()) {
                        binding.homeProgress.hide()
                        showToast(homeUiState.error)
                    }
                    if (homeUiState.bikeList.isNotEmpty()) {
                        binding.homeProgress.hide()
                        homeUiState.bikeList.forEach { bike ->
                            if (bike.isRented != true) {
                                val location =
                                    bike.location?.let { LatLng(it.latitude, it.longitude) }
                                val options = MarkerOptions()
                                    .position(location)
                                    .clusterable(true)
                                hMap?.addMarker(options)
                            }
                        }
                    }
                    if (homeUiState.lastLocation != null) {
                        hMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                homeUiState.lastLocation,
                                15f
                            )
                        )
                    }
                    if (homeUiState.onRide?.onRide == true) {
                        rideId = homeUiState.onRide.rideId.toString()
                        with(binding) {
                            btnStart.hide()
                            btnFinish.show()
                        }
                    }
                    if (homeUiState.onRide?.onRide == false) {
                        with(binding) {
                            btnStart.show()
                            btnFinish.hide()
                        }
                    }
                    if (homeUiState.startRentingId.isNotEmpty()) {
                        rideId = homeUiState.startRentingId
                        hMap?.clear()
                        Intent(context, LocationService::class.java).apply {
                            action = LocationService.ACTION_START
                            startService(context, this)
                        }
                        showToast(getString(R.string.start_renting))
                        homeViewModel.checkUserOnRide()
                    }
                    if (homeUiState.finishRenting) {
                        hMap?.clear()
                        val bundle = bundleOf(Constants.RIDE_ID to rideId)
                        findNavController().navigate(R.id.finishFragment, bundle)
                    }
                    if (homeUiState.unpaidRide.isNotEmpty()) {
                        unpaidRideId = homeUiState.unpaidRide
                        binding.ivAttention.show()
                        binding.btnStart.isEnabled = false
                        binding.btnStart.background = requireContext().getDrawable(R.drawable.bg_start_disable)
                    }
                    if (homeUiState.userWeight == 0.0) {
                        showToast(getString(R.string.sub_title_progress))
                    }
                }
            }
        }
    }

    private fun startRenting(bikeId: String) {
        homeViewModel.startRenting(bikeId)
        homeViewModel.checkUserOnRide()
    }

    private fun finishRenting() {
        Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            startService(context, this)
        }
        homeViewModel.finishRenting(LocationService.locationList, rideId)
        homeViewModel.checkUserOnRide()
    }

    private fun onBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity?.finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onMapReady(map: HuaweiMap) {
        hMap = map
        hMap?.apply {
            isMyLocationEnabled = true
            uiSettings?.isMyLocationButtonEnabled = true
            setPadding(0, 0, 0, 200)
            setMarkersClustering(true)

            homeViewModel.getBikes()
            homeViewModel.getLastLocation()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle: Bundle? = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }

        mMapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onStart() {
        super.onStart()
        mMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mMapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }
}