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

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;

import static com.huawei.hms.maps.HuaweiMap.MAP_TYPE_NORMAL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.huawei.hms.knowmyboard.dtse.R;
import com.huawei.hms.knowmyboard.dtse.activity.util.Constants;
import com.huawei.hms.knowmyboard.dtse.activity.util.MySharedPreferences;
import com.huawei.hms.knowmyboard.dtse.activity.util.RequestLocationData;
import com.huawei.hms.knowmyboard.dtse.activity.viewmodel.LoginViewModel;
import com.huawei.hms.knowmyboard.dtse.databinding.FragmentLoginBinding;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapsInitializer;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.site.api.model.Site;

public class LoginFragment extends Fragment implements OnMapReadyCallback {
    FragmentLoginBinding loginBinding;
    LoginViewModel loginViewModel;
    Menu menu;
    NavController navController;
    private String TAG = "TAG";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private Marker siteMarker;
    RequestLocationData locationData;
    HuaweiMap hMap;
    int search = 0;
    LatLng latLng = new LatLng(1.0f, 2.0f);
    private Site site;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        loginBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        loginViewModel = new ViewModelProvider(getActivity()).get(LoginViewModel.class);
        loginBinding.setLoginViewModel(loginViewModel);

        locationData = new RequestLocationData(getContext(), getActivity(), loginViewModel);
        locationData.setLoginViewModel(loginViewModel);
        locationData.initFusionLocationProviderClint();
        locationData.checkPermission();
        locationData.checkDeviceLocationSettings();

        if (!getPreferenceValue().equals(Constants.USER_NAME)) {
            enableMenu(menu);
            getActivity().setTitle(getPreferenceValue());
        }

        initMap(savedInstanceState);
        loginViewModel
                .getSiteSelected()
                .observeForever(
                        new Observer<Site>() {
                            @Override
                            public void onChanged(Site site1) {
                                site = site1;
                                search = 1;
                            }
                        });
        loginViewModel
                .getMessage()
                .observeForever(
                        new Observer<String>() {
                            @Override
                            public void onChanged(String message) {
                                updateMessage(message);
                                if (!message.equals(getResources().getString(R.string.app_name))) {
                                    setPreferenceValue(message);
                                    enableMenu(menu);
                                } else {
                                    disableMenu(menu);
                                    resetPreference();
                                }
                            }
                        });
        loginViewModel
                .getLocationResult()
                .observeForever(
                        new Observer<LocationResult>() {
                            @Override
                            public void onChanged(LocationResult locationResult) {
                                refreshLocation(locationResult);
                            }
                        });

        return loginBinding.getRoot();
    }

    private void initMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        loginBinding.mapview.onCreate(mapViewBundle);
        loginBinding.mapview.getMapAsync(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Initialize the SDK.
        MapsInitializer.initialize(getContext());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
        disableMenu(menu);
    }

    private void disableMenu(Menu menu) {
        try {
            if (menu != null) {
                if (MySharedPreferences.getInstance(getActivity())
                        .getStringValue(Constants.USER_NAME)
                        .equals(Constants.USER_NAME)) {
                    menu.findItem(R.id.menu_login_logout).setVisible(false);
                    menu.findItem(R.id.menu_cancel_auth).setVisible(false);
                    menu.findItem(R.id.menu_login).setVisible(true);
                    getActivity().setTitle(getResources().getString(R.string.app_name));
                } else {
                    menu.findItem(R.id.menu_login_logout).setVisible(true);
                    menu.findItem(R.id.menu_cancel_auth).setVisible(true);
                    menu.findItem(R.id.menu_login).setVisible(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableMenu(Menu menu) {
        try {
            menu.findItem(R.id.menu_login_logout).setVisible(true);
            menu.findItem(R.id.menu_cancel_auth).setVisible(true);
            menu.findItem(R.id.menu_login).setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cancel_auth:
                resetPreference();
                loginViewModel.cancelAuthorization();
                disableMenu(menu);
                return true;
            case R.id.menu_login_logout:
                resetPreference();
                loginViewModel.logoutHuaweiID();
                disableMenu(menu);
                return true;
            case R.id.menu_login:
                loginViewModel.loginClicked();
                return true;
            case R.id.option_refresh_location:
                locationData.refreshLocation();

                return true;
            case R.id.menu_share:
                shareLocation();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareLocation() {
        if (latLng != null) {
            String shareData = "mapapp://navigation?saddr=" + latLng.latitude + "," + latLng.longitude;
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Share location");
            intent.putExtra(Intent.EXTRA_TEXT, shareData);
            startActivity(Intent.createChooser(intent, "Share"));

        } else {
            Toast.makeText(getActivity(), "Please refresh the location.", Toast.LENGTH_LONG).show();
        }
    }

    private void resetPreference() {
        MySharedPreferences.getInstance(getActivity()).putStringValue(Constants.USER_NAME, Constants.USER_NAME);
    }

    public void updateMessage(String msg) {
        getActivity().setTitle(msg);
    }

    void setPreferenceValue(String message) {
        MySharedPreferences.getInstance(getActivity()).putStringValue(Constants.USER_NAME, message);
    }

    String getPreferenceValue() {
        return MySharedPreferences.getInstance(getActivity()).getStringValue(Constants.USER_NAME);
    }

    @RequiresPermission(allOf = {ACCESS_FINE_LOCATION, ACCESS_WIFI_STATE})
    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        hMap = huaweiMap;
        if (search == 1) {
            if (site != null) {
                addMarker(site);
            }

        } else {
            try {
                onHuaweiMapReady();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onHuaweiMapReady() {
        if (hMap != null) {
            hMap.setMapType(MAP_TYPE_NORMAL);
            hMap.setBuildingsEnabled(true);
            // Enable the my-location layer.
            hMap.setMyLocationEnabled(true);
            // Enable the my-location icon.
            hMap.getUiSettings().setMyLocationButtonEnabled(true);
            hMap.getUiSettings().setGestureScaleByMapCenter(true);
        }
        if (locationData != null) {
            locationData.refreshLocation();
        }
    }

    private void refreshLocation(LocationResult locationResult) {
        try {
            latLng =
                    new LatLng(
                            locationResult.getLastHWLocation().getLatitude(),
                            locationResult.getLastHWLocation().getLongitude());
            moveCamera(latLng, 10.1f);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moveCamera(LatLng latLng, float zoomRate) {
        float zoom = zoomRate;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        hMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        loginBinding.mapview.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        loginBinding.mapview.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        loginBinding.mapview.onStop();
        locationData.disableLocationData();
    }

    @Override
    public void onPause() {
        super.onPause();
        loginBinding.mapview.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        loginBinding.mapview.onStart();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        try {
            loginBinding.mapview.onSaveInstanceState(mapViewBundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    public void addMarker(Site site) {
        if (null != siteMarker) {
            siteMarker.remove();
        }
        MarkerOptions options =
                new MarkerOptions().position(new LatLng(site.getLocation().getLat(), site.getLocation().getLng())).title(site.getName()).snippet(site.getFormatAddress());
        siteMarker = hMap.addMarker(options);
        hMap.addMarker(options);
        moveCamera(latLng, 14f);
    }
}
