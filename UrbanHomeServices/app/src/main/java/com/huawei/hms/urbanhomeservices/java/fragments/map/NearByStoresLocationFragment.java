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

package com.huawei.hms.urbanhomeservices.java.fragments.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.MapsInitializer;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.CameraPosition;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.LatLngBounds;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.maps.model.Polyline;
import com.huawei.hms.maps.model.PolylineOptions;
import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.fragments.home.HomeViewModel;
import com.huawei.hms.urbanhomeservices.java.listener.ActivityUpdateListener;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;
import com.huawei.hms.urbanhomeservices.java.utils.NetworkRequestManager;
import com.huawei.hms.urbanhomeservices.java.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class displays route  between Consumer and Service Provider.
 *
 * @author: Huawei
 * @since 20-01-21
 */

public class NearByStoresLocationFragment extends Fragment implements OnMapReadyCallback {

    public static final String TAG = NearByStoresLocationFragment.class.getSimpleName();
    private HuaweiMap hMap;
    private double lat = 0.0;
    private double lng = 0.0;
    private String storeAddress = null;
    private String storeName = null;
    private ArrayList<ArrayList<LatLng>> mPaths = new ArrayList();
    private LatLngBounds mLatLngBounds = null;
    private LatLng currentLocLatLng;
    private LatLng destLocLatLng = null;
    private List<Polyline> mPolylines = new ArrayList();
    private Marker mMarkerOrigin = null;
    private Marker mMarkerDestination = null;
    private ActivityUpdateListener activityUpdateListner;
    private MapView mapView;
    private Activity mActivity;
    private LatLng LatLng;

    private Handler mHandler = new Handler(msg -> {
        switch (msg.what) {
            case 0:
                renderRoute(mPaths, mLatLngBounds);
                // fall through
            case 1:
                Bundle bundle = new Bundle();
                bundle.putString(String.valueOf(msg.getData()), "");
                String errorMsg = bundle.getString("errorMsg");
                Utils.showToast(getContext(), errorMsg);
                // fall through
        }
        return false;
    });

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
        View view = inflater.inflate(R.layout.activity_near_by_stores_location, container, false);
        TextView titleToolbarTextView = mActivity.findViewById(R.id.toolbar_title);
        Toolbar toolbarNearbyStoresLocation = mActivity.findViewById(R.id.toolbar_home);
        ((AppCompatActivity) mActivity).setSupportActionBar(toolbarNearbyStoresLocation);
        ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) mActivity).getSupportActionBar().setTitle("");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapView = getView().findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(AppConstants.MAPVIEW_BUNDLE_KEY);
        }
        MapsInitializer.setApiKey(AppConstants.API_KEY);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
        Bundle arguments = getArguments();
        lat = arguments.getDouble(AppConstants.SERVICE_LAT_KEY);
        lng = arguments.getDouble(AppConstants.SERVICE_LNG_KEY);
        storeName = arguments.getString(AppConstants.SERVICE_STORE_NAME_KEY);
        storeAddress = arguments.getString(AppConstants.SERVICE_ADDR_KEY);
        initViewModel();
    }

    /**
     * Initialize HomeViewModel class
     */

    private void initViewModel() {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        activityUpdateListner.hideShowNavBar(true, getString(R.string.nearby_search_services_providers));
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        currentLocLatLng = new LatLng(Utils.CURRENT_LAT, Utils.CURRENT_LON);
        destLocLatLng = new LatLng(this.lat, this.lng);
        hMap = huaweiMap;
        hMap.isMyLocationEnabled();
        CameraPosition build = new CameraPosition.Builder().target(currentLocLatLng).zoom(2f).tilt(45f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(build);
        hMap.animateCamera(cameraUpdate);
        hMap.moveCamera(cameraUpdate);
        addOriginMarker(currentLocLatLng);
        addDestinationMarker(destLocLatLng);
        removePolylines();
        hMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocLatLng, 13f));
        mMarkerOrigin.showInfoWindow();
        hMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destLocLatLng, 13f));
        hMap.resetMinMaxZoomPreference();
        mMarkerDestination.showInfoWindow();
        getDrivingRouteResult();
    }

    /**
     * To get driving route
     */
    public void getDrivingRouteResult() {
        removePolylines();
        NetworkRequestManager.getDrivingRoutePlanningResult(currentLocLatLng, destLocLatLng,
                new NetworkRequestManager.OnNetworkListener() {
                    @Override
                    public void requestSuccess(String result) {
                        generateRoute(result);
                    }

                    @Override
                    public void requestFail(String errorMsg) {
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putString("errorMsg", errorMsg);
                        msg.what = 1;
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                });
    }

    /**
     * To generate Route
     *
     * @param json json string for route
     */

    private void generateRoute(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray routes = jsonObject.optJSONArray("routes");
            if (null == routes || routes.length() == 0) {
                return;
            }
            JSONObject route = routes.getJSONObject(0);

            // get route bounds
            JSONObject bounds = route.optJSONObject("bounds");
            if (null != bounds && bounds.has("southwest") && bounds.has("northeast")) {
                JSONObject southwest = bounds.optJSONObject("southwest");
                JSONObject northeast = bounds.optJSONObject("northeast");
                LatLng sw = new LatLng(southwest.optDouble("lat"), southwest.optDouble("lng"));
                LatLng ne = new LatLng(northeast.optDouble("lat"), northeast.optDouble("lng"));
                mLatLngBounds = new LatLngBounds(sw, ne);
            }

            // get paths
            JSONArray paths = route.optJSONArray("paths");
            for (int i = 0; i < paths.length(); i++) {
                JSONObject path = paths.optJSONObject(i);
                List<LatLng> mPath = new ArrayList<>();

                JSONArray steps = path.optJSONArray("steps");
                for (int j = 0; j < steps.length(); j++) {
                    JSONObject step = steps.optJSONObject(j);

                    JSONArray polyline = step.optJSONArray("polyline");
                    for (int k = 0; k < polyline.length(); k++) {
                        if (j > 0 && k == 0) {
                            continue;
                        }
                        JSONObject line = polyline.getJSONObject(k);
                        LatLng latLng = new LatLng(line.optDouble("lat"), line.optDouble("lng"));
                        mPath.add(latLng);
                    }
                }
                mPaths.add(i, (ArrayList<LatLng>) mPath);
            }
            mHandler.sendEmptyMessage(0);

        } catch (JSONException e) {
            Log.e(TAG, "JSONException" + e.toString());
        }
    }

    /**
     * Render the route planning result
     *
     * @param paths         latlng path value
     * @param latLangBounds latlang bounds value
     */

    private void renderRoute(ArrayList<ArrayList<LatLng>> paths, LatLngBounds latLangBounds) {

        if (null == paths || paths.size() <= 0 || paths.get(0).size() <= 0) {
            return;
        }

        for (int i = 0; i < paths.size(); i++) {
            List<LatLng> path = paths.get(i);
            PolylineOptions options = new PolylineOptions().color(Color.BLUE).width(5);
            for (LatLng latLng : path) {
                options.add(latLng);
            }
            Polyline polyline = hMap.addPolyline(options);
            mPolylines.add(i, polyline);
        }
        addOriginMarker(paths.get(0).get(0));
        addDestinationMarker(paths.get(0).get(paths.get(0).size() - 1));
        if (null != latLangBounds) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLangBounds, 5);
            hMap.moveCamera(cameraUpdate);
        } else {
            hMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paths.get(0).get(0), 13));
        }
        addOriginMarker(paths.get(0).get(0));
        addDestinationMarker(paths.get(0).get(paths.get(0).size() - 1));
        if (null != latLangBounds) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLangBounds, 5);
            hMap.moveCamera(cameraUpdate);
        } else {
            hMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paths.get(0).get(0), 11f));
        }
    }

    /**
     * To remove polylines
     */

    private void removePolylines() {
        for (Polyline polyline : mPolylines) {
            polyline.remove();
        }
        mPolylines.clear();
        mPaths.clear();
        mLatLngBounds = null;
    }

    /**
     * This method shows marker for Consumer location on HMS Map
     *
     * @param latLng marker latlng
     */

    private void addOriginMarker(LatLng latLng) {
        if (null != mMarkerOrigin) {
            mMarkerOrigin.remove();
        }
        String address = getCompleteAddressString(Utils.CURRENT_LAT, Utils.CURRENT_LON);
        mMarkerOrigin = hMap.addMarker(
                new MarkerOptions().position(latLng)
                        .anchorMarker(0.5f, 0.9f)
                        .title(getString(R.string.title_current_location))
                        .snippet(address)
        );
    }

    /**
     * This method shows marker for Service Provider location on HMS Map
     *
     * @param latLng marker latlng
     */

    private void addDestinationMarker(LatLng latLng) {
        if (null != mMarkerDestination) {
            mMarkerDestination.remove();
        }
        mMarkerDestination = hMap.addMarker(
                new MarkerOptions().position(latLng).anchorMarker(0.5f, 0.9f).title(storeName)
                        .snippet(storeAddress)
        );
    }


    /**
     * This method fetches the complete address by using Lat and Long
     * <p>
     *
     * @param LATITUDE  latitude of address
     * @param LONGITUDE longitude of address
     * @return String address of given lat and lon
     */

    private String getCompleteAddressString(Double LATITUDE, Double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            Address returnedAddress = addresses.get(0);
            StringBuilder strReturnedAddress = new StringBuilder("");
            for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(AppConstants.NEW_LINE);
            }
            strAdd = strReturnedAddress.toString();
        } catch (Exception e) {
            Log.e(TAG, "Exception" + e.toString());
        }
        return strAdd;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}

