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
@file:Suppress("MaxLineLength")
package com.huawei.hms.urbanhomeservices.kotlin.fragments.servicedetails

import android.R.id.message
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.FragmentServiceDetailClouddbBinding
import com.huawei.hms.urbanhomeservices.kotlin.listener.ActivityUpdateListner
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants

/**
 * This activity provides information details about Service provider from Cloud DB.
 *
 * @author: Huawei
 * @since : 20-01-2021
 *
 */

class ServiceDetailsCloudDBFragment : Fragment(), View.OnClickListener {
    private var phone: String? = null
    private var serviceName: String? = null
    private var serviceProviderName: String? = null
    private var serviceProviderEmail: String? = null
    private var serviceProviderShopName: String? = null
    private lateinit var activityUpdateListner: ActivityUpdateListner

    private var _fraServiceDetailCloudBinding:FragmentServiceDetailClouddbBinding?=null
    private val fragServiceDetailCloudBinding get() = _fraServiceDetailCloudBinding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityUpdateListner = context as ActivityUpdateListner
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View?{
        _fraServiceDetailCloudBinding = FragmentServiceDetailClouddbBinding.inflate(inflater, container, false)
        return fragServiceDetailCloudBinding.root
    }
    @SuppressLint("SetTextI18n")
    @ExperimentalStdlibApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        fragServiceDetailCloudBinding.serviceProviderPhoneTV.setOnClickListener(this)
        fragServiceDetailCloudBinding.serviceProviderEmailTV.setOnClickListener(this)
        arguments?.apply {
            serviceName = getString(AppConstants.SERVICE_NAME_KEY)
            serviceProviderName = getString(AppConstants.PROVIDER_NAME_KEY)
            serviceProviderEmail = getString(AppConstants.PROVIDER_EMAIL_KEY)
            serviceProviderShopName = getString(AppConstants.PROVIDER_SHOP_NAME_KEY)
            fragServiceDetailCloudBinding.serviceProviderNameTV.text = "$serviceProviderName"
            fragServiceDetailCloudBinding.serviceProviderEmailTV.text = "$serviceProviderEmail"
            phone = getString(AppConstants.PROVIDER_PH_NUM_KEY)
            fragServiceDetailCloudBinding.serviceProviderPhoneTV.text = "$phone"
            fragServiceDetailCloudBinding.shopName.text = "$serviceProviderShopName"
            fragServiceDetailCloudBinding.serviceImageId.setImageDrawable(getResource(getString(AppConstants.PROVIDER_IMAGE_KEY)))
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.serviceProviderPhoneTV ->
                makeCall()
            R.id.serviceProviderEmailTV ->
                sendEmail()
        }
    }

    /**
     * This method is used to send an email to the Service provider.
     */
    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(serviceProviderEmail.toString()))
            putExtra(Intent.EXTRA_SUBJECT, AppConstants.PROVIDER_SUB_VALUE)
            putExtra(Intent.EXTRA_TEXT, message)
            type = AppConstants.PROVIDER_MSG_TYPE
        }

        startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_email_client_type)))
    }

    /**
     * This method is used to dial to the service provider number.
     */
    private fun makeCall() {
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("${AppConstants.SERVICE_PH_URI}$phone")//change the number
        startActivity(callIntent)
    }

    /**
     * This method is used to get the image resource and set the image.
     */
    private fun getResource(name: String?): Drawable? {
        val resID = resources.getIdentifier(name, AppConstants.SERVICE_DRAWABLE_KEY, activity?.packageName)
        return getDrawable(requireActivity(), resID)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        activityUpdateListner.hideShowNavBar(true, getString(R.string.service_details))
    }
}
