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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.NearbySearchRequest;
import com.huawei.hms.site.api.model.NearbySearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;
import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.adapter.SearchServiceAdapter;
import com.huawei.hms.urbanhomeservices.java.listener.ActivityUpdateListener;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;
import com.huawei.hms.urbanhomeservices.java.utils.Utils;

import java.util.List;
import java.util.Locale;

/**
 * Implement HMS Site kit
 * Based on user query, search NearBySearch result from HMS Site kit
 * Show list of NearByService
 *
 * @author: Huawei
 * @since 20-01-21
 */

public class SiteKitResultFragment extends Fragment {

    private SearchService searchService = null;
    private String imageString;
    private String queryString;
    private List<Site> sites;
    private ActivityUpdateListener activityUpdateListner;
    private RecyclerView mRecyclerView;
    private SearchServiceAdapter searchServiceAdapter;
    private Toolbar toolbar_nearby_stores;
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
        View view = inflater.inflate(R.layout.site_kit_result_layout, container, false);
        mRecyclerView = view.findViewById(R.id.siteKiteResultRV);
        toolbar_nearby_stores = mActivity.findViewById(R.id.toolbar_home);
        ((AppCompatActivity) mActivity).setSupportActionBar(toolbar_nearby_stores);
        ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) mActivity).getSupportActionBar().setTitle("");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        searchService = SearchServiceFactory.create(requireActivity(), Utils.getApiKey());
        NearbySearchRequest request = new NearbySearchRequest();
        queryString = arguments.getString(AppConstants.REQUEST_QUERY);
        imageString = arguments.getString(AppConstants.PROVIDER_IMAGE_KEY);
        request.setQuery(queryString);
        Coordinate location = new Coordinate(Utils.CURRENT_LAT, Utils.CURRENT_LON);
        request.setLocation(location);
        searchService.nearbySearch(request, searchResultListener);
        initRecyclerView();
    }

    /**
     * Based on Query And condition, fetch data
     * from  cloudDB
     * Init Recycler view
     * load service by category
     */

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.hasFixedSize();
        mRecyclerView.getItemAnimator();
        searchServiceAdapter = new SearchServiceAdapter(requireActivity(), queryString);
        mRecyclerView.setAdapter(searchServiceAdapter);
    }

    private final SearchResultListener<NearbySearchResponse> searchResultListener =
            new SearchResultListener<NearbySearchResponse>() {
                @Override
                public void onSearchResult(NearbySearchResponse results) {
                    Utils.hideKeyboard(getActivity());
                    sites = results.getSites();
                    if (sites.isEmpty()) {
                        switch (queryString) {
                            case AppConstants.PLUMBE:
                                queryString = AppConstants.SERVICE_TYPE_PLUMBER;
                                break;
                            case AppConstants.ELECTRICAL:
                                queryString = AppConstants.SERVICE_TYPE_ELECTRICIAN;
                                break;
                        }
                        Utils.showToast(getContext(), queryString.toUpperCase(Locale.ROOT) + " s" + getString(R.string.no_data_found));
                    } else {
                        searchServiceAdapter.setListItems(sites);
                        searchServiceAdapter.notifyDataSetChanged();
                    }
                    activityUpdateListner.hideShowNavBar(false, getString(R.string.nearby_search_title));
                }


                @Override
                public void onSearchError(SearchStatus searchStatus) {
                    String queryStringValue = queryString.equals("Plumbe") ? "Plumber" : queryString;
                    Utils.showToast(getContext(), queryStringValue + "s " + getString(R.string.no_data_found));
                    getFragmentManager().beginTransaction().remove(SiteKitResultFragment.this).commitAllowingStateLoss();
                    activityUpdateListner.hideShowNavBar(false, "UrbanHomeServices");
                    ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayShowTitleEnabled(false);
                    ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayShowHomeEnabled(false);
                }
            };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            return true;
        }
        return false;

    }

    @Override
    public void onResume() {
        super.onResume();
        activityUpdateListner.hideShowNavBar(true, getString(R.string.nearby_search_title));
    }
}
