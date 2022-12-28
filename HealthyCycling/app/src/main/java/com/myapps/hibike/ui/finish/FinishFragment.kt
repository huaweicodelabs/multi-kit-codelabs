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

package com.myapps.hibike.ui.finish

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.GeoPoint
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.maps.model.PolylineOptions
import com.myapps.hibike.R
import com.myapps.hibike.databinding.FragmentFinishBinding
import com.myapps.hibike.ui.home.HomeFragment
import com.myapps.hibike.utils.Constants
import com.myapps.hibike.utils.SecurityUtil
import com.myapps.hibike.utils.extension.hide
import com.myapps.hibike.utils.extension.show
import com.myapps.hibike.utils.extension.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class FinishFragment : Fragment() , OnMapReadyCallback {

    private val finishViewModel: FinishViewModel by viewModels()
    private lateinit var binding: FragmentFinishBinding
    private lateinit var rideId: String
    @Inject
    lateinit var iapClient: IapClient
    private lateinit var mMapView: MapView
    private var mapViewBundle: Bundle? = null
    private var hMap: HuaweiMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFinishBinding.inflate(inflater)

        rideId = arguments?.getString(Constants.RIDE_ID).toString()
        finishViewModel.getLastRide(rideId)
        setupObservable()
        initHuaweiMap(savedInstanceState)
        finishViewModel.updateUnpaidRide(rideId)

        return binding.root
    }

    private fun initHuaweiMap(savedInstanceState: Bundle?) {
        mMapView = binding.mapViewFinishScreen
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(HomeFragment.MAPVIEW_BUNDLE_KEY)
        }
        mMapView.apply {
            onCreate(mapViewBundle)
            getMapAsync(this@FinishFragment)
        }
    }


    private fun initView() {
        val finishUiStateValue = finishViewModel.finishUiState.value
        with(binding) {
            tvAmount.text = finishUiStateValue.amount
            tvDate.text = finishUiStateValue.date
            tvDistance.text = DecimalFormat(getString(R.string.decimal_pattern)).format(finishUiStateValue.totalDistance) + getString(R.string.km)
            if(finishUiStateValue.minute == 0L && finishUiStateValue.second != 0L){
                tvDuration.text = "${finishUiStateValue.hour} h 1 min"
            }
            else tvDuration.text = "${finishUiStateValue.hour} h ${finishUiStateValue.minute} min"
        }
    }

    private fun initListeners() {
        with(binding) {
            ivBackFinishScreen.setOnClickListener {
                findNavController().navigate(R.id.homeFragment)
            }
            btnPayAmount.setOnClickListener {
                if (finishViewModel.finishUiState.value.productId != ""){
                    finishViewModel.gotoPay(finishViewModel.finishUiState.value.productId)
                }
            }
        }
    }

    private var getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    val purchaseResultInfo = iapClient.parsePurchaseResultInfoFromIntent(result.data)
                    if(purchaseResultInfo!=null){
                        when (purchaseResultInfo.returnCode) {
                            OrderStatusCode.ORDER_STATE_SUCCESS -> {
                                val success: Boolean = SecurityUtil.doCheck(
                                    purchaseResultInfo.inAppPurchaseData,
                                    purchaseResultInfo.inAppDataSignature,
                                    Constants.PUBLIC_KEY
                                )
                                if (success) {
                                    finishViewModel.consumeOwnedPurchase(purchaseResultInfo.inAppPurchaseData)
                                } else {
                                    showToast(getString(R.string.pay_success))
                                }
                                updateButtonUi()
                                finishViewModel.updateUnpaidRide("")
                            }
                            OrderStatusCode.ORDER_STATE_CANCEL -> {
                                showToast(getString(R.string.pay_cancel))
                            }
                            else -> {
                                showToast(getString(R.string.pay_fail))
                            }
                        }
                    }
                }
            }
        }

    private fun updateButtonUi(){
       binding.btnPayAmount.apply {
           setBackgroundResource(R.drawable.bg_paid)
           text = getString(R.string.paid)
           isEnabled = false
       }
    }

    private fun renderRoute(list: ArrayList<GeoPoint>?) {
        val newList = arrayListOf<LatLng>()
        list?.forEach { loc ->
            newList.add(LatLng(loc.latitude, loc.longitude))
        }
        hMap?.addPolyline(
            PolylineOptions()
                .addAll(newList)
                .color(Color.RED)
                .width(Constants.POLYLINE_WIDTH)
        )

    }

    private fun addMarkers(list: ArrayList<GeoPoint>?){
        val firstOption = MarkerOptions()
            .position(list?.first()?.let { LatLng(it.latitude, it.longitude) })
            .clusterable(true)
        hMap?.addMarker(firstOption)

        val lastOption = MarkerOptions()
            .position(list?.last()?.let { LatLng(it.latitude, it.longitude) })
            .clusterable(true)
        hMap?.addMarker(lastOption)
    }

    private fun setupObservable() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                finishViewModel.finishUiState.collect { finishUiState ->
                    if (finishUiState.isLoading) {
                        binding.finishProgress.show()
                    }
                    if (finishUiState.error.isNotEmpty()) {
                        binding.finishProgress.hide()
                        showToast(finishUiState.error)
                    }
                    if (finishUiState.lastRide != null) {
                        binding.finishProgress.hide()
                        initView()
                        initListeners()
                        moveMapCamera(finishUiState.lastRide.locationList)
                        renderRoute(finishUiState.lastRide.locationList)
                        addMarkers(finishUiState.lastRide.locationList)
                    }
                    if (finishUiState.iapStatus != null) {
                        binding.finishProgress.hide()
                        if (finishUiState.iapStatus?.hasResolution() == true) {
                            getResult.launch(finishUiState.iapStatus?.resolutionIntent)
                        } else {
                            showToast(getString(R.string.intent_error))
                        }
                        finishUiState.iapStatus = null
                    }
                    if (finishUiState.cancelledPay){
                        binding.finishProgress.hide()
                    }
                }
            }
        }
    }

    private fun moveMapCamera(locationList: ArrayList<GeoPoint>?){
        val midLocation = locationList?.size?.let {
            locationList[it/2]
        }
        hMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(midLocation?.latitude ?: Constants.DEFAULT_DOUBLE, midLocation?.longitude ?: Constants.DEFAULT_DOUBLE),
                Constants.DEFAULT_ZOOM
            )
        )
    }

    override fun onMapReady(map: HuaweiMap?) {
        hMap = map
        hMap?.setMarkersClustering(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle: Bundle? = outState.getBundle(HomeFragment.MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(HomeFragment.MAPVIEW_BUNDLE_KEY, mapViewBundle)
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