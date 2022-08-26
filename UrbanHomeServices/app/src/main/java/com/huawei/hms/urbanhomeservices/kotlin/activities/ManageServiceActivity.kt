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
@file:Suppress("TooManyFunctions", "NewLineAtEndOfFile")

package com.huawei.hms.urbanhomeservices.kotlin.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.ActivityMainKBinding
import com.huawei.hms.urbanhomeservices.databinding.ManageServiceActivityBinding
import com.huawei.hms.urbanhomeservices.kotlin.adapter.ManageServiceAdapter
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.CloudDBZoneWrapper
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.ServiceType
import com.huawei.hms.urbanhomeservices.kotlin.listener.ServiceUpdateListener
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppPreferences
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils

/**
 * Used for managing the Service provider details
 * Also helps in Adding,deleting and editing the Service provider details
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

class ManageServiceActivity : AppCompatActivity(), CloudDBZoneWrapper.UiCallBack<ServiceType>,
        ServiceUpdateListener<ServiceType> {
    private val tag = "ManageServiceActivity"
    private var mCloudDBZoneWrapper: CloudDBZoneWrapper<ServiceType>? = null
    private lateinit var query: CloudDBZoneQuery<ServiceType>
    private var serviceType: ServiceType? = null

    private lateinit var manageBinding: ManageServiceActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manageBinding = ManageServiceActivityBinding.inflate(layoutInflater)
        val view = manageBinding.root
        setContentView(view)
        setSupportActionBar(manageBinding.toolbarAddService)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        initRecyclerView()
    }

    /**
     * Based on query and condition fetch data from  cloudDB
     * Init recycler View
     * load service by category
     */
    private fun initRecyclerView() {
        manageBinding.manageServiceRV.apply {
            layoutManager = LinearLayoutManager(this@ManageServiceActivity)
            isNestedScrollingEnabled = false
            hasFixedSize()
            itemAnimator = DefaultItemAnimator()
        }
    }

    /**
     * Init cloud DB
     */
    private fun initCloudDB() {
        serviceType = ServiceType()
        mCloudDBZoneWrapper = CloudDBZoneWrapper()
        mCloudDBZoneWrapper?.let {
            it.setCloudObject(serviceType)
            it.createObjectType()
        }
        fetchServiceFromCloudDB()
    }

    /**
     * load service
     * from site Api by service cat name
     */
    private fun fetchServiceFromCloudDB() {
        mCloudDBZoneWrapper?.let {
            it.setmUiCallBack(this)
            it.openCloudDBZoneV2()
        }
        query = CloudDBZoneQuery.where(ServiceType::class.java)
        query.equalTo(AppConstants.USER_NAME_KEY, AppPreferences.username)
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

    override fun onInsertSuccess(cloudDBZoneResult: Int?) {
        Log.w(tag, "Data updated successfully")
    }

    /**
     * load data into recycler view
     */
    private fun updateAdapter(serviceCatList: MutableList<ServiceType>?) {
        if (serviceCatList.isNullOrEmpty()) {
            Utils.showToast(this, getString(R.string.no_services_available))
        } else {
            manageBinding.manageServiceRV.adapter = ManageServiceAdapter(this, serviceCatList, this)
        }
    }

    override fun deleteService(listObject: ServiceType) {
        val serviceList: MutableList<ServiceType> = mutableListOf()
        serviceList.add(listObject)
        mCloudDBZoneWrapper?.deleteTableData(serviceList)
    }

    override fun editService(listObject: ServiceType) {
        val intent = Intent(this, AddServiceActivity::class.java)
        intent.apply {
            putExtra(AppConstants.CATEGORY_NAME, listObject.catName)
            putExtra(AppConstants.PROVIDER_PH_NUM, listObject.phoneNumber.toString())
            putExtra(AppConstants.PROVIDER_MAIL_ID, listObject.emailId)
            putExtra(AppConstants.PROVIDER_COUNTRY, listObject.country)
            putExtra(AppConstants.PROVIDER_ID, listObject.id)
            putExtra(AppConstants.PROVIDER_NAME, listObject.serviceProviderName)
            putExtra(AppConstants.PROVIDER_CITY, listObject.city)
            putExtra(AppConstants.PROVIDER_STATE, listObject.state)
        }
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        initCloudDB()
    }
}
