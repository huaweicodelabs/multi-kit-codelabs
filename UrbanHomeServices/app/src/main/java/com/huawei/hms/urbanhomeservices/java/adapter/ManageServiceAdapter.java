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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.clouddb.ServiceType;
import com.huawei.hms.urbanhomeservices.java.listener.ServiceUpdateListener;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This adapter is used for managing service from cloud db and displaying ui content.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class ManageServiceAdapter extends RecyclerView.Adapter<ManageServiceAdapter.ViewHolder> {

    private Context context;
    private List<ServiceType> serviceList = new ArrayList<>();
    ;
    private ServiceUpdateListener<ServiceType> serviceUpdateListener;
    private ServiceType serviceType;

    public ManageServiceAdapter(Context context, List<ServiceType> serviceCatList, ServiceUpdateListener<ServiceType> serviceUpdateListener) {
        this.context = context;
        this.serviceList = serviceCatList;
        this.serviceUpdateListener = serviceUpdateListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView serviceImg;
        TextView serviceProviderMo;
        TextView serviceProviderEmail;
        TextView serviceProviderName;
        Button deleteBtn;
        Button editServiceBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            serviceImg = itemView.findViewById(R.id.serviceTypeImg);
            serviceProviderEmail = itemView.findViewById(R.id.serviceProviderEmail);
            serviceProviderMo = itemView.findViewById(R.id.serviceProviderMo);
            serviceProviderName = itemView.findViewById(R.id.serviceProviderName);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            editServiceBtn = itemView.findViewById(R.id.editServiceBtn);
        }
    }

    @Override
    public @NotNull ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manage_service_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        serviceType = serviceList.get(position);
        switch (serviceType.getCatName()) {
            case AppConstants.SERVICE_TYPE_PLUMBER:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_plumbing));
                break;
            case AppConstants.SERVICE_TYPE_CARPENTER:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_carpentry));
                break;
            case AppConstants.SERVICE_TYPE_ELECTRICIAN:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_electric_labour));
                break;
            case AppConstants.SERVICE_TYPE_APPLIANCE_REPAIR:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_appliance_repair));
                break;
            case AppConstants.SERVICE_TYPE_HOUSEKEEPER:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_cleaner));
                break;
            case AppConstants.SERVICE_TYPE_PAINTER:
                holder.serviceImg.setImageDrawable(context.getDrawable(R.drawable.ic_painter));
                break;
        }

        holder.serviceProviderMo.setText(context.getString(R.string.txt_mobile_number) + " " + serviceType.getPhoneNumber());
        holder.serviceProviderEmail.setText(context.getString(R.string.txt_email) + " " + serviceType.getEmailId());
        holder.serviceProviderName.setText(serviceType.getServiceProviderName());
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteConfirmation(context, serviceType, position);
            }
        });
        holder.editServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceUpdateListener.editService(serviceList.get(position));
            }
        });
    }

    /**
     * show delete service
     * confirmation AlertDialog here!
     * click on yes button will delete service from cloud DB
     *
     * @param context     context of app
     * @param position    position of the data
     * @param serviceType type of service
     */
    private void deleteConfirmation(Context context, ServiceType serviceType, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.app_name));
        builder.setMessage(context.getString(R.string.are_you_sure_want_to_delete) + " " + serviceType.getServiceProviderName() + " " + context.getString(R.string.service));
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                serviceList.remove(position);
                serviceUpdateListener.deleteService(serviceType);
                notifyDataSetChanged();
                dialog.cancel();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}

