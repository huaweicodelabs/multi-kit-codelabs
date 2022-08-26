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
@file:Suppress("TooManyFunctions","MaxLineLength","MagicNumber")
package com.huawei.hms.urbanhomeservices.kotlin.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.huawei.agconnect.auth.SignInResult
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.AddServiceLayoutBinding
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.CloudDBZoneWrapper
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.LoginHelper
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.ServiceType
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppPreferences
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils.hideKeyboard
import com.huawei.hms.urbanhomeservices.kotlin.viewmodel.AddServiceViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * Following operation are performed in this activity
 * 1: Select user details
 * 2: Validate user details
 * 3: Add the details to the CloudDb
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AddServiceActivity : AppCompatActivity(), View.OnClickListener,
        CloudDBZoneWrapper.UiCallBack<ServiceType>, LoginHelper.OnLoginEventCallBack {
    private var serviceType: ServiceType? = null
    private var serviceID: Int? = 0
    private var isServiceEdited: Boolean = false
    private lateinit var addServiceViewModel: AddServiceViewModel
    private var countryName: String? = null
    private var cityName: String? = null
    private var stateName: String? = null
    private var mCloudDBZoneWrapper: CloudDBZoneWrapper<ServiceType>? = null
    private val tag = "AddServiceActivity"
    private var isCityAvailable = false

    private lateinit var addServiceActivityBinding: AddServiceLayoutBinding
    @SuppressLint("ClickableViewAccessibility")
    @Suppress("LongMethod","ComplexMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addServiceActivityBinding = AddServiceLayoutBinding.inflate(layoutInflater)
        val view = addServiceActivityBinding.root
        setContentView(view)
        initViewModel()
        AppPreferences.userType = AppConstants.SERVICE_PROVIDER_TYPE
        setSupportActionBar(addServiceActivityBinding.toolbarAddService)
        addServiceActivityBinding.selectCountrySpn.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }
        addServiceActivityBinding.etServiceName.doAfterTextChanged {
            when {
                addServiceActivityBinding.etServiceName.text.toString().isNotEmpty() -> {
                    addServiceActivityBinding.etServiceNameTV.error = null
                }
                else -> {
                    addServiceActivityBinding.etServiceNameTV.error = getString(R.string.enter_service_name)
                }
            }
            enableDisableButton()
        }
        addServiceActivityBinding.etPhoneNum.doAfterTextChanged {
            when {
                addServiceActivityBinding.etPhoneNum.text.toString().isNotEmpty() ->
                    addServiceActivityBinding.etPhoneNumTV.error = null
                else ->
                    addServiceActivityBinding.etPhoneNumTV.error = getString(R.string.enter_phone_num)
            }
            enableDisableButton()
        }
        addServiceActivityBinding.etEmailId.doAfterTextChanged {
            when {
                addServiceActivityBinding.etEmailId.text.toString().isNotEmpty() ->
                    addServiceActivityBinding.etEmailTV.error = null
                else -> {
                    addServiceActivityBinding.etEmailTV.error = getString(R.string.enter_email_id)
                }
            }
            enableDisableButton()
        }
        val items = resources.getStringArray(R.array.select_service)
        val adapter: ArrayAdapter<String> =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        serviceType = ServiceType()
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        addServiceActivityBinding.selectServiceSpinner.adapter = adapter
        addServiceActivityBinding.selectServiceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @Suppress("EmptyFunctionBlock")
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.w(tag, "onNothingSelected")
            }
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                hideKeyboard()
                enableDisableButton()
            }
        }

        /**
         * Initialize cloud db zone
         */
        initCloudZone()
        addServiceActivityBinding.loginSericeProviderBtn.setOnClickListener(this)
        if (intent.hasExtra(AppConstants.CATEGORY_NAME)) {
            intent.extras.let {
                addServiceActivityBinding.toolbarTitle.text = getString(R.string.edit_service_title)
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    setDisplayShowHomeEnabled(true)
                    setDisplayShowTitleEnabled(false)
                }
                isServiceEdited = true
                addServiceActivityBinding.etEmailId.setText(it?.getString(AppConstants.PROVIDER_MAIL_ID))
                addServiceActivityBinding.etPhoneNum.setText(it?.getString(AppConstants.PROVIDER_PH_NUM))
                addServiceActivityBinding.etServiceName.setText(it?.getString(AppConstants.PROVIDER_NAME))
                serviceID = it?.getInt(AppConstants.PROVIDER_ID)
                countryName = it?.getString(AppConstants.PROVIDER_COUNTRY)
                stateName = it?.getString(AppConstants.PROVIDER_STATE)
                cityName = it?.getString(AppConstants.PROVIDER_CITY)
                items.forEachIndexed { index, element ->
                    if (it?.getString(AppConstants.CATEGORY_NAME).equals(element)) {
                        addServiceActivityBinding.selectServiceSpinner.setSelection(index)
                    }
                }
            }
        } else {
            addServiceActivityBinding.toolbarTitle.text = getString(R.string.add_services)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
        populateCountries()
    }

    /**
     * Initialize View model
     */
    private fun initViewModel() {
        addServiceViewModel = ViewModelProvider(this).get(AddServiceViewModel::class.java)
    }
    @Suppress("ComplexCondition")
    /**
     * To enable or disable "Save button"
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun enableDisableButton() {
        if (addServiceActivityBinding.etPhoneNum.text.isNullOrEmpty() || addServiceActivityBinding.etEmailId.text.isNullOrEmpty()
                || addServiceActivityBinding.etServiceName.text.isNullOrEmpty()
                || addServiceActivityBinding.selectServiceSpinner.selectedItem == AppConstants.CHOOSE_SERVICE
                || addServiceActivityBinding.selectCountrySpn.selectedItem == getString(R.string.choose_country)
                || addServiceActivityBinding.selectCitySpn.selectedItem == getString(R.string.choose_city)
                || addServiceActivityBinding.selectStateSpn.selectedItem == getString(R.string.choose_state)
        ) {
            addServiceActivityBinding.loginSericeProviderBtn.background = getDrawable(R.drawable.rounded_corner_gray_btn)
        } else {
            addServiceActivityBinding.loginSericeProviderBtn.background = getDrawable(R.drawable.rounded_corner_green_btn)
        }
    }

    /**
     * Init cloud db Zone
     * for fetching data from cloud
     */
    private fun initCloudZone() {
        mCloudDBZoneWrapper = CloudDBZoneWrapper()
        mCloudDBZoneWrapper?.setCloudObject(serviceType)
    }
    @Suppress("ComplexMethod")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.loginSericeProviderBtn -> {
                when {
                    addServiceActivityBinding.selectServiceSpinner.selectedItem.toString()
                            .equals(AppConstants.CHOOSE_SERVICE, true) ->
                        (addServiceActivityBinding.selectServiceSpinner.selectedView as TextView).error =
                                getString(R.string.service_type_err_msg)
                    addServiceActivityBinding.etServiceName.text.isNullOrEmpty() ->
                        addServiceActivityBinding.etServiceNameTV.error = getString(R.string.enter_service_name)
                    addServiceActivityBinding.etPhoneNum.text.isNullOrEmpty() ->
                        addServiceActivityBinding. etPhoneNumTV.error = getString(R.string.enter_phone_num)
                    isValidMobile(addServiceActivityBinding.etPhoneNum.text.toString()) ->
                        addServiceActivityBinding. etPhoneNumTV.error = getString(R.string.enter_valid_phone_number)
                    addServiceActivityBinding.etEmailId.text.toString().isEmpty() ->
                        addServiceActivityBinding.etEmailTV.error = getString(R.string.enter_email_id)
                    !isValidMail((addServiceActivityBinding.etEmailId.text.toString()).toLowerCase(Locale.ROOT)) ->
                        addServiceActivityBinding.etEmailTV.error = getString(R.string.enter_valid_email_id)
                    addServiceActivityBinding.selectCountrySpn.selectedItem.toString()
                            .equals(getString(R.string.choose_country), true) ->
                        (addServiceActivityBinding.selectCountrySpn.selectedView as TextView).error =
                                getString(R.string.select_country)
                    addServiceActivityBinding.selectStateSpn.selectedItem.toString()
                            .equals(getString(R.string.choose_state), true) ->
                        (addServiceActivityBinding.selectStateSpn.selectedView as TextView).error =
                                getString(R.string.select_state)
                    isCityAvailable && addServiceActivityBinding.selectCitySpn.selectedItem.toString()
                            .equals(getString(R.string.choose_city), true) ->
                        (addServiceActivityBinding.selectCitySpn.selectedView as TextView).error =
                                getString(R.string.select_city)
                    else -> {
                        processAddAction()
                    }
                }
            }
        }
    }
    @SuppressWarnings("checkstyle:magicnumber")
    /**
     * Adds user details to CloudDB
     */
    private fun processAddAction() {
        GlobalScope.launch(context = Dispatchers.Main) {
            val loginHelper = LoginHelper(this@AddServiceActivity)
            loginHelper.addLoginCallBack(this@AddServiceActivity)
            loginHelper.login()
        }
        val rnds = (1..100000).random()
        serviceType?.catName = addServiceActivityBinding.selectServiceSpinner.selectedItem as String?
        if (serviceID == 0)
            serviceType?.id = rnds
        else {
            serviceType?.id = serviceID
        }
        serviceType?.userName = AppPreferences.username
        serviceType?.country = addServiceActivityBinding.selectCountrySpn.selectedItem as String?
        serviceType?.state = addServiceActivityBinding.selectStateSpn.selectedItem as String?
        serviceType?.city = addServiceActivityBinding.selectCitySpn.selectedItem as String?
        serviceType?.emailId = addServiceActivityBinding.etEmailId.text.toString()
        serviceType?.phoneNumber = addServiceActivityBinding.etPhoneNum.text.toString().toLong()
        serviceType?.serviceProviderName = addServiceActivityBinding.etServiceName.text.toString()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * To validate Email
     */
    private fun isValidMail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    @SuppressWarnings("checkstyle:magicnumber")
    /**
     *To validate Module number
     * 
     * @param phone mobile phone value
     * @return boolean validation of mobile phone
     */
    private fun isValidMobile(phone: String): Boolean {
        if (phone.length in 14 downTo 5) {
            return false
        }
        return true
    }

    override fun updateUiOnError(errorMessage: String?) {
        Log.w(tag, "Cloud DB update error")
    }

    /**
     * Insert data in cloud db
     * Based on table which you pass
     */
    override fun onInitCloud() {
        mCloudDBZoneWrapper?.insertDbZoneInfo(serviceType)
    }
    @Suppress("EmptyFunctionBlock")
    override fun onAddOrQuery(dbZoneList: MutableList<ServiceType>?) {
        Log.w(tag, "onAddOrQuery")
    }
    @Suppress("EmptyFunctionBlock")
    override fun onSubscribe(dbZoneList: MutableList<ServiceType>?) {
        Log.w(tag, "onSubscribe")
    }
    @Suppress("EmptyFunctionBlock")
    override fun onDelete(dbZoneList: MutableList<ServiceType>?) {
        Log.w(tag, "onDelete")
    }
    override fun onLogin(showLoginUserInfo: Boolean, signInResult: SignInResult?) {
        GlobalScope.launch(context = Dispatchers.IO) {
            mCloudDBZoneWrapper?.setmUiCallBack(this@AddServiceActivity)
            mCloudDBZoneWrapper?.createObjectType()
            mCloudDBZoneWrapper?.openCloudDBZoneV2()
        }
    }

    override fun onLogOut(showLoginUserInfo: Boolean) {
        Log.w(tag, "onLogout")
    }

    override fun onInsertSuccess(cloudDBZoneResult: Int?) {
        if (isServiceEdited) {
            Utils.showToast(this@AddServiceActivity, getString(R.string.msg_data_updated))
            finish()
        } else {
            Utils.showToast(this@AddServiceActivity, getString(R.string.msg_data_updated))
            startActivity(Intent(this@AddServiceActivity, AddServiceActivity::class.java))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.provider_menu, menu)
        return true
    }

    /**
     * On select menu option
     * it will navigate to LogOut or Manage Services
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.provider_item_logout -> {
                Utils.logoutDialog(this)
                true
            }
            R.id.manageService -> {
                val intent = Intent(this, ManageServiceActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * onBack pressed, show logout dialog
     */
    override fun onBackPressed() {
        Utils.logoutDialog(this)
    }

    /**
     * Following operation performed :
     * 1 : Parse the Country.json file
     * 2 : Populate country, state and city details in spinner
     * 3 : Validate the spinners for country, state and city.
     */
    private fun populateCountries() {
        hideKeyboard(addServiceActivityBinding.selectCountrySpn)
        enableDisableButton()
        addServiceViewModel.fetchCountryData()
        addServiceViewModel.listOfCounty.observe(this, androidx.lifecycle.Observer {
            it.let {
                var index = 0
                if (isServiceEdited)
                    index = it.indexOf(countryName)
                val countryListAdapter: ArrayAdapter<String?> = ArrayAdapter(
                        this@AddServiceActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        it
                )
                addServiceActivityBinding.selectCountrySpn.apply {
                    adapter = countryListAdapter
                    prompt = getString(R.string.choose_country)
                    setSelection(index)
                }

                addServiceActivityBinding.selectCountrySpn.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                Log.w(tag, "onSubscribe")
                            }

                            override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View,
                                    position: Int,
                                    id: Long
                            ) {
                                if (position > 0)
                                    addServiceViewModel.fetchStateData(position - 1)
                                else
                                    addServiceViewModel.fetchStateData(position)

                                addServiceViewModel.listOfstate.observe(
                                        this@AddServiceActivity,
                                        androidx.lifecycle.Observer {
                                            it.let {
                                                var stateIndex = 0
                                                if (isServiceEdited) {
                                                    stateIndex = it.indexOf(stateName)
                                                }
                                                val stateListAdapter: ArrayAdapter<String?> =
                                                        ArrayAdapter<String?>(
                                                                this@AddServiceActivity,
                                                                android.R.layout.simple_spinner_dropdown_item, it
                                                        )
                                                addServiceActivityBinding.selectStateSpn.apply {
                                                    adapter = stateListAdapter
                                                    prompt = getString(R.string.choose_country)
                                                    setSelection(stateIndex)
                                                }
                                                enableDisableButton()
                                                addServiceActivityBinding. selectStateSpn.onItemSelectedListener =
                                                        object : AdapterView.OnItemSelectedListener {
                                                            override fun onItemSelected(
                                                                    parent: AdapterView<*>,
                                                                    view: View,
                                                                    position: Int,
                                                                    id: Long
                                                            ) {
                                                                isCityAvailable = true
                                                                if (position > 0) {
                                                                    addServiceViewModel.fetchCityData(position - 1)
                                                                } else {
                                                                    addServiceViewModel.fetchCityData(position)
                                                                }
                                                                addServiceViewModel.listOfCity.observe(
                                                                        this@AddServiceActivity,
                                                                        androidx.lifecycle.Observer {
                                                                            it.let {
                                                                                isCityAvailable = true

                                                                                addServiceActivityBinding.selectCitySpn.visibility =
                                                                                        View.VISIBLE
                                                                                var cityIndex = 0
                                                                                if (isServiceEdited) {
                                                                                    cityIndex =
                                                                                            it.indexOf(cityName)
                                                                                }
                                                                                val cityStateAdapter: ArrayAdapter<String?> =
                                                                                        ArrayAdapter<String?>(
                                                                                                this@AddServiceActivity,
                                                                                                android.R.layout.simple_spinner_dropdown_item,
                                                                                                it
                                                                                        )
                                                                                addServiceActivityBinding.selectCitySpn.apply {
                                                                                    adapter = cityStateAdapter
                                                                                    prompt =
                                                                                            getString(R.string.choose_country)
                                                                                    setSelection(cityIndex)
                                                                                }
                                                                                if (it.isEmpty()) {
                                                                                    isCityAvailable = false
                                                                                    addServiceActivityBinding.selectCitySpn.visibility =
                                                                                            View.GONE
                                                                                }
                                                                                addServiceActivityBinding.selectCitySpn.onItemSelectedListener =
                                                                                        object :
                                                                                                AdapterView.OnItemSelectedListener {
                                                                                            override fun onItemSelected(
                                                                                                    parent: AdapterView<*>,
                                                                                                    view: View,
                                                                                                    position: Int,
                                                                                                    id: Long
                                                                                            ) {
                                                                                                enableDisableButton()
                                                                                            }

                                                                                            override fun onNothingSelected(
                                                                                                    parent: AdapterView<*>?
                                                                                            ) {
                                                                                                Log.d(
                                                                                                        tag,
                                                                                                        "On state nothing selected"
                                                                                                )
                                                                                            }
                                                                                        }
                                                                            }
                                                                        })
                                                                when {
                                                                    !isCityAvailable -> {
                                                                        addServiceActivityBinding.selectCitySpn.visibility = View.GONE
                                                                    }
                                                                }
                                                            }

                                                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                                                Log.d(tag, "On state nothing selected")
                                                            }
                                                        }
                                            }
                                        })
                            }
                        }
            }
        })
    }
}

