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
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.SearchServiceDbListItemBinding
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.ServiceType
import com.huawei.hms.urbanhomeservices.kotlin.fragments.servicedetails.ServiceDetailsCloudDBFragment
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants

/**
 * This adapter is used to Search Service from CloudDb and and displaying ui content.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

class SearchServiceDbListAdapter(
        private var context: Context,
        private val serviceList: List<ServiceType>?,
        private val imageString: String?
) :
        RecyclerView.Adapter<SearchServiceDbListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val searchServiceBinding = SearchServiceDbListItemBinding.bind(itemView)
        val serviceImg: ImageView = searchServiceBinding.serviceTypeImg
        val serviceProviderEmail: TextView = searchServiceBinding.serviceProviderEmail
        val serviceProviderMo: TextView = searchServiceBinding.serviceProviderMo
        val serviceProviderName: TextView = searchServiceBinding.serviceProviderName
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.search_service_db_list_item, viewGroup, false)
    )

    override fun getItemCount(): Int {
        serviceList?.let {
            return serviceList.size
        }
        return 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity: AppCompatActivity = context as AppCompatActivity
        serviceList?.let {
            val serviceType: ServiceType = serviceList[position]
            holder.serviceImg.setImageDrawable(getResource(imageString))
            holder.serviceProviderMo.text =
                    "${activity.resources.getString(R.string.txt_mobile_number)} ${serviceType.phoneNumber.toString()}"
            holder.serviceProviderEmail.text =
                    "${activity.resources.getString(R.string.txt_email)}  ${serviceType.emailId}"
            holder.serviceProviderName.text = serviceType.serviceProviderName
            holder.itemView.setOnClickListener {
                val activityContext: AppCompatActivity = context as AppCompatActivity
                val fragmentTransaction: FragmentTransaction =
                        activityContext.supportFragmentManager.beginTransaction()
                val bundle = Bundle()
                bundle.apply {
                    putString(AppConstants.SERVICE_NAME_KEY, serviceType.catName)
                    putString(AppConstants.PROVIDER_IMAGE_KEY, imageString)
                    putString(AppConstants.PROVIDER_NAME_KEY, serviceType.serviceProviderName)
                    putString(AppConstants.PROVIDER_PH_NUM_KEY, serviceType.phoneNumber.toString())
                    putString(AppConstants.PROVIDER_EMAIL_KEY, serviceType.emailId)
                    putString(AppConstants.PROVIDER_COUNTRY, serviceType.country)
                    putString(AppConstants.PROVIDER_CITY, serviceType.state)
                    putString(AppConstants.PROVIDER_STATE, serviceType.city)
                }
                val mFrag: Fragment =
                        ServiceDetailsCloudDBFragment()
                mFrag.arguments = bundle
                fragmentTransaction.apply {
                    add(R.id.nav_host_fragment, mFrag)
                    addToBackStack(activity.getString(R.string.service_provider_title))
                    commit()
                }
            }
        }
    }

    /**
     * This method is used to get the image resource and set the image.
     *
     * @param name name of resource
     */
    private fun getResource(name: String?): Drawable? {
        val resID = context.resources.getIdentifier(
                name,
                AppConstants.SERVICE_DRAWABLE_KEY,
                context.packageName
        )
        return ActivityCompat.getDrawable(context, resID)
    }
}
