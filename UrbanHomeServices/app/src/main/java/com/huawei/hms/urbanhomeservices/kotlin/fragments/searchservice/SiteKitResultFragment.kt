/*
 *
 *  * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */
@file:Suppress("NewLineAtEndOfFile","WildcardImport")
package com.huawei.hms.urbanhomeservices.kotlin.fragments.searchservice

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.databinding.SiteKitResultLayoutBinding
import com.huawei.hms.urbanhomeservices.kotlin.adapter.SearchServiceAdapter
import com.huawei.hms.urbanhomeservices.kotlin.listener.ActivityUpdateListner
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils
import com.huawei.hms.urbanhomeservices.kotlin.utils.Utils.hideKeyboard
import java.util.*

/**
 * Implement HMS Site kit
 * Based on user query, search NearBySearch result from HMS Site kit
 * Show list of NearByService
 *
 * @author: Huawei
 * @since 20-01-21
 */

class SiteKitResultFragment : Fragment() {
    private var searchService: SearchService? = null
    private lateinit var imageString: String
    private lateinit var queryString: String
    private lateinit var sites: MutableList<Site>
    private lateinit var activityUpdateListner: ActivityUpdateListner

    private var _siteKitResultBinding: SiteKitResultLayoutBinding? = null
    private val siteKitResultBinding get() = _siteKitResultBinding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityUpdateListner = context as ActivityUpdateListner
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
       _siteKitResultBinding = SiteKitResultLayoutBinding.inflate(inflater, container, false)
        return siteKitResultBinding.root
    }

    @ExperimentalStdlibApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        initRecyclerView()
        searchService = SearchServiceFactory.create(requireActivity(), Utils.getApiKey())
        arguments?.let {
            val request = NearbySearchRequest().apply {
                queryString = it.getString(AppConstants.REQUEST_QUERY).toString()
                setQuery(queryString)
                setLocation(
                        Coordinate(
                                it.getDouble(
                                        AppConstants.SERVICE_LAT_KEY,
                                        AppConstants.DEFAULT_LAT_LNG_VALUE
                                ),
                                it.getDouble(
                                        AppConstants.SERVICE_LNG_KEY,
                                        AppConstants.DEFAULT_LAT_LNG_VALUE
                                )
                        )
                )
            }
            imageString = it.getString(AppConstants.PROVIDER_IMAGE_KEY).toString()
            searchService?.nearbySearch(request, searchResultListener)
        }
    }

    @ExperimentalStdlibApi
    private var searchResultListener: SearchResultListener<NearbySearchResponse> =
            object : SearchResultListener<NearbySearchResponse> {
                override fun onSearchResult(results: NearbySearchResponse?) {
                    hideKeyboard()
                    results?.getSites()?.let {
                        sites = results.getSites()
                        if (sites.isNullOrEmpty()) {
                            when (queryString) {
                                AppConstants.PLUMBE ->
                                    queryString = AppConstants.SERVICE_TYPE_PLUMBER
                                AppConstants.ELECTRICAL ->
                                    queryString = AppConstants.SERVICE_TYPE_ELECTRICIAN
                            }
                            Utils.showToast(
                                    activity,
                                    "${queryString.capitalize(Locale.ROOT)}s ${getString(R.string.no_data_found)}"
                            )
                        } else {
                            siteKitResultBinding.siteKiteResultRV.adapter =
                                    SearchServiceAdapter(requireActivity(), sites, queryString)
                        }
                    } ?: run {
                        when (queryString) {
                            AppConstants.PLUMBE ->
                                queryString = AppConstants.SERVICE_TYPE_PLUMBER
                            AppConstants.ELECTRICAL ->
                                queryString = AppConstants.SERVICE_TYPE_ELECTRICIAN
                        }
                        Utils.showToast(
                                activity,
                                "${queryString.capitalize(Locale.ROOT)}s ${getString(R.string.no_data_found)}"
                        )
                        activityUpdateListner.hideShowNavBar(false, getString(R.string.app_name))
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }

                override fun onSearchError(status: SearchStatus) {
                    Utils.showToast(activity, getString(R.string.msg_unknown_error))
                }
            }

    /**
     * Based on Query And condition, fetch data
     * from  cloudDB
     * Init Recycler view
     * load service by category
     */
    private fun initRecyclerView() {
        siteKitResultBinding.siteKiteResultRV.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            isNestedScrollingEnabled = false
            hasFixedSize()
            itemAnimator = DefaultItemAnimator()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        activityUpdateListner.hideShowNavBar(true, getString(R.string.nearby_search_title))
    }
}