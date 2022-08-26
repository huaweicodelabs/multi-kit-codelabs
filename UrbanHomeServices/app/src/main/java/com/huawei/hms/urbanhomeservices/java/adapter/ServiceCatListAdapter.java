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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.clouddb.ServiceCategory;
import com.huawei.hms.urbanhomeservices.java.fragments.searchservice.SearchServiceListDbFragment;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;

import java.util.List;

/**
 * This adapter is used to Search Service from NearbyServices and and displaying ui content.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class ServiceCatListAdapter extends RecyclerView.Adapter<ServiceCatListAdapter.ViewHolder> {

    private Context context;
    private List<ServiceCategory> serviceList;

    public ServiceCatListAdapter(Context context, List<ServiceCategory> serviceCatList) {
        this.context = context;
        this.serviceList = serviceCatList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName;
        ImageView serviceImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.serviceName);
            serviceImg = itemView.findViewById(R.id.serviceImg);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.service_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceCategory serviceCategory = serviceList.get(position);
        holder.serviceName.setText(serviceCategory.getServiceName());
        holder.serviceImg.setImageDrawable(getResource(serviceCategory.getImageName()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment mFrag = new SearchServiceListDbFragment();
                FragmentTransaction fragmentTransaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putString(AppConstants.SERVICE_NAME_KEY, serviceCategory.getServiceCategory());
                bundle.putString(AppConstants.PROVIDER_IMAGE_KEY, serviceCategory.getImageName());
                bundle.putString(AppConstants.SEARCH_NAME_KEY, serviceCategory.getServiceName());
                mFrag.setArguments(bundle);
                fragmentTransaction.replace(R.id.nav_host_fragment, mFrag);
                fragmentTransaction.addToBackStack(context.getString(R.string.app_name));
                fragmentTransaction.commit();
            }
        });
    }

    /**
     * This method is used to get the image resource and set the image.
     *
     * @param name  resource name
     * @return Drawable drawable resource 
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable getResource(String name) {
        int resID = context.getResources().getIdentifier(
                name,
                AppConstants.SERVICE_DRAWABLE_KEY,
                context.getPackageName()
        );
        return context.getDrawable(resID);
    }
}
