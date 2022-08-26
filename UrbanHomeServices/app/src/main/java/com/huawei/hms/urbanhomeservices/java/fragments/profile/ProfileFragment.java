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

package com.huawei.hms.urbanhomeservices.java.fragments.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.identity.entity.GetUserAddressResult;
import com.huawei.hms.identity.entity.UserAddress;
import com.huawei.hms.identity.entity.UserAddressRequest;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.activities.MainActivity;
import com.huawei.hms.urbanhomeservices.java.listener.ActivityUpdateListener;
import com.huawei.hms.urbanhomeservices.java.model.LoginModel;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;
import com.huawei.hms.urbanhomeservices.java.utils.Utils;

import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = ProfileFragment.class.getSimpleName();
    private ActivityUpdateListener activityUpdateListner;
    private TextView userName;
    private TextView userEmail;
    private TextView user_address_id;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activityUpdateListner = (ActivityUpdateListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_j, container, false);
        Utils.IS_PROFILE_FRAGMENT = true;
        userName = view.findViewById(R.id.userName);
        userEmail = view.findViewById(R.id.userEmail);
        user_address_id = view.findViewById(R.id.user_address_id);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);
        Button queryUserAddress = view.findViewById(R.id.query_user_address);
        queryUserAddress.setOnClickListener(this);
        showProfile();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogout:
                Utils.logoutDialog(getActivity());
                break;
            case R.id.query_user_address:

                if (Utils.isConnected(getContext())) {
                    getUserAddress();
                } else {
                    Utils.showToast(((MainActivity) getContext()), getString(R.string.check_internet));
                }
        }
    }

    /**
     * Show User address
     */
    private void showProfile() {
        LoginModel loginModel = Utils.getSharePrefData(getContext());
        userName.setText(HtmlCompat.fromHtml("<b>Name: </b>" + loginModel.getDisplayName(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        userEmail.setText(HtmlCompat.fromHtml("<b>Email: </b>" + loginModel.getEmail(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        getCompleteAddressString(Utils.CURRENT_LAT, Utils.CURRENT_LON);
    }

    /**
     * Get User address based on current lat and long
     *
     * @param LATITUDE latitude of address
     * @param LONGITUDE longitude of address
     * @return String address from lat and lon
     */
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            Address returnedAddress = addresses.get(0);
            StringBuilder strReturnedAddress = new StringBuilder("");
            for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(AppConstants.NEW_LINE);
            }
            strAdd = strReturnedAddress.toString();
        } catch (Exception e) {
            Log.d(TAG, "UserAddressException");
        }
        return strAdd;
    }

    /**
     * Get User address from Identity Kit
     */
    private void getUserAddress() {
        UserAddressRequest req = new UserAddressRequest();
        Task<GetUserAddressResult> task = com.huawei.hms.identity.Address.getAddressClient(getContext()).getUserAddress(req);
        task.addOnSuccessListener(result -> {
            try {
                startActivityForResult(result);
            } catch (IntentSender.SendIntentException ignored) {
                ignored.printStackTrace();
            }
        }).addOnFailureListener(e -> Log.i(TAG, "on Failed result code:"));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.LOGIN_GET_ADDRESS_REQUESTCODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    UserAddress userAddress = UserAddress.parseIntent(data);
                    if (userAddress != null) {
                        Utils.storeCountryName(getContext(), userAddress.getCountryCode());
                        StringBuilder sb = new StringBuilder();
                        sb.append("<b>Name: </b>" + userAddress.getName() + "<br>");
                        sb.append("<b>Address: </b>" + userAddress.getAddressLine1() + userAddress.getAddressLine2() + "<br>");
                        sb.append("<b>City: </b>" + userAddress.getLocality() + "<br>");
                        sb.append("<b>State: </b>" + userAddress.getAdministrativeArea() + "<br>");
                        sb.append("<b>Country: </b>" + userAddress.getCountryCode() + "<br>");
                        sb.append("<b>Phone: </b>" + userAddress.getPhoneNumber());
                        user_address_id.setText(sb);
                    } else if (!user_address_id.equals(AppConstants.SEARCH_NAME_KEY)) {
                        user_address_id.setText(getString(R.string.failed_address_text));
                    }
            }
        }
    }

    /**
     * Get User address from Identity Kit
     *
     * @param result user address result
     */
    private void startActivityForResult(GetUserAddressResult result) throws IntentSender.SendIntentException {
        Status status = result.getStatus();
        if (result.getReturnCode() == 0 && status.hasResolution()) {
            status.startResolutionForResult(getActivity(), AppConstants.LOGIN_GET_ADDRESS_REQUESTCODE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        activityUpdateListner.hideShowNavBar(false, getString(R.string.title_profile));
        if (!Utils.getUserAddress(getContext()).isEmpty()) {
            user_address_id.setText(HtmlCompat.fromHtml(
                    Utils.getUserAddress(getContext()),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
            ));
        }
    }
}
