/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.knowmyboard.dtse.activity.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.knowmyboard.dtse.R;
import com.huawei.hms.knowmyboard.dtse.activity.intefaces.ItemClickListener;
import com.huawei.hms.site.api.model.Site;

import java.util.ArrayList;

public class SitesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Site> sites = new ArrayList<>();

    ItemClickListener itemClickListener;

    public SitesAdapter(ArrayList<Site> sites, Context mContext, ItemClickListener itemClickListener) {
        this.sites = sites;
        this.itemClickListener = itemClickListener;

    }

    @NonNull
    @Override
    public SitesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.site_item, parent, false);
        return new SitesViewHolder(rootView);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Site site = sites.get(position);

        SitesViewHolder viewHolder = (SitesViewHolder) holder;
        viewHolder.siteName.setText(site.getName());
        viewHolder.siteAddress.setText(site.getFormatAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemClicked(viewHolder, site, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sites.size();
    }

    class SitesViewHolder extends RecyclerView.ViewHolder {
        TextView siteName;
        TextView siteAddress;

        public SitesViewHolder(@NonNull View itemView) {
            super(itemView);
            siteName = itemView.findViewById(R.id.name);
            siteAddress = itemView.findViewById(R.id.address);
        }
    }
}
