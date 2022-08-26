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
import com.huawei.hms.urbanhomeservices.databinding.ServiceListItemBinding
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.ServiceCategory
import com.huawei.hms.urbanhomeservices.kotlin.fragments.searchservice.SearchServiceListDbFragment
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants

/**
 * This adapter is used to Search Service from NearbyServices and and displaying ui content.
 *
 * @author: Huawei
 * @since : 20-01-2021
 *
 */

class ServiceCatListAdapter(var context: Context, private val serviceList: List<ServiceCategory>?) :
        RecyclerView.Adapter<ServiceCatListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceBinding = ServiceListItemBinding.bind(itemView)
        val serviceName: TextView = serviceBinding.serviceName
        val serviceImg: ImageView = serviceBinding.serviceImg
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.service_list_item, viewGroup, false)
    )

    override fun getItemCount(): Int {
        serviceList?.let {
            return serviceList.size
        }
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val serviceCategory: ServiceCategory? = serviceList?.get(position)
        holder.apply {
            serviceName.text = serviceCategory?.serviceName
            serviceImg.setImageDrawable(getResource(serviceCategory?.imageName))
            itemView.setOnClickListener {
                val activity: AppCompatActivity = context as AppCompatActivity
                val fragmentTransaction: FragmentTransaction =
                        activity.supportFragmentManager.beginTransaction()
                val bundle = Bundle()
                bundle.apply {
                    putString(AppConstants.SERVICE_NAME_KEY, serviceCategory?.serviceCategory)
                    putString(AppConstants.PROVIDER_IMAGE_KEY, serviceCategory?.imageName)
                    putString(AppConstants.SEARCH_NAME_KEY, serviceCategory?.serviceName)
                }
                val mFrag: Fragment = SearchServiceListDbFragment()
                mFrag.arguments = bundle
                fragmentTransaction.apply {
                    add(R.id.nav_host_fragment, mFrag)
                    addToBackStack(activity.getString(R.string.app_name))
                    commit()
                }
            }
        }
    }

    /**
     * This method is used to get the image resource and set the image.
     *
     * @param name resource name
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
