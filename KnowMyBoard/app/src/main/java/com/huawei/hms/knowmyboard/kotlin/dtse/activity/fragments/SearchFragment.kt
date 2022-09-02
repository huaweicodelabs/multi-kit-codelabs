package com.huawei.hms.knowmyboard.dtse.activity.fragments

import androidx.navigation.Navigation.findNavController
import com.huawei.hms.knowmyboard.dtse.activity.viewmodel.LoginViewModel
import androidx.navigation.NavController
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.knowmyboard.dtse.activity.adapter.SitesAdapter
import com.huawei.hms.site.api.model.Site
import com.huawei.hms.location.LocationResult
import com.huawei.hms.knowmyboard.dtse.activity.intefaces.ItemClickListener
import android.view.WindowManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.knowmyboard.dtse.R
import com.huawei.hms.knowmyboard.dtse.activity.util.Constants
import com.huawei.hms.knowmyboard.dtse.databinding.FragmentSearchBinding
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.TextSearchRequest
import com.huawei.hms.site.api.model.Coordinate
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.model.TextSearchResponse
import com.huawei.hms.site.api.model.SearchStatus
import com.huawei.hms.site.api.model.NearbySearchRequest
import com.huawei.hms.site.api.model.HwLocationType
import com.huawei.hms.site.api.model.NearbySearchResponse
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.net.URLEncoder
import java.util.ArrayList

class SearchFragment : Fragment() {
    var binding: FragmentSearchBinding? = null
    var loginViewModel: LoginViewModel? = null

    //View view;
    var navController: NavController? = null
    private var searchService: SearchService? = null
    var adapter: SitesAdapter? = null
    var siteArrayList = ArrayList<Site>()
    var locationResult: LocationResult? = null
   /* var siteClicklistener = ItemClickListener { vh, site, pos ->
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
        loginViewModel!!.setSiteSelected(site)
        navController!!.navigate(R.id.loginFragment)
    }*/

    var siteClicklistener = object : ItemClickListener{
        override fun onItemClicked(vh: RecyclerView.ViewHolder?, item: Site?, pos: Int) {
            requireActivity().window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            )
            loginViewModel!!.setSiteSelected(item!!)
            navController!!.navigate(R.id.loginFragment)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        loginViewModel = ViewModelProvider(requireActivity()).get(
            LoginViewModel::class.java
        )
        val searchView = binding!!.edSearch
        val recyclerView = binding!!.suggestionRv
        navController = findNavController(requireActivity(), R.id.nav_host_fragment)
        searchView.isFocusable = true
        searchView.onActionViewExpanded()
        adapter = SitesAdapter(siteArrayList, context, siteClicklistener)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        loginViewModel!!.locationResult.observeForever { locationResult1 ->
            locationResult = locationResult1
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.length > 4) {
                    nearByPlacesSearch(newText)
                }
                return false
            }
        })
        return binding!!.root
    }

    fun keywordSearch(search: String?) {
        try {
            val key = URLEncoder.encode(Constants.API_KEY, "UTF-8")
            // Instantiate the SearchService object.
            searchService = SearchServiceFactory.create(context, key)
            // Create a request body.
            val request = TextSearchRequest()
            request.query = search
            if (locationResult != null) {
                val location = Coordinate(
                    locationResult!!.lastHWLocation.latitude,
                    locationResult!!.lastHWLocation.longitude
                )
                request.location = location
            }
            request.radius = 1000
            //request.setHwPoiType(HwLocationType.HOTEL_MOTEL);
            request.countryCode = "IN"
            request.language = "en"
            request.pageIndex = 1
            request.pageSize = 5
            request.isChildren = false
            // request.setCountries(Arrays.asList("en", "fr", "cn", "de", "ko","in"));
            // Create a search result listener.
            val resultListener: SearchResultListener<TextSearchResponse?> =
                object : SearchResultListener<TextSearchResponse?> {
                    // Return search results upon a successful search.
                    override fun onSearchResult(results: TextSearchResponse?) {
                        if (results == null || results.totalCount <= 0) {
                            return
                        }
                        val sites = results.sites
                        if (sites == null || sites.size == 0) {
                            return
                        }
                        siteArrayList.clear()
                        for (site in sites) {
                            siteArrayList.add(site)
                        }
                        siteArrayList.addAll(sites)
                        adapter!!.notifyDataSetChanged()
                    }

                    // Return the result code and description upon a search exception.
                    override fun onSearchError(status: SearchStatus) {
                        Log.i("TAG", "Error : " + status.errorCode + " " + status.errorMessage)
                    }
                }
            // Call the keyword search API.
            searchService!!.textSearch(request, resultListener)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    fun nearByPlacesSearch(newText: String?) {
        try {
            val key = URLEncoder.encode(Constants.API_KEY, "UTF-8")
            // Instantiate the SearchService object.
            searchService = SearchServiceFactory.create(context, key)
            // Create a request body.
            val request = NearbySearchRequest()
            if (locationResult != null) {
                val location = Coordinate(
                    locationResult!!.lastHWLocation.latitude,
                    locationResult!!.lastHWLocation.longitude
                )
                request.location = location
            }
            request.query = newText
            request.radius = 1000
            request.hwPoiType = HwLocationType.ADDRESS
            request.language = "en"
            request.pageIndex = 1
            request.pageSize = 5
            request.strictBounds = false
            // Create a search result listener.
            val resultListener: SearchResultListener<NearbySearchResponse?> =
                object : SearchResultListener<NearbySearchResponse?> {
                    // Return search results upon a successful search.
                    override fun onSearchResult(results: NearbySearchResponse?) {
                        if (results == null || results.totalCount <= 0) {
                            return
                        }
                        val sites = results.sites
                        if (sites == null || sites.size == 0) {
                            return
                        }
                        siteArrayList.clear()
                        for (site in sites) {
                            siteArrayList.add(site)
                        }
                        siteArrayList.addAll(sites)
                        adapter!!.notifyDataSetChanged()
                    }

                    // Return the result code and description upon a search exception.
                    override fun onSearchError(status: SearchStatus) {
                        Log.i("TAG", "Error : " + status.errorCode + " " + status.errorMessage)
                    }
                }
            // Call the nearby place search API.
            searchService!!.nearbySearch(request, resultListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}