package com.huawei.discovertourismapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.huawei.discovertourismapp.R;
import com.huawei.discovertourismapp.adapter.NearByAdapter;
import com.huawei.discovertourismapp.utils.Util;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.AddressDetail;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.LocationType;
import com.huawei.hms.site.api.model.NearbySearchRequest;
import com.huawei.hms.site.api.model.NearbySearchResponse;
import com.huawei.hms.site.api.model.Poi;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;

import java.util.Arrays;
import java.util.List;

public class NearbySearchActivity extends AppCompatActivity {
    private SearchService searchService;
    RecyclerView recyclerView;
    Util util;
    int type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_search);
        util = new Util();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Double lat = getIntent().getDoubleExtra("lat", 0.0);
        Double lng = getIntent().getDoubleExtra("long", 0.0);
        type = getIntent().getIntExtra("type",0);
        searchService = SearchServiceFactory.create(this, Util.getApiKey(this));
        nearbySearch(type, lat, lng);
    }

    SearchResultListener searchResultListener = new SearchResultListener<NearbySearchResponse>() {
        @Override
        public void onSearchResult(NearbySearchResponse results) {
            StringBuilder stringBuilder = new StringBuilder();
            if (results != null) {
                List<Site> sites = results.getSites();

                NearByAdapter nearByAdapter = new NearByAdapter(NearbySearchActivity.this, sites);
                recyclerView.setAdapter(nearByAdapter);
                util.stopProgressBar();

            }
            showSuccessResult(stringBuilder.toString());
        }

        @Override
        public void onSearchError(SearchStatus status) {
            showFailResult("", status.getErrorCode(), status.getErrorMessage());
        }
    };

    private void nearbySearch(int type, Double lat, Double lng) {

        util.showProgressBar(this);
        NearbySearchRequest request = new NearbySearchRequest();
        request.setLocation(new Coordinate(lat, lng));
        if(type==1){
            request.setPoiType(LocationType.SHOPPING_MALL);
        }else if(type==2){
            request.setPoiType(LocationType.RESTAURANT);
        }else if(type==3){
            request.setPoiType(LocationType.HOSPITAL);
        }else if(type==4){
            request.setPoiType(LocationType.ATM);
        }
        request.setRadius(10000);
        searchService.nearbySearch(request, searchResultListener);

    }

    private void showFailResult(String result, String errorCode, String errorMessage) {
    }

    private void showSuccessResult(String result) {
    }
}