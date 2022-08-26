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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.huawei.hms.site.api.model.Site
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.FragmentServiceDetailBinding
import com.huawei.hms.urbanhomeservices.kotlin.fragments.map.NearByStoresLocationFragment
import com.huawei.hms.urbanhomeservices.kotlin.listener.ActivityUpdateListner
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils

/**
 * Provides details of Service provider such as name, phone number, email id etc.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

class ServiceDetailFragment : Fragment(), View.OnClickListener {
    private var site: Site? = null
    private var phone: String? = null
    private lateinit var activityUpdateListner: ActivityUpdateListner

    private var _fragmentServiceDetailBinding : FragmentServiceDetailBinding?=null
    private val fragmentServiceDetailBinding get() = _fragmentServiceDetailBinding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityUpdateListner = context as ActivityUpdateListner
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View?{
       _fragmentServiceDetailBinding = FragmentServiceDetailBinding.inflate(inflater, container, false)
        return fragmentServiceDetailBinding.root
    }
    @Suppress("ComplexMethod")
    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    @ExperimentalStdlibApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        fragmentServiceDetailBinding.locationImg.setOnClickListener(this)
        arguments?.let {
            site = it.getParcelable(AppConstants.SERVICE_TYPE)
            fragmentServiceDetailBinding. shopName.text = "${site?.name}"
            fragmentServiceDetailBinding. shopAddress.text = "${site?.formatAddress}"
            val poi = site?.getPoi()
            val siteDistance: String = String.format(
                    AppConstants.STRING_FORMATTER_DISTANCE,
                    site?.distance?.toDouble()?.div(AppConstants.DISTANCE_CONVERT_KM)
            )
            fragmentServiceDetailBinding.shopDistance.text = "$siteDistance ${activity?.resources?.getString(R.string.txt_KM)}"
            poi?.let {
                phone = it.getPhone()
            }
        }
        when (Utils.imageType) {
            AppConstants.PLUMBE -> {
                fragmentServiceDetailBinding.serviceImageId.setImageDrawable(activity?.getDrawable(R.drawable.ic_plumbing))
            }
            AppConstants.SERVICE_TYPE_CARPENTER -> {
                fragmentServiceDetailBinding.serviceImageId.setImageDrawable(activity?.getDrawable(R.drawable.ic_carpentry))
            }
            AppConstants.ELECTRICAL -> {
                fragmentServiceDetailBinding.serviceImageId.setImageDrawable(activity?.getDrawable(R.drawable.ic_electric_labour))
            }
            AppConstants.SERVICE_TYPE_APPLIANCE_REPAIR -> {
                fragmentServiceDetailBinding.serviceImageId.setImageDrawable(activity?.getDrawable(R.drawable.ic_appliance_repair))
            }
            AppConstants.SERVICE_TYPE_HOUSEKEEPER -> {
                fragmentServiceDetailBinding.serviceImageId.setImageDrawable(activity?.getDrawable(R.drawable.ic_cleaner))
            }
            AppConstants.CLEANER -> {
                fragmentServiceDetailBinding.serviceImageId.setImageDrawable(activity?.getDrawable(R.drawable.ic_cleaner))
            }
            AppConstants.SERVICE_TYPE_PAINTER -> {
                fragmentServiceDetailBinding.serviceImageId.setImageDrawable(activity?.getDrawable(R.drawable.ic_painter))
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.getId()) {
            R.id.phoneImg -> makeCall()
            R.id.locationImg -> showPath()
        }
    }

    /**
     * This method is used for dialing the service provider number.
     */
    private fun makeCall() {
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("${AppConstants.SERVICE_PH_URI}$phone")
        startActivity(callIntent)
    }

    /**
     * This method is used to provide Nearby service provider details from HMS Site kit
     */
    private fun showPath() {
        val fragmentTransaction: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.apply {
            site?.let {
                putDouble(AppConstants.SERVICE_LAT_KEY, it.location.lat)
                putDouble(AppConstants.SERVICE_LNG_KEY, it.location.lng)
                putString(AppConstants.SERVICE_STORE_NAME_KEY, it.getName())
                putString(AppConstants.SERVICE_ADDR_KEY, it.getFormatAddress())
            }
        }
        val mFrag: Fragment = NearByStoresLocationFragment()
        mFrag.arguments = bundle
        fragmentTransaction.apply {
            add(R.id.nav_host_fragment, mFrag)
            addToBackStack(getString(R.string.service_details_title))
            commit()
        }
    }

    override fun onResume() {
        super.onResume()
        activityUpdateListner.hideShowNavBar(true, getString(R.string.service_details_title))
    }
}
