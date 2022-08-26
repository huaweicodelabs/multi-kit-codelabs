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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.clouddb.ServiceType;
import com.huawei.hms.urbanhomeservices.java.fragments.servicedetails.ServiceDetailsCloudDBFragment;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;

import java.util.List;

/**
 * This adapter is used to Search Service from CloudDb and and displaying ui content.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class SearchServiceDbListAdapter extends RecyclerView.Adapter<SearchServiceDbListAdapter.ViewHolder> {

    private final Context context;
    private final List<ServiceType> serviceList;
    private final String image_string;
    private ServiceType serviceType;

    public SearchServiceDbListAdapter(Context context, List<ServiceType> serviceList, String serviceImg) {
        this.context = context;
        this.serviceList = serviceList;
        this.image_string = serviceImg;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView serviceImg;
        TextView serviceProviderEmail;
        TextView serviceProviderMo;
        TextView serviceProviderName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceImg = itemView.findViewById(R.id.serviceTypeImg);
            serviceProviderEmail = itemView.findViewById(R.id.serviceProviderEmail);
            serviceProviderMo = itemView.findViewById(R.id.serviceProviderMo);
            serviceProviderName = itemView.findViewById(R.id.serviceProviderName);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_service_db_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        serviceType = serviceList.get(position);
        holder.serviceImg.setImageDrawable(getResource(image_string));
        holder.serviceProviderMo.setText(context.getResources().getString(R.string.txt_mobile_number) + "" + serviceType.getPhoneNumber().toString());
        holder.serviceProviderEmail.setText(context.getResources().getString(R.string.txt_email) + "" + serviceType.getEmailId());
        holder.serviceProviderName.setText(serviceType.getServiceProviderName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putString(AppConstants.SERVICE_NAME_KEY, serviceType.getCatName());
                bundle.putString(AppConstants.PROVIDER_IMAGE_KEY, image_string);
                bundle.putString(AppConstants.PROVIDER_NAME_KEY, serviceType.getServiceProviderName());
                bundle.putString(AppConstants.PROVIDER_PH_NUM_KEY, serviceType.getPhoneNumber().toString());
                bundle.putString(AppConstants.PROVIDER_EMAIL_KEY, serviceType.getEmailId());
                bundle.putString(AppConstants.PROVIDER_COUNTRY, serviceType.getCountry());
                bundle.putString(AppConstants.PROVIDER_CITY, serviceType.getState());
                bundle.putString(AppConstants.PROVIDER_STATE, serviceType.getCity());
                ServiceDetailsCloudDBFragment mFrag = new ServiceDetailsCloudDBFragment(context);
                mFrag.setArguments(bundle);
                fragmentTransaction.add(R.id.nav_host_fragment, mFrag);
                fragmentTransaction.addToBackStack(context.getString(R.string.service_provider_title));
                fragmentTransaction.commit();
            }
        });
    }

    /**
     * This method is used to get the image resource and set the image.
     *
     * @param name resource name
     * @return Drawable get resource drawable of given name
     */
    private Drawable getResource(String name) {
        int resID = context.getResources().getIdentifier(
                name,
                AppConstants.SERVICE_DRAWABLE_KEY,
                context.getPackageName()
        );
        return ActivityCompat.getDrawable(context, resID);
    }
}
