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
@file:Suppress("TooManyFunctions","TooGenericExceptionCaught")
package com.huawei.hms.urbanhomeservices.kotlin.fragments.home

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.FragmentHomeKBinding
import com.huawei.hms.urbanhomeservices.kotlin.adapter.ServiceCatListAdapter
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.CloudDBZoneWrapper
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.ServiceCategory
import com.huawei.hms.urbanhomeservices.kotlin.fragments.searchservice.SiteKitResultFragment
import com.huawei.hms.urbanhomeservices.kotlin.listener.ActivityUpdateListner
import com.huawei.hms.urbanhomeservices.kotlin.searchbar.MaterialSearchBar
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils.curentLatitude
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils.currentLongitude
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils.hideKeyboard
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils.latLng
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 *HomeFragment  will open first time
 * Based on current location, we are fetching services from Cloud DB
 * Search Services from Huawei Site Kit
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

class HomeFragment : Fragment(), MaterialSearchBar.OnSearchActionListener,
        CloudDBZoneWrapper.UiCallBack<ServiceCategory>, View.OnClickListener {
    companion object {
        private const val TAG: String = "HomeFragment"
    }
    private var mCloudDBZoneWrapper: CloudDBZoneWrapper<ServiceCategory>? = null
    private var serviceCategory: ServiceCategory? = null
    private lateinit var query: CloudDBZoneQuery<ServiceCategory>
    private lateinit var activityUpdateListner: ActivityUpdateListner
    private lateinit var homeViewModel: HomeViewModel
    private var imageType: String? = null
    private var isEnableSearch: Boolean = false
    private var _bindingHomeFragment : FragmentHomeKBinding?=null
    private val bindingHomeFragment get() = _bindingHomeFragment!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ):View? {
       _bindingHomeFragment = FragmentHomeKBinding.inflate(inflater, container, false)

        return bindingHomeFragment.root
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)
//    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initCloudDb()
        initUI()
    }

    /**
     *Initialize Cloud DB to fetch data from Cloud DB
     */
    private fun initCloudDb() {
        serviceCategory = ServiceCategory()
        mCloudDBZoneWrapper = CloudDBZoneWrapper()
        mCloudDBZoneWrapper?.setCloudObject(serviceCategory)
        mCloudDBZoneWrapper?.createObjectType()
    }

    /**
     * Initialize UI
     * Initialize View model
     */
    private fun initUI() {
        Utils.isProfileFragment = false
        activity?.supportFragmentManager?.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        bindingHomeFragment.llPlumber.setOnClickListener(this)
        bindingHomeFragment.llElectrician.setOnClickListener(this)
        bindingHomeFragment.llAppliance.setOnClickListener(this)
        bindingHomeFragment.llCarpenter.setOnClickListener(this)
        bindingHomeFragment.llCleaner.setOnClickListener(this)
        bindingHomeFragment.llPainter.setOnClickListener(this)
        initViewModel()
        initRecyclerViewService()
        startLocationUpdate()
        bindingHomeFragment.searchBar.apply {
            setOnSearchActionListener(this@HomeFragment)
            setCardViewElevation(0)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityUpdateListner = context as ActivityUpdateListner
    }

    /**
     * Initialize RecyclerView for HMS Site Kit search
     */
    private fun initRecyclerViewService() {
        bindingHomeFragment.serviceGridRV.apply {
            layoutManager = GridLayoutManager(activity, 2)
            isNestedScrollingEnabled = false
            hasFixedSize()
            itemAnimator = DefaultItemAnimator()
        }
    }

    /**
     * Initializing ViewModel class and write query to fetch data from Cloud DB
     */
    private fun initViewModel() {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        activity?.let {
            mCloudDBZoneWrapper?.setmUiCallBack(this)
            mCloudDBZoneWrapper?.openCloudDBZoneV2()
            query = CloudDBZoneQuery.where(ServiceCategory::class.java)
        }
    }

    /**
     * Current Location from HMS Location Kit
     * When location change, live data will update
     */
    @DelicateCoroutinesApi
    private fun startLocationUpdate() {
        GlobalScope.launch(Dispatchers.Main) {
            homeViewModel.getLocationData().observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    latLng(it.latitud, it.longitude)
                    getCompleteAddressString(it.latitud, it.longitude)
                }
            })
        }
    }
    @Suppress("TooGenericExceptionCaught","FunctionParameterNaming")
    /**
     * Get Address from Lat , lang
     *
     * @param LATITUDE  latitude of address
     * @param LONGITUDE longitude of address
     */
    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(activity, Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            addresses.let {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = java.lang.StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(AppConstants.NEW_LINE)
                }
                strAdd = strReturnedAddress.toString()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Can not get address exception")
        }
        return strAdd
    }

    /**
     * Fetch service list from Cloud DB
     * Update in Adapter
     *
     * @param serviceCatList list of service category
     */
    private fun updateAdapter(serviceCatList: MutableList<ServiceCategory>?) {
        bindingHomeFragment.serviceGridRV.adapter = ServiceCatListAdapter(requireActivity(), serviceCatList)
    }

    override fun onButtonClicked(buttonCode: Int) {
        when (buttonCode) {
            MaterialSearchBar.BUTTON_BACK -> bindingHomeFragment.searchBar.closeSearch()
        }
    }

    /**
     *Based on User action on SearchBar, hide/show Service list of categories
     * and Search list from HMS Site Kit
     */
    override fun onSearchStateChanged(enabled: Boolean) {
        if (enabled) {
            bindingHomeFragment.dropDownLL.visibility = View.VISIBLE
            bindingHomeFragment.bgView.visibility = View.VISIBLE
        } else {
            bindingHomeFragment.dropDownLL.visibility = View.GONE
            bindingHomeFragment.bgView.visibility = View.GONE
        }
    }

    /**
     * Based on User search keyword, fetch data from HMS Site Kit
     */
    @ExperimentalStdlibApi
    @Suppress("ComplexCondition")
    override fun onSearchConfirmed(text: CharSequence?) {
        var queryString = text.toString()
        if (queryString.equals(AppConstants.SERVICE_TYPE_PLUMBER, true)) {
            queryString = AppConstants.PLUMBE
        }
        if (queryString.startsWith(AppConstants.PLUMBE, true)
                || queryString.startsWith(AppConstants.ELECTRICAL, true)
                || queryString.equals(AppConstants.CLEANER, true)
                || queryString.equals(AppConstants.SERVICE_TYPE_HOUSEKEEPER, true)
                || queryString.equals(AppConstants.SERVICE_TYPE_PAINTER, true)
                || queryString.equals(AppConstants.SERVICE_TYPE_CARPENTER, true)
                || queryString.equals(AppConstants.SERVICE_TYPE_APPLIANCE_REPAIR, true)) {
            val fragmentTransaction: FragmentTransaction =
                    requireActivity().supportFragmentManager.beginTransaction()
            val bundle = Bundle()
            bundle.apply {
                putString(AppConstants.REQUEST_QUERY, queryString)
                putDouble(AppConstants.SERVICE_LAT_KEY, curentLatitude)
                putDouble(AppConstants.SERVICE_LNG_KEY, currentLongitude)
                putString(AppConstants.PROVIDER_IMAGE_KEY, imageType)
            }
            val mFrag: Fragment = SiteKitResultFragment()
            mFrag.arguments = bundle
            fragmentTransaction.apply {
                add(R.id.nav_host_fragment, mFrag)
                addToBackStack(getString(R.string.app_name))
                commit()
            }
        } else {
            bindingHomeFragment.searchBar.text = ""
            bindingHomeFragment.searchBar.openSearch()
            hideKeyboard()
            Utils.showLongToast(
                    requireActivity(),
                    "${queryString.capitalize(Locale.ROOT)} ${getString(R.string.no_data_found)}"
            )
        }
    }
    override fun onAddOrQuery(dbZoneList: MutableList<ServiceCategory>?) {
        updateAdapter(dbZoneList)
    }
    @Suppress("EmptyFunctionBlock")
    override fun onSubscribe(dbZoneList: MutableList<ServiceCategory>?) {
        Log.w(TAG, "onSubscribe")
    }
    @Suppress("EmptyFunctionBlock")
    override fun onDelete(dbZoneList: MutableList<ServiceCategory>?) {
        Log.w(TAG, "onDelete")
    }
    @Suppress("EmptyFunctionBlock")
    override fun updateUiOnError(errorMessage: String?) {
        Log.w(TAG, "updateUiOnError")
    }
    override fun onInitCloud() {
        mCloudDBZoneWrapper?.queryAllData(query)
    }
    @Suppress("EmptyFunctionBlock")
    override fun onInsertSuccess(cloudDBZoneResult: Int?) {
        Log.w(TAG, "onInsertSuccess")
    }
    @ExperimentalStdlibApi
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ll_plumber -> {
                isEnableSearch = true
                onSearchConfirmed(AppConstants.PLUMBE)
                bindingHomeFragment.searchBar.text = AppConstants.SERVICE_TYPE_PLUMBER

            }
            R.id.ll_electrician -> {
                isEnableSearch = true
                onSearchConfirmed(AppConstants.ELECTRICAL)
                bindingHomeFragment.searchBar.text = AppConstants.ELECTRICAL
            }
            R.id.ll_cleaner -> {
                isEnableSearch = true
                onSearchConfirmed(AppConstants.CLEANER)
                bindingHomeFragment.searchBar.text = AppConstants.CLEANER
            }
            R.id.ll_painter -> {
                isEnableSearch = true
                onSearchConfirmed(AppConstants.SERVICE_TYPE_PAINTER)
                bindingHomeFragment.searchBar.text = AppConstants.SERVICE_TYPE_PAINTER
            }
            R.id.ll_carpenter -> {
                isEnableSearch = true
                onSearchConfirmed(AppConstants.SERVICE_TYPE_CARPENTER)
                bindingHomeFragment.searchBar.text = AppConstants.SERVICE_TYPE_CARPENTER
            }
            R.id.ll_appliance -> {
                isEnableSearch = true
                onSearchConfirmed(AppConstants.SERVICE_TYPE_APPLIANCE_REPAIR)
                bindingHomeFragment.searchBar.text = AppConstants.SERVICE_TYPE_APPLIANCE_REPAIR
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityUpdateListner.hideShowNavBar(false, getString(R.string.app_name))
    }
}

