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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.ManageServiceListItemBinding
import com.huawei.hms.urbanhomeservices.kotlin.clouddb.ServiceType
import com.huawei.hms.urbanhomeservices.kotlin.listener.ServiceUpdateListener
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants

/**
 * This adapter is used for managing service from cloud db and displaying ui content.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

class ManageServiceAdapter(
        private var context: Context,
        private val serviceList: MutableList<ServiceType>?,
        private var serviceUpdateListener: ServiceUpdateListener<ServiceType>
) :
        RecyclerView.Adapter<ManageServiceAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val manageServiceListItemBinding = ManageServiceListItemBinding.bind(itemView)
        val serviceImg: ImageView = manageServiceListItemBinding.serviceTypeImg
        val serviceProviderEmail: TextView = manageServiceListItemBinding.serviceProviderEmail
        val serviceProviderMo: TextView = manageServiceListItemBinding.serviceProviderMo
        val serviceProviderName: TextView = manageServiceListItemBinding.serviceProviderName
        val deletBtn: TextView = manageServiceListItemBinding.deleteBtn
        val editServiceBtn: TextView = manageServiceListItemBinding.editServiceBtn
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.manage_service_list_item, viewGroup, false)

    )

    override fun getItemCount(): Int {
        serviceList?.let {
            return serviceList.size
        }
        return 0
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        serviceList?.let {
            val serviceType: ServiceType = serviceList[position]
            val activity: AppCompatActivity = context as AppCompatActivity
            holder.apply {
                when (serviceType.catName) {
                    AppConstants.SERVICE_TYPE_PLUMBER -> {
                        serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_plumbing))
                    }
                    AppConstants.SERVICE_TYPE_CARPENTER -> {
                        serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_carpentry))
                    }
                    AppConstants.SERVICE_TYPE_ELECTRICIAN -> {
                        serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_electric_labour))
                    }
                    AppConstants.SERVICE_TYPE_APPLIANCE_REPAIR -> {
                        serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_appliance_repair))
                    }
                    AppConstants.SERVICE_TYPE_HOUSEKEEPER -> {
                        serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_cleaner))
                    }
                    AppConstants.SERVICE_TYPE_PAINTER -> {
                        serviceImg.setImageDrawable(activity.getDrawable(R.drawable.ic_painter))
                    }
                }

                serviceProviderMo.text =
                        "${activity.resources.getString(R.string.txt_mobile_number)} ${serviceType.phoneNumber}"
                serviceProviderEmail.text =
                        "${activity.resources.getString(R.string.txt_email)} ${serviceType.emailId}"
                serviceProviderName.text = serviceType.serviceProviderName
                deletBtn.setOnClickListener {
                    deleteConfirmation(context, serviceType, position)
                }
                editServiceBtn.setOnClickListener {
                    serviceUpdateListener.editService(serviceType)
                }
            }
        }
    }

    /**
     * show delete service
     * confirmation AlertDialog here!
     * click on yes button will delete service from cloud DB
     */
    private fun deleteConfirmation(context: Context, serviceType: ServiceType, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle(context.getString(R.string.app_name))
            setMessage(
                    "${context.getString(R.string.are_you_sure_want_to_delete)} ${serviceType.serviceProviderName} ${
                        context.getString(
                                R.string.service
                        )
                    }"
            )
            setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
                serviceList?.removeAt(position)
                serviceUpdateListener.deleteService(serviceType)
                notifyDataSetChanged()
                dialog.cancel()
            }
            setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
                dialog.cancel()
            }
            show()
        }
    }
}
