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
@file:Suppress("TooManyFunctions")
package com.huawei.hms.urbanhomeservices.kotlin.fragments.searchservice

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.FragmentServiceSuggestionListKBinding
import com.huawei.hms.urbanhomeservices.kotlin.adapter.SearchServiceDbListAdapter
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.CloudDBZoneWrapper
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.ServiceType
import com.huawei.hms.urbanhomeservices.kotlin.listener.ActivityUpdateListner
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils
import java.util.*

/**
 * Fetch Service list from Cloud DB and update into Adapter class
 * To check Service details
 *
 * @author: Huawei
 * @since 20-01-21
 */

class SearchServiceListDbFragment : Fragment(), CloudDBZoneWrapper.UiCallBack<ServiceType> {
    private var serviceImg: String? = null
    private var mCloudDBZoneWrapper: CloudDBZoneWrapper<ServiceType>? = null
    private var serviceType: ServiceType? = null
    private lateinit var query: CloudDBZoneQuery<ServiceType>
    private lateinit var serviceName: String
    private lateinit var activityUpdateListner: ActivityUpdateListner
    companion object {
        private const val TAG: String = "SearchServiceListDbFragment"
    }
    private var _fragmentServiceListBinding: FragmentServiceSuggestionListKBinding? = null
    private val fragmentServiceListBinding get() = _fragmentServiceListBinding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityUpdateListner = context as ActivityUpdateListner
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _fragmentServiceListBinding = FragmentServiceSuggestionListKBinding.inflate(inflater, container, false)
        return fragmentServiceListBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecyclerView()
        initCloudDB()
        fetchServiceFromCloudDB()
    }

    /**
     * Initialize Cloud DB
     */
    private fun initCloudDB() {
        serviceType = ServiceType()
        mCloudDBZoneWrapper = CloudDBZoneWrapper()
        mCloudDBZoneWrapper?.setCloudObject(serviceType)
        mCloudDBZoneWrapper?.createObjectType()
    }

    /**
     * Load service
     * from site Api by service cat name
     */
    private fun fetchServiceFromCloudDB() {
        arguments.let {
            serviceImg = it?.getString(AppConstants.PROVIDER_IMAGE_KEY)
            mCloudDBZoneWrapper?.setmUiCallBack(this)
            mCloudDBZoneWrapper?.openCloudDBZoneV2()
            query = CloudDBZoneQuery.where(ServiceType::class.java)
            query.equalTo(AppConstants.COUNTRY_STR, Utils.getSharePrefCountry(requireActivity()))
            serviceName = it?.getString(AppConstants.SEARCH_NAME_KEY).toString()
            query.beginsWith(AppConstants.CAT_NAME, serviceName.substring(AppConstants.INTIAL_VALUE,
                    serviceName.length.minus(AppConstants.INITIAL_VALUE_ONE)).replace(" ", ""))
        }
    }

    /**
     * Based on Query And condition, fetch data
     * from  cloudDB
     * Initialize  Recycler view
     * load service by category
     */
    private fun initRecyclerView() {
        fragmentServiceListBinding.serviceRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            isNestedScrollingEnabled = false
            hasFixedSize()
            itemAnimator = DefaultItemAnimator()
        }
    }

    override fun onSubscribe(dbZoneList: MutableList<ServiceType>?) {
        Log.w(tag, "onSubscribe")
    }

    override fun onDelete(dbZoneList: MutableList<ServiceType>?) {
        Log.w(tag, "onDelete")
    }

    override fun updateUiOnError(errorMessage: String?) {
        Log.w(tag, "updateUiOnError")
    }

    /**
     * Fetch data from Cloud DB And update
     * into list
     */
    @ExperimentalStdlibApi
    override fun onAddOrQuery(dbZoneList: MutableList<ServiceType>?) {
        updateAdapter(dbZoneList)
    }

    /**
     * Make query fetch data from Cloud Db
     */
    override fun onInitCloud() {
        Log.w(tag, "onInit")
        mCloudDBZoneWrapper?.queryAllData(query)
    }
    @Suppress("EmptyFunctionBlock")
    override fun onInsertSuccess(cloudDBZoneResult: Int?) {
        Log.w(tag, "onInsertSuccess")
    }

    /**
     * load data into recycler view
     */
    @ExperimentalStdlibApi
    private fun updateAdapter(serviceCatList: MutableList<ServiceType>?) {
        if (serviceCatList.isNullOrEmpty()) {
            Utils.showToast(activity, "${serviceName.capitalize(Locale.ROOT)} ${getString(R.string.no_data_found)}")
            activityUpdateListner.hideShowNavBar(false, getString(R.string.app_name))
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            fragmentServiceListBinding.serviceRecyclerView.adapter =
                    SearchServiceDbListAdapter(requireActivity(), serviceCatList, serviceImg)
        }
    }

    override fun onResume() {
        super.onResume()
        activityUpdateListner.hideShowNavBar(true, getString(R.string.service_provider_title))
    }
}
