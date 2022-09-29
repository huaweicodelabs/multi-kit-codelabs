/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.huawei.hms.couriertracking.core.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.couriertracking.core.utils.ImageHelper
import com.huawei.hms.couriertracking.databinding.ItemOrderBinding
import com.huawei.hms.couriertracking.domain.model.Order

class OrderRecyclerViewAdapter : RecyclerView.Adapter<OrderRecyclerViewAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Order>(){
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(
            ItemOrderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private var onItemClickListener: ((order:Order)->Unit)? = null

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.binding.apply {
            textViewTitle.text = order.title
            textViewDescription.text = order.description
            textViewPrice.text = order.price
            ImageHelper().loadUrl(order.photoUrl,imgProduct)
            cardViewOrderItem.setOnClickListener {
                onItemClickListener?.let { it(order) }
            }
        }
    }

    fun setOnItemClickListener(listener: (order:Order)->Unit){
        onItemClickListener = listener
    }

    override fun getItemCount(): Int = differ.currentList.size
}