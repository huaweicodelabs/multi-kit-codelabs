/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
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

package com.huawei.hms.urbanhomeservices.java.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.site.api.model.Site;
import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.fragments.servicedetails.ServiceDetailFragment;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;
import com.huawei.hms.urbanhomeservices.java.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * This adapter is used to Search Service from NearbyServices and and displaying ui content
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class SearchServiceAdapter extends RecyclerView.Adapter<SearchServiceAdapter.ViewHolder> {

    private final Context context;
    private List<Site> serviceList = new ArrayList<>();
    private String imageType;

    public SearchServiceAdapter(Context context, String queryString) {
        this.context = context;
        this.imageType = queryString;
    }

    /**
     * To set list items to RecyclerView
     *
     * @param results  list of sites
     */
    public void setListItems(List<Site> results) {
        this.serviceList = results;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName;
        ImageView serviceImg;
        TextView serviceAddress;
        TextView serviceDistance;
        TextView serviceRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.searchServiceName);
            serviceImg = itemView.findViewById(R.id.serviceTypeImg);
            serviceAddress = itemView.findViewById(R.id.serviceAddress);
            serviceDistance = itemView.findViewById(R.id.distance);
            serviceRating = itemView.findViewById(R.id.rating);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.service_cat_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Site site = serviceList.get(position);
        double poi = site.getPoi().getRating();
        holder.serviceName.setText(site.name);
        holder.serviceAddress.setText(site.formatAddress);
        @SuppressLint("DefaultLocale") String siteDistance = String.format(AppConstants.STRING_FORMATTER_DISTANCE, Double.parseDouble(String.valueOf(site.distance)));
        holder.serviceDistance.setText(siteDistance + " " + context.getResources().getString(R.string.txt_KM));
        holder.serviceRating.setText(String.valueOf(poi));
        switch (imageType) {
            case AppConstants.PLUMBE:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_plumbing));
                break;
            case AppConstants.SERVICE_TYPE_CARPENTER:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_carpentry));
                break;
            case AppConstants.ELECTRICAL:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_electric_labour));
                break;
            case AppConstants.SERVICE_TYPE_APPLIANCE_REPAIR:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_appliance_repair));
                break;
            case AppConstants.CLEANER:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_cleaner));
                break;
            case AppConstants.SERVICE_TYPE_HOUSEKEEPER:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_cleaner));
                break;
            case AppConstants.SERVICE_TYPE_PAINTER:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_painter));
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.IMAGE_TYPE = imageType;
                FragmentTransaction fragmentTransaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppConstants.SERVICE_TYPE, site);
                ServiceDetailFragment serviceDetailFragment = new ServiceDetailFragment();
                serviceDetailFragment.setArguments(bundle);
                fragmentTransaction.add(R.id.nav_host_fragment, serviceDetailFragment);
                fragmentTransaction.addToBackStack(context.getString(R.string.nearby_search_title));
                fragmentTransaction.commit();
            }
        });
    }
}
