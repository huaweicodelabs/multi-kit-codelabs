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
@file:Suppress("NewLineAtEndOfFile","TooGenericExceptionCaught")
package com.huawei.hms.urbanhomeservices.kotlin.fragments.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.huawei.hms.identity.entity.GetUserAddressResult
import com.huawei.hms.identity.entity.UserAddress
import com.huawei.hms.identity.entity.UserAddressRequest
import com.huawei.hms.support.api.client.Status
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.FragmentProfileKBinding
import com.huawei.hms.urbanhomeservices.kotlin.listener.ActivityUpdateListner
import com.huawei.hms.urbanhomeservices.kotlin.model.LoginModel
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils
import java.util.*

/**
 * Load User address from Identity Kit and choose required address
 * CHeck User details
 *
 * @author: Huawei
 * @since 20-01-21
 */

class ProfileFragment : Fragment(), View.OnClickListener {
    companion object {
        private const val TAG: String = "ProfileFragment"
    }
    private lateinit var activityUpdateListner: ActivityUpdateListner
    private var _bindingHomeFragment: FragmentProfileKBinding? = null
    private val bindingHomeFragment get() = _bindingHomeFragment!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityUpdateListner = context as ActivityUpdateListner
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _bindingHomeFragment = FragmentProfileKBinding.inflate(inflater, container, false)
        return bindingHomeFragment.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProfile()
        Utils.isProfileFragment = true
        bindingHomeFragment.btnLogout.setOnClickListener(this)
        bindingHomeFragment.queryUserAddress.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnLogout -> {
                Utils.logoutDialog(requireActivity())
            }
            R.id.query_user_address -> {
                if (Utils.isConnected(activity)) {
                    getUserAddress()
                } else {
                    Utils.showToast(activity, getString(R.string.check_internet))
                }
            }
        }
    }

    /**
     * Show User address
     */
    private fun showProfile() {
        val loginModel: LoginModel = Utils.getSharePrefData(requireActivity())
        val name = "<b>Name: </b>${loginModel.displayName}"
        bindingHomeFragment.userName.text = HtmlCompat.fromHtml(name, HtmlCompat.FROM_HTML_MODE_LEGACY)
        bindingHomeFragment.userEmail.text = HtmlCompat.fromHtml(
                "<b>Email: </b>${loginModel.email}",
                HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        getCompleteAddressString(Utils.curentLatitude, Utils.currentLongitude)
    }
    @Suppress("TooGenericExceptionCaught","FunctionParameterNaming")
    /**
     * Get User address based on current lat and long
     *
     * @param LATITUDE latitude of address
     * @param LONGITUDE longitude of address
     */
    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(activity, Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            val returnedAddress: Address = addresses[0]
            val strReturnedAddress = java.lang.StringBuilder("")
            for (i in 0..returnedAddress.maxAddressLineIndex) {
                strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(AppConstants.NEW_LINE)
            }
            strAdd = strReturnedAddress.toString()
        } catch (e: Exception) {
            Log.d(tag, "UserAddressException")
        }
        return strAdd
    }

    /**
     * Get User address from Identity Kit
     */
    @Suppress("EmptyFunctionBlock")
    private fun getUserAddress() {
        val req = UserAddressRequest()
        val task = com.huawei.hms.identity.Address.getAddressClient(activity).getUserAddress(req)
        task.addOnSuccessListener { result ->
            try {
                startActivityForResult(result)
            } catch (e: IntentSender.SendIntentException) {
                Log.d(tag, "SendIntentException")
            }
        }.addOnFailureListener { _ -> Log.i(tag, "on Failed result code:") }
    }


    @Throws(IntentSender.SendIntentException::class)
    private fun startActivityForResult(result: GetUserAddressResult) {
        val status: Status = result.status
        if (result.returnCode == 0 && status.hasResolution()) {
            status.startResolutionForResult(activity, AppConstants.LOGIN_GET_ADDRESS_REQUESTCODE)
        }
    }
    @Suppress("NestedBlockDepth")
    /**
     * Based on user action, Identity Kit return User Address
     * Format user Identity Kit address to show User
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.LOGIN_GET_ADDRESS_REQUESTCODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    val userAddress = UserAddress.parseIntent(data)
                    if (userAddress != null) {
                        val sb = StringBuilder()
                        sb.apply {
                            appendln("<b>Name: </b> ${userAddress.name}<br>")
                            appendln("<b>Address: </b> ${userAddress.addressLine1} ${userAddress.addressLine2}<br>")
                            appendln("<b>City: </b> ${userAddress.locality} <br>")
                            appendln("<b>State: </b> ${userAddress.administrativeArea}<br>")
                            appendln("<b>Country: </b> ${userAddress.countryCode} <br>")
                            appendln("<b>Phone: </b> ${userAddress.phoneNumber}")
                            activity?.let { Utils.storeCountryName(it, userAddress.countryCode.toString()) }
                        }
                        bindingHomeFragment.userAddressId.text =
                                HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                    } else if (!bindingHomeFragment.userAddressId.text.contains(AppConstants.SEARCH_NAME_KEY)) {
                        bindingHomeFragment. userAddressId.text = getString(R.string.failed_address_text)
                    }
                }
                Activity.RESULT_CANCELED -> {
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityUpdateListner.hideShowNavBar(false, getString(R.string.title_profile))
        if (!Utils.getUserAddress(requireActivity()).isNullOrEmpty()) {
            bindingHomeFragment.userAddressId.text = HtmlCompat.fromHtml(
                    Utils.getUserAddress(requireActivity()).toString(),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
    }
}
