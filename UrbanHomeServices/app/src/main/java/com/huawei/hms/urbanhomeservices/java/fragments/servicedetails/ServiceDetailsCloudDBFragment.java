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

package com.huawei.hms.urbanhomeservices.java.fragments.servicedetails;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.listener.ActivityUpdateListener;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

/**
 * This activity provides information details about Service provider from Cloud DB.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class ServiceDetailsCloudDBFragment extends Fragment implements View.OnClickListener {

    private String serviceProviderEmail = null;
    private String phone = null;
    private ActivityUpdateListener activityUpdateListner;
    private TextView serviceProviderPhoneTV;
    private TextView serviceProviderEmailTV;
    private TextView serviceProviderNameTV;
    private TextView shopNameTV;
    private ImageView serviceImageId;
    private final Context context;
    Activity mActivity;

    public ServiceDetailsCloudDBFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activityUpdateListner = (ActivityUpdateListener) context;
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_detail_clouddb, container, false);
        serviceProviderPhoneTV = view.findViewById(R.id.serviceProviderPhoneTV);
        serviceProviderEmailTV = view.findViewById(R.id.serviceProviderEmailTV);
        serviceProviderNameTV = view.findViewById(R.id.serviceProviderNameTV);
        shopNameTV = view.findViewById(R.id.shopName);
        serviceImageId = view.findViewById(R.id.serviceImageId);
        return view;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) mActivity).getSupportActionBar().setTitle("");
        serviceProviderPhoneTV.setOnClickListener(this);
        serviceProviderEmailTV.setOnClickListener(this);
        Bundle arguments = getArguments();
        assert arguments != null;
        String serviceName = arguments.getString(AppConstants.SERVICE_NAME_KEY);
        String serviceProviderName = arguments.getString(AppConstants.PROVIDER_NAME_KEY);
        serviceProviderEmail = arguments.getString(AppConstants.PROVIDER_EMAIL_KEY);
        String serviceProviderShopName = arguments.getString(AppConstants.PROVIDER_SHOP_NAME_KEY);
        serviceProviderNameTV.setText(serviceProviderName);
        serviceProviderEmailTV.setText(serviceProviderEmail);
        phone = arguments.getString(AppConstants.PROVIDER_PH_NUM_KEY);
        serviceProviderPhoneTV.setText(phone);
        shopNameTV.setText(serviceProviderShopName);
        String serviceImg = arguments.getString(AppConstants.PROVIDER_IMAGE_KEY);
        serviceImageId.setImageDrawable(getResource(serviceImg));

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.serviceProviderPhoneTV) {
            makeCall();
        } else if (id == R.id.serviceProviderEmailTV) {
            sendEmail();
        }
    }

    /**
     * This method is used to send an email to the Service provider.
     */
    private void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, serviceProviderEmail);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, AppConstants.PROVIDER_SUB_VALUE);
        String message = null;
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        emailIntent.setType(AppConstants.PROVIDER_MSG_TYPE);
        startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_email_client_type)));
    }

    /**
     * This method is used to dial to the service provider number.
     */
    private void makeCall() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse(AppConstants.SERVICE_PH_URI + ":" + phone));
        startActivity(callIntent);
    }

    /**
     * This method is used to get the image resource and set the image.
     *
     * @param name name of the resource
     * @return Drawable drawable of given name
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable getResource(String name) {
        int resID = getResources().getIdentifier(
                name,
                AppConstants.SERVICE_DRAWABLE_KEY,
                context.getPackageName()
        );
        return getDrawable(context, resID);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        activityUpdateListner.hideShowNavBar(true, getString(R.string.service_details));
    }
}
