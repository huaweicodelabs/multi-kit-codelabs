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

package com.huawei.hms.urbanhomeservices.java.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.adapter.ManageServiceAdapter;
import com.huawei.hms.urbanhomeservices.java.clouddb.CloudDBZoneWrapper;
import com.huawei.hms.urbanhomeservices.java.clouddb.ServiceType;
import com.huawei.hms.urbanhomeservices.java.listener.ServiceUpdateListener;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;
import com.huawei.hms.urbanhomeservices.java.utils.AppPreferences;
import com.huawei.hms.urbanhomeservices.java.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for managing the Service provider details
 * Also helps in Adding,deleting and editing the Service provider details
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class ManageServiceActivity extends AppCompatActivity implements CloudDBZoneWrapper.UiCallBack<ServiceType>,
        ServiceUpdateListener<ServiceType> {

    public static final String TAG = ManageServiceActivity.class.getSimpleName();
    private CloudDBZoneWrapper<ServiceType> mCloudDBZoneWrapper = null;
    private CloudDBZoneQuery<ServiceType> query;
    private ServiceType serviceType = null;
    private Toolbar toolbar_add_service;
    private LinearLayoutManager layoutManager;
    private RecyclerView mRecyclerView;
    private RecyclerView.ItemAnimator itemAnimator;
    private ManageServiceAdapter manageServiceAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_service_activity);
        toolbar_add_service = findViewById(R.id.toolbar_add_service);
        setSupportActionBar(toolbar_add_service);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mRecyclerView = findViewById(R.id.manageServiceRV);
        initRecyclerView();
    }

    /**
     * Based on query and condition fetch data from  cloudDB
     * Init recycler View
     * load service by category
     */
    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(ManageServiceActivity.this);
        mRecyclerView.hasFixedSize();
        mRecyclerView.setLayoutManager(layoutManager);
        itemAnimator = new DefaultItemAnimator();
    }

    /**
     * Init cloud DB
     */
    private void initCloudDB() {
        serviceType = new ServiceType();
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mCloudDBZoneWrapper.setCloudObject(serviceType);
        mCloudDBZoneWrapper.createObjectType();
        fetchServiceFromCloudDB();
    }

    /**
     * load service
     * from site Api by service cat name
     */
    private void fetchServiceFromCloudDB() {
        mCloudDBZoneWrapper.setmUiCallBack(this);
        mCloudDBZoneWrapper.openCloudDBZoneV2();
        query = CloudDBZoneQuery.where(ServiceType.class);
        query = query.equalTo(AppConstants.USER_NAME_KEY, AppPreferences.getUserName());
    }

    @Override
    public void onSubscribe(List<ServiceType> dbZoneList) {
        Log.w(TAG, "onSubscribe");
    }

    @Override
    public void onDelete(List<ServiceType> dbZoneList) {

    }

    @Override
    public void updateUiOnError(String errorMessage) {
        Log.w(TAG, "updateUiOnError");
    }

    /**
     * Fetch data from Cloud DB And update
     * into list
     *
     * @param dbZoneList db zone list
     */
    @Override
    public void onAddOrQuery(List<ServiceType> dbZoneList) {
        updateAdapter(dbZoneList);
    }

    private void updateAdapter(List<ServiceType> dbZoneList) {
        if (dbZoneList.isEmpty()) {
            Utils.showToast(this, getString(R.string.no_services_available));
        } else {
            manageServiceAdapter = new ManageServiceAdapter(ManageServiceActivity.this, dbZoneList, this);
            mRecyclerView.setAdapter(manageServiceAdapter);
            manageServiceAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Make query fetch data from Cloud Db
     */
    @Override
    public void onInitCloud() {
        Log.w(TAG, "onInit");
        mCloudDBZoneWrapper.queryAllData(query);
    }

    @Override
    public void onInsertSuccess(Integer cloudDBZoneResult) {
        Log.w(TAG, "Data updated successfully");

    }

    @Override
    public void deleteService(ServiceType listObject) {
        List<ServiceType> serviceList = new ArrayList<ServiceType>();
        serviceList.add(listObject);
        mCloudDBZoneWrapper.deleteTableData(serviceList);
    }

    @Override
    public void editService(ServiceType listObject) {
        Intent intent = new Intent(this, AddServiceActivity.class);
        intent.putExtra(AppConstants.CATEGORY_NAME, listObject.getCatName());
        intent.putExtra(AppConstants.PROVIDER_PH_NUM, listObject.getPhoneNumber());
        intent.putExtra(AppConstants.PROVIDER_MAIL_ID, listObject.getEmailId());
        intent.putExtra(AppConstants.PROVIDER_COUNTRY, listObject.getCountry());
        intent.putExtra(AppConstants.PROVIDER_ID, listObject.getId());
        intent.putExtra(AppConstants.PROVIDER_NAME, listObject.getServiceProviderName());
        intent.putExtra(AppConstants.PROVIDER_CITY, listObject.getCity());
        intent.putExtra(AppConstants.PROVIDER_STATE, listObject.getState());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCloudDB();
    }
}
