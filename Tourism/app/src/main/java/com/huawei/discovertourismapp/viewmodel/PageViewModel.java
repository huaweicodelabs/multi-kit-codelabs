package com.huawei.discovertourismapp.viewmodel;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.huawei.discovertourismapp.R;
import com.huawei.discovertourismapp.model.NearByData;
import com.huawei.discovertourismapp.model.User;
import com.huawei.discovertourismapp.utils.Constants;
import com.huawei.discovertourismapp.utils.ExceptionHandling;
import com.huawei.discovertourismapp.utils.Util;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.LocationType;
import com.huawei.hms.site.api.model.NearbySearchRequest;
import com.huawei.hms.site.api.model.NearbySearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class PageViewModel extends ViewModel {
    private final MutableLiveData<String> mTitle = new MutableLiveData<>();
    public MutableLiveData<ArrayList<NearByData>> userMutableLiveData = new MutableLiveData<>();
    Util util=new Util();
    private ArrayList<Site> mSites = new ArrayList<>();
    private final ArrayList<NearByData> nearByDataList = new ArrayList<>();
    private final Location mCurrentLocation = new Location(Constants.STR_EMPTY);


    private final LiveData<String> mText = Transformations.map(mTitle, new Function<String, String>() {
        @Override
        public String apply(String input) {
            return "Contact not available in " + input;
        }
    });
    public void setIndex(String index) {
        mTitle.setValue(index);
    }

    private String getApiKey(String apiKey) {
        String encodeKey = Constants.STR_EMPTY;
        try {
            encodeKey = URLEncoder.encode(apiKey, Constants.LANGUAGE_EN );
        } catch (UnsupportedEncodingException e) {
           // ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_unsupported_encoding), e);
        }
        return encodeKey;
    }
    public void getNearByData(Context context, String type,double lat,double lng) {
        SearchService searchService = null;
        try {
            searchService = SearchServiceFactory.create(context, URLEncoder.encode(context.getResources().getString(R.string.api_key), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        NearbySearchRequest mHospitalRequest = new NearbySearchRequest();
        mHospitalRequest.setRadius(Constants.INIT_1000);
        mHospitalRequest.setLocation(new Coordinate(lat, lng));
       // mHospitalRequest.setPoiType(LocationType.HOSPITAL);
        //  mHospitalRequest.setPageIndex(1);
        //  mHospitalRequest.setPageSize(5);
        //  mHospitalRequest.setQuery("");
        //mHospitalRequest.setLanguage(Constants.LANGUAGE_EN);

        switch (type){
            case Constants.SHOPPING_CENTERS:
                mHospitalRequest.setPoiType(LocationType.HOSPITAL);
                break;
            case Constants.RESTAURANTS:
                mHospitalRequest.setPoiType(LocationType.RESTAURANT);
                break;
            case Constants.HOTELS:
                mHospitalRequest.setPoiType(LocationType.HOSPITAL);
                break;
            case Constants.TOURIST_SPOTS:
                mHospitalRequest.setPoiType(LocationType.TOURIST_ATTRACTION);
                break;
        }

        searchService.nearbySearch(mHospitalRequest, mListener);

    }

    SearchResultListener<NearbySearchResponse> mListener = new SearchResultListener<NearbySearchResponse>() {
        @Override
        public void onSearchResult(NearbySearchResponse nearbySearchResponse) {
            mSites = new ArrayList<>();
            mSites = (ArrayList<Site>) nearbySearchResponse.getSites();

            for(Site site:mSites){
                NearByData nearByData=new NearByData();
                nearByData.setName(site.getName());
                nearByDataList.add(nearByData);
            }

            if (mSites != null) {
                userMutableLiveData.setValue(nearByDataList);
            } else {
                //Log.d(TAG, getString(R.string.no_near_hospital));
            }
        }

        @Override
        public void onSearchError(SearchStatus searchStatus) {
            if(searchStatus.getErrorCode().equals("10004")){
                //  Toast.makeText(context, "No Records Found", Toast.LENGTH_SHORT).show();
                userMutableLiveData.setValue(nearByDataList);
            }
            // Toast.makeText(context, searchStatus.getErrorMessage(), Toast.LENGTH_SHORT).show();

        }
    };
}