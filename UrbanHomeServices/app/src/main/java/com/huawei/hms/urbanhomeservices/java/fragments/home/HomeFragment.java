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

package com.huawei.hms.urbanhomeservices.java.fragments.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.activities.MainActivity;
import com.huawei.hms.urbanhomeservices.java.adapter.ServiceCatListAdapter;
import com.huawei.hms.urbanhomeservices.java.clouddb.CloudDBZoneWrapper;
import com.huawei.hms.urbanhomeservices.java.clouddb.ServiceCategory;
import com.huawei.hms.urbanhomeservices.java.fragments.searchservice.SiteKitResultFragment;
import com.huawei.hms.urbanhomeservices.java.listener.ActivityUpdateListener;
import com.huawei.hms.urbanhomeservices.java.searchbar.MaterialSearchBar;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;
import com.huawei.hms.urbanhomeservices.java.utils.Utils;
import com.huawei.hms.urbanhomeservices.kotlin.fragments.home.LocationModel;

import java.util.List;
import java.util.Locale;

import static com.huawei.hms.urbanhomeservices.java.utils.Utils.CURRENT_LAT;
import static com.huawei.hms.urbanhomeservices.java.utils.Utils.CURRENT_LON;

/**
 * HomeFragment  will open first time
 * Based on current location, we are fetching services from Cloud DB
 * Search Services from Huawei Site Kit
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class HomeFragment extends Fragment implements MaterialSearchBar.OnSearchActionListener, CloudDBZoneWrapper.UiCallBack<ServiceCategory>, View.OnClickListener {


    public static final String TAG = HomeFragment.class.getSimpleName();
    private CloudDBZoneWrapper<ServiceCategory> mCloudDBZoneWrapper = null;
    private ServiceCategory serviceCategory;
    private CloudDBZoneQuery<ServiceCategory> query;
    private ActivityUpdateListener activityUpdateListner;
    private HomeViewModel homeViewModel;
    private String imageType;
    private Context context;
    private ScrollView dropDownLL;
    private View bgView;
    private MaterialSearchBar searchBar;
    public RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_j, container, false);
        initCloudDb();
        initUI(view);
        return view;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Initialize UI
     * Initialize View model
     *
     * @param view view to initialize
     */

    private void initUI(View view) {
        Utils.IS_PROFILE_FRAGMENT = false;
        getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        LinearLayout llAppliance = view.findViewById(R.id.ll_appliance);
        LinearLayout llCarpenter = view.findViewById(R.id.ll_carpenter);
        LinearLayout llElectrician = view.findViewById(R.id.ll_electrician);
        LinearLayout llCleaner = view.findViewById(R.id.ll_cleaner);
        LinearLayout llPainter = view.findViewById(R.id.ll_painter);
        LinearLayout llPlumber = view.findViewById(R.id.ll_plumber);
        llPlumber.setOnClickListener(this);
        llElectrician.setOnClickListener(this);
        llAppliance.setOnClickListener(this);
        llCarpenter.setOnClickListener(this);
        llCleaner.setOnClickListener(this);
        llPainter.setOnClickListener(this);
        mRecyclerView = view.findViewById(R.id.serviceGridRV);
        dropDownLL = view.findViewById(R.id.dropDownLL);
        dropDownLL.setVisibility(View.GONE);
        bgView = view.findViewById(R.id.bgView);
        initViewModel();
        initRecyclerViewService();
        startLocationUpdate();
        searchBar = view.findViewById(R.id.searchBar);
        searchBar.setOnSearchActionListener(HomeFragment.this);
        searchBar.setCardViewElevation(0);
    }

    /**
     * Initialize Cloud DB to fetch data from Cloud DB
     */

    private void initCloudDb() {
        serviceCategory = new ServiceCategory();
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mCloudDBZoneWrapper.setCloudObject(serviceCategory);
        mCloudDBZoneWrapper.createObjectType();
    }

    /**
     * Current Location from HMS Location Kit
     * When location change, live data will update
     */
    private void startLocationUpdate() {
        homeViewModel.getLocationData().observe(getViewLifecycleOwner(), new Observer<com.huawei.hms.urbanhomeservices.kotlin.fragments.home.LocationModel>() {
            @Override
            public void onChanged(LocationModel locationModel) {
                double latitude = locationModel.getLatitud();
                double longitude = locationModel.getLongitude();
                Utils.latLng(latitude, longitude);
                getCompleteAddressString(latitude, longitude);
            }
        });
    }

    /**
     * Initialize RecyclerView for HMS Site Kit search
     */

    private void initRecyclerViewService() {
        GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
        mRecyclerView.hasFixedSize();
        mRecyclerView.setLayoutManager(layoutManager);
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
    }

    /**
     * Initializing ViewModel class and write query to fetch data from Cloud DB
     */
    private void initViewModel() {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        mCloudDBZoneWrapper.setmUiCallBack(this);
        mCloudDBZoneWrapper.openCloudDBZoneV2();
        query = CloudDBZoneQuery.where(ServiceCategory.class);
    }

    /**
     * Get Address from Lat , lang
     *
     * @param LATITUDE  latitude of address
     * @param LONGITUDE longitude of address
     * @return String address from lat and lon
     */
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            Address returnedAddress = addresses.get(0);
            StringBuilder strReturnedAddress = new StringBuilder("");
            for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(AppConstants.NEW_LINE);
            }
            strAdd = strReturnedAddress.toString();
        } catch (Exception e) {
            Log.w(TAG, "Can not get address exception");
        }
        return strAdd;
    }

    /**
     * Fetch service list from Cloud DB
     * Update in Adapter
     *
     * @param serviceCatList service category list
     */
    private void updateAdapter(List<ServiceCategory> serviceCatList) {
        ServiceCatListAdapter serviceCatListAdapter = new ServiceCatListAdapter(getActivity(), serviceCatList);
        mRecyclerView.setAdapter(serviceCatListAdapter);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activityUpdateListner = (ActivityUpdateListener) context;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_plumber:
                onSearchConfirmed(AppConstants.PLUMBE);
                searchBar.setText(AppConstants.SERVICE_TYPE_PLUMBER);
                Utils.hideKeyboard(getActivity());
                break;
            case R.id.ll_electrician:
                onSearchConfirmed(AppConstants.ELECTRICAL);
                searchBar.setText(AppConstants.ELECTRICAL);
                Utils.hideKeyboard(getActivity());
                break;
            case R.id.ll_cleaner:
                onSearchConfirmed(AppConstants.CLEANER);
                searchBar.setText(AppConstants.CLEANER);
                Utils.hideKeyboard(getActivity());
                break;
            case R.id.ll_painter:
                onSearchConfirmed(AppConstants.SERVICE_TYPE_PAINTER);
                searchBar.setText(AppConstants.SERVICE_TYPE_PAINTER);
                Utils.hideKeyboard(getActivity());
                break;
            case R.id.ll_carpenter:
                onSearchConfirmed(AppConstants.SERVICE_TYPE_CARPENTER);
                searchBar.setText(AppConstants.SERVICE_TYPE_CARPENTER);
                Utils.hideKeyboard(getActivity());
                break;
            case R.id.ll_appliance:
                onSearchConfirmed(AppConstants.SERVICE_TYPE_APPLIANCE_REPAIR);
                searchBar.setText(AppConstants.SERVICE_TYPE_APPLIANCE_REPAIR);
                Utils.hideKeyboard(getActivity());
                break;
        }
    }

    @Override
    public void onAddOrQuery(List<ServiceCategory> dbZoneList) {
        updateAdapter(dbZoneList);
    }

    @Override
    public void onSubscribe(List<ServiceCategory> dbZoneList) {

    }

    @Override
    public void onDelete(List<ServiceCategory> dbZoneList) {

    }

    @Override
    public void updateUiOnError(String errorMessage) {

    }

    @Override
    public void onInitCloud() {
        mCloudDBZoneWrapper.queryAllData(query);
    }

    @Override
    public void onInsertSuccess(Integer cloudDBZoneResult) {

    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if (enabled) {
            dropDownLL.setVisibility(View.VISIBLE);
            bgView.setVisibility(View.VISIBLE);
        } else {
            dropDownLL.setVisibility(View.GONE);
            bgView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        String queryString = text.toString();
        if (queryString.equals(AppConstants.SERVICE_TYPE_PLUMBER)) {
            queryString = AppConstants.PLUMBE;
        }
        if (queryString.startsWith(AppConstants.PLUMBE)
                || queryString.startsWith(AppConstants.ELECTRICAL)
                || queryString.equals(AppConstants.CLEANER)
                || queryString.equals(AppConstants.SERVICE_TYPE_HOUSEKEEPER)
                || queryString.equals(AppConstants.SERVICE_TYPE_PAINTER)
                || queryString.equals(AppConstants.SERVICE_TYPE_CARPENTER)
                || queryString.equals(AppConstants.SERVICE_TYPE_APPLIANCE_REPAIR)) {
            FragmentTransaction fragmentTransaction =
                    requireActivity().getSupportFragmentManager().beginTransaction();
            Bundle bundle = new Bundle();
            bundle.putString(AppConstants.REQUEST_QUERY, queryString);
            bundle.putDouble(AppConstants.SERVICE_LAT_KEY, CURRENT_LAT);
            bundle.putDouble(AppConstants.SERVICE_LNG_KEY, CURRENT_LON);
            bundle.putString(AppConstants.PROVIDER_IMAGE_KEY, imageType);
            SiteKitResultFragment mFrag = new SiteKitResultFragment();
            mFrag.setArguments(bundle);
            fragmentTransaction.add(R.id.nav_host_fragment, mFrag);
            fragmentTransaction.addToBackStack(getString(R.string.app_name));
            fragmentTransaction.commit();
        } else {
            searchBar.setText(" ");
            searchBar.openSearch();
            Utils.hideKeyboard(((MainActivity) context));
            Utils.showLongToast(requireActivity(), queryString.toUpperCase(Locale.ROOT) + "" + getString(R.string.no_data_found));
        }
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
            searchBar.closeSearch();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        activityUpdateListner.hideShowNavBar(false, getString(R.string.app_name));
    }

}
