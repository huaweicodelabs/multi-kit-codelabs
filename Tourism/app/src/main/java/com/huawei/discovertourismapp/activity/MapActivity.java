package com.huawei.discovertourismapp.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.huawei.discovertourismapp.R;
import com.huawei.discovertourismapp.utils.Constants;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.MapsInitializer;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.SupportMapFragment;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.Site;

public class MapActivity extends BaseActivity implements OnMapReadyCallback {
    double lat, lng;
    String locationName;
    SupportMapFragment mSupportMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Bundle bundle = getIntent().getExtras();
        lat = bundle.getDouble("lat");
        lng = bundle.getDouble("lng");
        locationName = bundle.getString("location_name");
        MapsInitializer.setApiKey(Constants.apiKey);

        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment_mapfragmentdemo);
        mSupportMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        HuaweiMap hMap = huaweiMap;
        LatLng hospitalLatLng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(hospitalLatLng, 12);
        huaweiMap.moveCamera(cameraUpdate);
        hMap.addMarker(new MarkerOptions().position(hospitalLatLng));
    }
}
