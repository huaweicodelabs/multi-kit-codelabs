package com.huawei.discovertourismapp.fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.discovertourismapp.MainActivity;
import com.huawei.discovertourismapp.R;
import com.huawei.discovertourismapp.activity.NearbySearchActivity;
import com.huawei.discovertourismapp.adapter.NearByAdapter;
import com.huawei.discovertourismapp.model.NearByData;
import com.huawei.discovertourismapp.utils.AppLog;
import com.huawei.discovertourismapp.utils.Constants;
import com.huawei.discovertourismapp.utils.ExceptionHandling;
import com.huawei.discovertourismapp.utils.Util;
import com.huawei.discovertourismapp.viewmodel.AuthServiceViewModel;
import com.huawei.discovertourismapp.viewmodel.PageViewModel;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;


public class NearByFragment extends Fragment implements NearByAdapter.ContactAdapterListener {
    LinearLayout linearLayout;
    CardView hospitals, atm, restaurants, shoppingCentres;
    private static final String TAG = "SpeedDial";
    private PageViewModel pageViewModel;
    Util util;
    NearByData nearByData;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mCurrentLocation = new Location(Constants.STR_EMPTY);
    private LocationCallback mLocationCallback;
    private boolean isCalledHospitalApi;

    public NearByFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment SpeedDialFragment.
     */
    public static NearByFragment newInstance() {
        return new NearByFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = new Util();

        pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);
        pageViewModel.setIndex(TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.activity_main, container, false);
        linearLayout = root.findViewById(R.id.grid_linear);
        shoppingCentres = root.findViewById(R.id.shopping_centre);
        hospitals = root.findViewById(R.id.shopping_centre);
        restaurants = root.findViewById(R.id.Restaurants);
        hospitals = root.findViewById(R.id.hospitals);
        atm = root.findViewById(R.id.atm);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        getLastLocation("");

        shoppingCentres.setOnClickListener(view -> navigatotoNext(1));

        restaurants.setOnClickListener(view -> navigatotoNext(2));

        hospitals.setOnClickListener(view -> navigatotoNext(3));

        atm.setOnClickListener(view -> navigatotoNext(4));

        return root;
    }

    private void navigatotoNext(int type) {
        Intent intent = new Intent(new Intent(getActivity(), NearbySearchActivity.class));
        intent.putExtra("lat", mCurrentLocation.getLatitude());
        intent.putExtra("long", mCurrentLocation.getLongitude());
        intent.putExtra("type", type);
        startActivity(intent);
    }

    /**
     * Obtain the last known location
     */
    private void getLastLocation(String type) {
        try {
            Task<Location> lastLocation = mFusedLocationProviderClient.getLastLocation();
            lastLocation.addOnSuccessListener(location -> {
                if (location == null) {
                    Log.i(TAG, "getLastLocation onSuccess location is null");
                    return;
                }

                String latlong = "" + location.getLatitude() + " -- " + location.getLongitude();
                Log.i(TAG, "getLastLocation onSuccess location[Longitude,Latitude]:"
                        + location.getLongitude() + "," + location.getLatitude());
                mCurrentLocation.setLatitude(location.getLatitude());
                mCurrentLocation.setLongitude(location.getLongitude());

                return;
            }).addOnFailureListener(e -> Log.e(TAG, "getLastLocation onFailure:" + e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, "getLastLocation exception:" + e.getMessage());
        }
    }

    @Override
    public void setContactData(NearByData user) {
        setUser(user);
    }

    private void setUser(NearByData user) {
        this.nearByData = user;
    }

    private void removeLocationUpdatesCallback() {
        try {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, getString(R.string.request_loaction_update_with_success_callback)))
                    .addOnFailureListener(e -> ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.request_loaction_update_with_failure_callback), e));
        } catch (Exception e) {
            ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.request_loaction_update_exception_callback), e);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        removeLocationUpdatesCallback();
    }

}