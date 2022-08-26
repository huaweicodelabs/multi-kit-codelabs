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

package com.huawei.hms.urbanhomeservices.java.fragments.searchservice;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.adapter.SearchServiceDbListAdapter;
import com.huawei.hms.urbanhomeservices.java.clouddb.CloudDBZoneWrapper;
import com.huawei.hms.urbanhomeservices.java.clouddb.ServiceType;
import com.huawei.hms.urbanhomeservices.java.listener.ActivityUpdateListener;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;
import com.huawei.hms.urbanhomeservices.java.utils.Utils;

import java.util.List;

/**
 * Fetch Service list from Cloud DB and update into Adapter class
 * To check Service details
 *
 * @author: Huawei
 * @since 20-01-21
 */

public class SearchServiceListDbFragment extends Fragment implements CloudDBZoneWrapper.UiCallBack<ServiceType> {

    private String serviceImg = null;
    private CloudDBZoneWrapper<ServiceType> mCloudDBZoneWrapper = null;
    private CloudDBZoneQuery<ServiceType> query;
    private ActivityUpdateListener activityUpdateListener;
    public static final String TAG = SearchServiceListDbFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private DefaultItemAnimator itemAnimator;
    Activity mActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activityUpdateListener = (ActivityUpdateListener) context;
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_suggestion_list_j, container, false);
        mRecyclerView = view.findViewById(R.id.serviceRecyclerView);
        ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) mActivity).getSupportActionBar().setTitle("");
        initCloudDB();
        initRecyclerView();
        fetchServiceFromCloudDB();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Based on Query And condition, fetch data
     * from  cloudDB
     * Initialize  Recycler view
     * load service by category
     */
    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.hasFixedSize();
        itemAnimator = new DefaultItemAnimator();
    }

    /**
     * Load service
     * from site Api by service cat name
     */
    private void fetchServiceFromCloudDB() {
        Bundle arguments = getArguments();
        serviceImg = arguments.getString(AppConstants.PROVIDER_IMAGE_KEY);
        mCloudDBZoneWrapper.setmUiCallBack(this);
        mCloudDBZoneWrapper.openCloudDBZoneV2();
        query = CloudDBZoneQuery.where(ServiceType.class);
        query.equalTo(AppConstants.COUNTRY_STR, Utils.getSharePrefCountry(requireActivity()));
        String serviceName = arguments.getString(AppConstants.SEARCH_NAME_KEY);
        query.beginsWith(AppConstants.CAT_NAME, serviceName.substring(AppConstants.INTIAL_VALUE,
                serviceName.length() - 1).replace(" ", ""));
    }

    /**
     * Initialize Cloud DB
     */
    private void initCloudDB() {
        ServiceType serviceType = new ServiceType();
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mCloudDBZoneWrapper.setCloudObject(serviceType);
        mCloudDBZoneWrapper.createObjectType();
    }

    /**
     * Fetch data from Cloud DB And update
     * into list
     *
     * @param dbZoneList dbzone list value
     */

    @Override
    public void onAddOrQuery(List<ServiceType> dbZoneList) {
        updateAdapter(dbZoneList);
    }

    @Override
    public void onSubscribe(List<ServiceType> dbZoneList) {
        Log.w(TAG, "onSubscribe");
    }

    @Override
    public void onDelete(List<ServiceType> dbZoneList) {
        Log.w(TAG, "onDelete");
    }

    @Override
    public void updateUiOnError(String errorMessage) {
        Log.w(TAG, "updateUiOnError");
    }

    @Override
    public void onInitCloud() {
        Log.w(TAG, "onInit");
        mCloudDBZoneWrapper.queryAllData(query);
    }

    @Override
    public void onInsertSuccess(Integer cloudDBZoneResult) {

    }

    /**
     * load data into recycler view
     *
     * @param serviceCatList service category list
     */

    private void updateAdapter(List<ServiceType> serviceCatList) {
        if (serviceCatList.isEmpty()) {
            Utils.showToast(getActivity(), getString(R.string.no_data_found));
            activityUpdateListener.hideShowNavBar(false, getString(R.string.app_name));
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            SearchServiceDbListAdapter searchServiceDbListAdapter = new SearchServiceDbListAdapter(getActivity(), serviceCatList, serviceImg);
            mRecyclerView.setAdapter(searchServiceDbListAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        activityUpdateListener.hideShowNavBar(true, getString(R.string.service_provider_title));
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) mActivity).getSupportActionBar().setTitle("");
    }
}
