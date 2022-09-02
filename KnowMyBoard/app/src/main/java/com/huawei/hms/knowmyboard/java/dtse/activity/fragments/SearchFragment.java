/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
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

package com.huawei.hms.knowmyboard.dtse.activity.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.knowmyboard.dtse.R;
import com.huawei.hms.knowmyboard.dtse.activity.adapter.SitesAdapter;
import com.huawei.hms.knowmyboard.dtse.activity.intefaces.ItemClickListener;
import com.huawei.hms.knowmyboard.dtse.activity.util.Constants;
import com.huawei.hms.knowmyboard.dtse.activity.viewmodel.LoginViewModel;
import com.huawei.hms.knowmyboard.dtse.databinding.FragmentSearchBinding;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.HwLocationType;
import com.huawei.hms.site.api.model.NearbySearchRequest;
import com.huawei.hms.site.api.model.NearbySearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;
import com.huawei.hms.site.api.model.TextSearchRequest;
import com.huawei.hms.site.api.model.TextSearchResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    FragmentSearchBinding binding;
    LoginViewModel loginViewModel;

    // View view;
    NavController navController;
    private SearchService searchService;
    SitesAdapter adapter;
    ArrayList<Site> siteArrayList = new ArrayList<>();
    LocationResult locationResult = null;

    public SearchFragment() {
        // Required empty public constructor
    }

    ItemClickListener siteClicklistener =
            new ItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView.ViewHolder vh, Site site, int pos) {
                    getActivity()
                            .getWindow()
                            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    loginViewModel.setSiteSelected(site);
                    navController.navigate(R.id.loginFragment);
                }
            };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        loginViewModel = new ViewModelProvider(getActivity()).get(LoginViewModel.class);

        SearchView searchView = binding.edSearch;
        RecyclerView recyclerView = binding.suggestionRv;
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        searchView.setFocusable(true);
        searchView.onActionViewExpanded();
        adapter = new SitesAdapter(siteArrayList, getContext(), siteClicklistener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        loginViewModel
                .getLocationResult()
                .observeForever(
                        new Observer<LocationResult>() {
                            @Override
                            public void onChanged(LocationResult locationResult1) {
                                locationResult = locationResult1;
                            }
                        });
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (newText.length() > 4) {
                            nearByPlacesSearch(newText);
                        }
                        return false;
                    }
                });

        return binding.getRoot();
    }

    void keywordSearch(String search) {
        try {
            String key = URLEncoder.encode(Constants.API_KEY, "UTF-8");
            // Instantiate the SearchService object.
            searchService = SearchServiceFactory.create(getContext(), key);
            // Create a request body.
            TextSearchRequest request = new TextSearchRequest();
            request.setQuery(search);
            if (locationResult != null) {
                Coordinate location =
                        new Coordinate(
                                locationResult.getLastHWLocation().getLatitude(),
                                locationResult.getLastHWLocation().getLongitude());
                request.setLocation(location);
            }
            request.setRadius(1000);
            request.setCountryCode("IN");
            request.setLanguage("en");
            request.setPageIndex(1);
            request.setPageSize(5);
            request.setChildren(false);
            // Create a search result listener.
            SearchResultListener<TextSearchResponse> resultListener =
                    new SearchResultListener<TextSearchResponse>() {
                        // Return search results upon a successful search.
                        @Override
                        public void onSearchResult(TextSearchResponse results) {
                            if (results == null || results.getTotalCount() <= 0) {
                                return;
                            }
                            List<Site> sites = results.getSites();
                            if (sites == null || sites.size() == 0) {
                                return;
                            }

                            siteArrayList.clear();
                            for (Site site : sites) {
                                siteArrayList.add(site);
                            }

                            siteArrayList.addAll(sites);
                            adapter.notifyDataSetChanged();
                        }

                        // Return the result code and description upon a search exception.
                        @Override
                        public void onSearchError(SearchStatus status) {
                            Log.i("TAG", "Error : " + status.getErrorCode() + " " + status.getErrorMessage());
                        }
                    };
            // Call the keyword search API.
            searchService.textSearch(request, resultListener);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    void nearByPlacesSearch(String newText) {
        try {
            String key = URLEncoder.encode(Constants.API_KEY, "UTF-8");
            // Instantiate the SearchService object.
            searchService = SearchServiceFactory.create(getContext(), key);
            // Create a request body.
            NearbySearchRequest request = new NearbySearchRequest();
            if (locationResult != null) {
                Coordinate location =
                        new Coordinate(
                                locationResult.getLastHWLocation().getLatitude(),
                                locationResult.getLastHWLocation().getLongitude());
                request.setLocation(location);
            }
            request.setQuery(newText);
            request.setRadius(1000);
            request.setHwPoiType(HwLocationType.ADDRESS);
            request.setLanguage("en");
            request.setPageIndex(1);
            request.setPageSize(5);
            request.setStrictBounds(false);
            // Create a search result listener.
            SearchResultListener<NearbySearchResponse> resultListener =
                    new SearchResultListener<NearbySearchResponse>() {
                        // Return search results upon a successful search.
                        @Override
                        public void onSearchResult(NearbySearchResponse results) {
                            if (results == null || results.getTotalCount() <= 0) {
                                return;
                            }
                            List<Site> sites = results.getSites();
                            if (sites == null || sites.size() == 0) {
                                return;
                            }
                            siteArrayList.clear();
                            for (Site site : sites) {
                                siteArrayList.add(site);
                            }

                            siteArrayList.addAll(sites);
                            adapter.notifyDataSetChanged();
                        }

                        // Return the result code and description upon a search exception.
                        @Override
                        public void onSearchError(SearchStatus status) {
                            Log.i("TAG", "Error : " + status.getErrorCode() + " " + status.getErrorMessage());
                        }
                    };
            // Call the nearby place search API.
            searchService.nearbySearch(request, resultListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
