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
@file:Suppress("TooManyFunctions","NewLineAtEndOfFile")
package com.huawei.hms.urbanhomeservices.kotlin.activities

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.huawei.hms.common.ApiException
import com.huawei.hms.identity.Address
import com.huawei.hms.identity.entity.GetUserAddressResult
import com.huawei.hms.identity.entity.UserAddress
import com.huawei.hms.identity.entity.UserAddressRequest
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.ActivityMainKBinding
import com.huawei.hms.urbanhomeservices.databinding.AddServiceLayoutBinding
import com.huawei.hms.urbanhomeservices.kotlin.listener.ActivityUpdateListner
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppPreferences
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils



/**
 * Used for navigation between screens.
 * Also provides logout functionality from consumer module
 *
 * @author: Huawei
 * @since : 20-01-2021
 *
 */

class MainActivity : AppCompatActivity(), ActivityUpdateListner {
    private val tag = "MainActivity"
    private var showDialogCount = 0
    private lateinit var bindingMain : ActivityMainKBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMain = ActivityMainKBinding.inflate(layoutInflater)
        val view = bindingMain.root
        setContentView(view)
        isFirstTimeLogin()
        val navController = findNavController(R.id.nav_host_fragment)
        setSupportActionBar(bindingMain.toolbarHome)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val appBarConfiguration = AppBarConfiguration(
                setOf(R.id.navigation_home, R.id.navigation_profile)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        bindingMain.navView.setupWithNavController(navController)
    }

    /**
     * To hide/show Navigation bottom bar
     *
     * @param hideShow boolean to hide and show navigation
     * @param title title text
     */
    override fun hideShowNavBar(hideShow: Boolean, title: String) {
        bindingMain.toolbarTitle.text = title
        supportActionBar?.setDisplayHomeAsUpEnabled(hideShow)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * On back button pressed, manage Fragment BackStack
     */
    override fun onBackPressed() {
        val count: Int = supportFragmentManager.backStackEntryCount
        if (Utils.isProfileFragment) {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            Utils.isProfileFragment = false
        } else if (count > 0) {
            val entryValue = supportFragmentManager.getBackStackEntryAt(count - 1)
            supportFragmentManager.popBackStack()
            bindingMain.toolbarTitle.text = entryValue.name
            if (entryValue.name.equals(getString(R.string.app_name), true)) {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
        } else {
            Utils.logoutDialog(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val count: Int = supportFragmentManager.backStackEntryCount
                if (Utils.isProfileFragment) {
                    supportFragmentManager.popBackStack(
                            null,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    Utils.isProfileFragment = false
                } else if (count > 0) {
                    val entryValue = supportFragmentManager.getBackStackEntryAt(count - 1)
                    supportFragmentManager.popBackStack()
                    bindingMain.toolbarTitle.text = entryValue.name
                    if (entryValue.name.equals(getString(R.string.app_name), true)) {
                        supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    }
                }
                true
            }
            R.id.item_logout -> {
                Utils.logoutDialog(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.LOGIN_GET_ADDRESS_REQUESTCODE -> {
                onGetAddressResult(resultCode, data)
            }
        }
    }

    /**
     * Stores the user address in shared preferences
     *
     * @param resultCode result code
     * @param data result intent data
     */
    private fun onGetAddressResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val userAddress: UserAddress? = UserAddress.parseIntent(data)
                if (userAddress != null) {
                    val sb = StringBuilder()
                    sb.apply {
                        appendln("<b>Name: </b> ${userAddress.name}<br>")
                        appendln("<b>Address: </b> ${userAddress.addressLine1} ${userAddress.addressLine2}<br>")
                        appendln("<b>City: </b> ${userAddress.locality} <br>")
                        appendln("<b>State: </b> ${userAddress.administrativeArea}<br>")
                        appendln("<b>Country: </b> ${userAddress.countryCode} <br>")
                        appendln("<b>Phone: </b> ${userAddress.phoneNumber}")
                        Utils.storeCountryName(
                                this@MainActivity,
                                userAddress.countryCode.toString()
                        )
                    }
                    Utils.storeUserAddress(this@MainActivity, sb.toString())
                } else {
                    Utils.showToast(this@MainActivity, getString(R.string.msg_user_addr_failed))
                }
            }
            Activity.RESULT_CANCELED -> {
                Log.i(tag, "User cancelled the address update")
                if (Utils.getUserAddress(this@MainActivity).isNullOrEmpty()) {
                    AppPreferences.isFirstTimeLogin = true
                    isFirstTimeLogin()
                }
            }
            else -> {
                Utils.showToast(this@MainActivity, getString(R.string.msg_user_addr_failed))
            }
        }
    }

    /**
     * First time user login dialog
     * 1. If yes navigates to Huawei Identity module
     * 2. Fetches address details
     */
    private fun isFirstTimeLogin() {
        if (AppPreferences.isFirstTimeLogin) {
            AppPreferences.isFirstTimeLogin = false
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle(R.string.address_title)
                setMessage(R.string.choose_address)
                setIcon(android.R.drawable.ic_dialog_alert)
                setPositiveButton(R.string.select_yes) { _, _ ->
                    getUserAddress()
                }
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.apply {
                setCancelable(false)
                show()
            }
        }
    }

    /**
     * Gets the user address from identity kit.
     */
    private fun getUserAddress() {
        val task = Address.getAddressClient(this@MainActivity).getUserAddress(UserAddressRequest())
        task.addOnSuccessListener {
            Log.i(tag, "Login user data fetched successfully")
            try {
                startActivityForResult(it)
            } catch (ex: IntentSender.SendIntentException) {
                Log.d(tag, "SendIntentException")
            }
        }.addOnFailureListener {
            Log.i(tag, "User data fetch failed")
            if (it is ApiException) {
                when (it.statusCode) {
                    AppConstants.USER_COUNTRY_NOT_SUPPORTED -> {
                        Utils.showToast(
                                this@MainActivity,
                                getString(R.string.country_not_supported_identity)
                        )
                    }
                    AppConstants.CHILDREN_ACC_NOT_SUPPORTED -> {
                        Utils.showToast(
                                this@MainActivity,
                                getString(R.string.child_account_not_supported_identity)
                        )
                    }
                    else -> {
                        Utils.showToast(this@MainActivity, getString(R.string.msg_data_failed))
                    }
                }
            } else {
                Utils.showToast(this@MainActivity, getString(R.string.msg_unknown_error))
            }
        }
    }

    /**
     * To fetch User address result
     */
    private fun startActivityForResult(result: GetUserAddressResult) {
        val status = result.status
        if (result.returnCode == 0 && status.hasResolution()) {
            Log.i(tag, "the result had resolution.")
            status.startResolutionForResult(
                    this@MainActivity, AppConstants.LOGIN_GET_ADDRESS_REQUESTCODE
            )
        } else {
            Log.i(tag, "Failed the result had resolution.")
            Utils.showToast(this@MainActivity, getString(R.string.msg_failed_user_resolution))
        }
    }
}
