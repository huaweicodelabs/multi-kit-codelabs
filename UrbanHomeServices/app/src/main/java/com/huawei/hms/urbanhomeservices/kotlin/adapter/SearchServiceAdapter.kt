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

package com.huawei.hms.urbanhomeservices.kotlin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.site.api.model.Site
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.ServiceCatListItemBinding
import com.huawei.hms.urbanhomeservices.databinding.ServiceListItemBinding
import com.huawei.hms.urbanhomeservices.kotlin.fragments.servicedetails.ServiceDetailFragment
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils

/**
 * This adapter is used to Search Service from NearbyServices and and displaying ui content
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

class SearchServiceAdapter(
        private var context: Context,
        private val serviceList: List<Site?>?,
        private var imageType: String
) :
        RecyclerView.Adapter<SearchServiceAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceCatLstItemBinding = ServiceCatListItemBinding.bind(itemView)
        val serviceName: TextView = serviceCatLstItemBinding.searchServiceName
        val serviceImg: ImageView = serviceCatLstItemBinding.serviceTypeImg
        val serviceAddress: TextView = serviceCatLstItemBinding.serviceAddress
        val serviceDistance: TextView = serviceCatLstItemBinding.distance
        val serviceRating: TextView = serviceCatLstItemBinding.rating
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.service_cat_list_item, viewGroup, false)
    )

    override fun getItemCount(): Int {
        serviceList?.let {
            return serviceList.size
        }
        return 0
    }
    @Suppress("ComplexMethod")
    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity: AppCompatActivity = context as AppCompatActivity
        val site: Site? = serviceList?.get(position)
        val poi = site?.getPoi()
        holder.apply {
            serviceName.text = site?.name
            serviceAddress.text = site?.formatAddress
            val siteDistance: String = String.format(
                    AppConstants.STRING_FORMATTER_DISTANCE,
                    site?.distance?.toDouble()?.div(AppConstants.DISTANCE_CONVERT_KM)
            )
            serviceDistance.text = "$siteDistance ${activity.resources.getString(R.string.txt_KM)}"
            poi?.let {
                serviceRating.text = it.getRating().toString()
            }
            when (imageType) {
                AppConstants.PLUMBE -> {
                    serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_plumbing))
                }
                AppConstants.SERVICE_TYPE_CARPENTER -> {
                    serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_carpentry))
                }
                AppConstants.ELECTRICAL -> {
                    serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_electric_labour))
                }
                AppConstants.SERVICE_TYPE_APPLIANCE_REPAIR -> {
                    serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_appliance_repair))
                }
                AppConstants.CLEANER -> {
                    serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_cleaner))
                }
                AppConstants.SERVICE_TYPE_HOUSEKEEPER -> {
                    serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_cleaner))
                }
                AppConstants.SERVICE_TYPE_PAINTER -> {
                    serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_painter))
                }
            }

            itemView.setOnClickListener {
                Utils.imageType = imageType
                val activityContext: AppCompatActivity = context as AppCompatActivity
                val fragmentTransaction: FragmentTransaction =
                        activityContext.supportFragmentManager.beginTransaction()
                val bundle = Bundle()
                bundle.apply {
                    putParcelable(AppConstants.SERVICE_TYPE, site)
                }
                val mFrag: Fragment = ServiceDetailFragment()
                mFrag.arguments = bundle
                fragmentTransaction.apply {
                    add(R.id.nav_host_fragment, mFrag)
                    addToBackStack(activity.getString(R.string.nearby_search_title))
                    commit()
                }
            }
        }
    }
}
