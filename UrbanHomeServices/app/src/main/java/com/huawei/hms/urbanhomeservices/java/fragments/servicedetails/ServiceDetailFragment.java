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
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.huawei.hms.site.api.model.Poi;
import com.huawei.hms.site.api.model.Site;
import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.fragments.map.NearByStoresLocationFragment;
import com.huawei.hms.urbanhomeservices.java.listener.ActivityUpdateListener;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;
import com.huawei.hms.urbanhomeservices.java.utils.Utils;

/**
 * Provides details of Service provider such as name, phone number, email id etc.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class ServiceDetailFragment extends Fragment implements View.OnClickListener {

    private Site site = null;
    private String phone = null;
    private ActivityUpdateListener activityUpdateListner;
    private TextView shopName;
    private TextView shopAddress;
    private TextView shopDistance;
    private ImageView serviceImageId;
    private ImageView locationImg;
    Activity mActivity;

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
        View view = inflater.inflate(R.layout.fragment_service_detail, container, false);
        shopName = view.findViewById(R.id.shopName);
        shopAddress = view.findViewById(R.id.shopAddress);
        shopDistance = view.findViewById(R.id.shopDistance);
        serviceImageId = view.findViewById(R.id.serviceImageId);
        locationImg = view.findViewById(R.id.locationImg);
        ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) mActivity).getSupportActionBar().setTitle("");
        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        locationImg.setOnClickListener(this);
        assert arguments != null;
        site = arguments.getParcelable(AppConstants.SERVICE_TYPE);
        shopName.setText(site.name);
        shopAddress.setText(site.formatAddress);
        Poi poi = site.getPoi();
        @SuppressLint("DefaultLocale") String siteDistance = String.format(AppConstants.STRING_FORMATTER_DISTANCE, Double.parseDouble(String.valueOf(site.distance)));
        shopDistance.setText(siteDistance + " " + getActivity().getResources().getString(R.string.txt_KM));
        poi.getPhone();
        switch (Utils.IMAGE_TYPE) {
            case AppConstants.PLUMBE:
                serviceImageId.setImageDrawable(getActivity().getDrawable(R.drawable.ic_plumbing));
                break;
            case AppConstants.SERVICE_TYPE_CARPENTER:
                serviceImageId.setImageDrawable(getActivity().getDrawable(R.drawable.ic_carpentry));
                break;
            case AppConstants.ELECTRICAL:
                serviceImageId.setImageDrawable(getActivity().getDrawable(R.drawable.ic_electric_labour));
                break;
            case AppConstants.SERVICE_TYPE_APPLIANCE_REPAIR:
                serviceImageId.setImageDrawable(getActivity().getDrawable(R.drawable.ic_appliance_repair));
                break;
            case AppConstants.SERVICE_TYPE_HOUSEKEEPER:
                serviceImageId.setImageDrawable(getActivity().getDrawable(R.drawable.ic_cleaner));
                break;
            case AppConstants.CLEANER:
                serviceImageId.setImageDrawable(getActivity().getDrawable(R.drawable.ic_cleaner));
                break;
            case AppConstants.SERVICE_TYPE_PAINTER:
                serviceImageId.setImageDrawable(getActivity().getDrawable(R.drawable.ic_painter));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.phoneImg:
                makeCall();
                break;
            case R.id.locationImg:
                showPath();
                break;
        }
    }

    /**
     * This method is used to provide Nearby service provider details from HMS Site kit
     */
    private void showPath() {
        FragmentTransaction fragmentTransaction = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putDouble(AppConstants.SERVICE_LAT_KEY, site.location.lat);
        bundle.putDouble(AppConstants.SERVICE_LNG_KEY, site.location.lng);
        bundle.putString(AppConstants.SERVICE_STORE_NAME_KEY, site.getName());
        bundle.putString(AppConstants.SERVICE_ADDR_KEY, site.getFormatAddress());
        Fragment mFrag = new NearByStoresLocationFragment();
        mFrag.setArguments(bundle);
        fragmentTransaction.add(R.id.nav_host_fragment, mFrag);
        fragmentTransaction.addToBackStack(getString(R.string.service_details_title));
        fragmentTransaction.commit();
    }

    /**
     * This method is used for dialing the service provider number.
     */
    private void makeCall() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse(AppConstants.SERVICE_PH_URI));
        startActivity(callIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        activityUpdateListner.hideShowNavBar(true, getString(R.string.service_details_title));
    }
}
