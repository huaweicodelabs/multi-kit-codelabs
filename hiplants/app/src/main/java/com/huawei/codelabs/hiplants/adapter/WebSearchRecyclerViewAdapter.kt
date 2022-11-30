package com.huawei.codelabs.hiplants.adapter


import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.huawei.codelabs.hiplants.R
import com.huawei.codelabs.hiplants.model.WebSearchResults

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

class WebSearchRecyclerViewAdapter(private val itemList: ArrayList<WebSearchResults>) :
    RecyclerView.Adapter<WebSearchRecyclerViewAdapter.ModelViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WebSearchRecyclerViewAdapter.ModelViewHolder {


        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_view_row_holder, parent, false)

        val url = v.findViewById<TextView>(R.id.url)
        url.movementMethod = LinkMovementMethod.getInstance()

        return ModelViewHolder(v)
    }

    override fun onBindViewHolder(
        holder: WebSearchRecyclerViewAdapter.ModelViewHolder,
        position: Int
    ) {

        holder.titleText.text = itemList[position].title
        holder.snippetText.text = itemList[position].snippet
        holder.urlText.text = itemList[position].url


    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleText: TextView
        var snippetText: TextView
        var urlText: TextView


        init {
            titleText = itemView.findViewById(R.id.title) as TextView
            snippetText = itemView.findViewById(R.id.snippet) as TextView
            urlText = itemView.findViewById(R.id.url) as TextView

        }

    }


}
