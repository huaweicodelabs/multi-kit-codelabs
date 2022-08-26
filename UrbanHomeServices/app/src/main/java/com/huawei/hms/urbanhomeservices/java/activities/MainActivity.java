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

package com.huawei.hms.urbanhomeservices.java.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.identity.Address;
import com.huawei.hms.identity.entity.GetUserAddressResult;
import com.huawei.hms.identity.entity.UserAddress;
import com.huawei.hms.identity.entity.UserAddressRequest;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.listener.ActivityUpdateListener;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;
import com.huawei.hms.urbanhomeservices.java.utils.AppPreferences;
import com.huawei.hms.urbanhomeservices.java.utils.Utils;

/**
 * Used for navigation between screens.
 * Also provides logout functionality from consumer module
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class MainActivity extends AppCompatActivity implements ActivityUpdateListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private int showDialogCount = 0;
    private TextView titleToolbarTextView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_j);
        NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
        BottomNavigationView navigationView = findViewById(R.id.navView);
        titleToolbarTextView = findViewById(R.id.toolbar_title);
        toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        AppBarConfiguration.Builder appBarConfigurationBuilder = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_profile);
        AppBarConfiguration appBarConfiguration = appBarConfigurationBuilder.build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        isFirstTimeLogin();
    }

    /**
     * To hide/show Navigation bottom bar
     *
     * @param hideShow value to show or hide navigation
     * @param title title text
     */
    @Override
    public void hideShowNavBar(boolean hideShow, String title) {
        titleToolbarTextView.setText(title);
        getSupportActionBar().setDisplayShowTitleEnabled(hideShow);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * On back button pressed, manage Fragment BackStack
     */
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (Utils.IS_PROFILE_FRAGMENT) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Utils.IS_PROFILE_FRAGMENT = false;
        } else if (count > 0) {
            FragmentManager.BackStackEntry entryValue = getSupportFragmentManager().getBackStackEntryAt(count - 1);
            getSupportFragmentManager().popBackStack();
            titleToolbarTextView.setText(entryValue.getName());
            if (entryValue.getName().equals(getString(R.string.app_name))) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);
            }
        } else {
            Utils.logoutDialog(this);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                int count = getSupportFragmentManager().getBackStackEntryCount();
                if (Utils.IS_PROFILE_FRAGMENT) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    Utils.IS_PROFILE_FRAGMENT = false;
                } else if (count > 0) {
                    FragmentManager.BackStackEntry entryValue = getSupportFragmentManager().getBackStackEntryAt(count - 1);
                    getSupportFragmentManager().popBackStack();
                    titleToolbarTextView.setText(entryValue.getName());
                    if (entryValue.getName().equals(getString(R.string.app_name))) {
                        getSupportActionBar().setDisplayShowTitleEnabled(false);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        getSupportActionBar().setDisplayShowHomeEnabled(false);
                    }
                }
                break;
            case R.id.item_logout:
                Utils.logoutDialog(this);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult requestCode " + requestCode + " resultCode " + resultCode);
        if (requestCode == AppConstants.LOGIN_GET_ADDRESS_REQUESTCODE) {
            onGetAddressResult(resultCode, data);
        }
        Log.i(TAG, "result is wrong, req code is " + requestCode);
    }

    /**
     * Stores the user address in shared preferences
     *
     * @param resultCode result code of the action
     * @param data intent data
     */
    private void onGetAddressResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            UserAddress userAddress = UserAddress.parseIntent(data);
            if (userAddress != null) {
                StringBuilder sb;
                sb = new StringBuilder();
                sb.append("<b>Name: </b>" + userAddress.getName() + "<br>");
                sb.append("<b>Address: </b>" + userAddress.getAddressLine1() + "" + userAddress.getAddressLine2() + "<br>");
                sb.append("<b>City: </b>" + userAddress.getLocality() + "<br>");
                sb.append("<b>State: </b>" + userAddress.getAdministrativeArea() + "<br>");
                sb.append("<b>Country: </b>" + userAddress.getCountryCode() + "<br>");
                sb.append("<b>Phone: </b>" + userAddress.getPhoneNumber());
                Utils.storeCountryName(MainActivity.this, userAddress.getCountryCode());
                Utils.storeUserAddress(MainActivity.this, sb.toString());
            } else {
                Utils.showToast(MainActivity.this, getString(R.string.msg_user_addr_failed));
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i(TAG, "User cancelled the address update");
            if (Utils.getUserAddress(MainActivity.this) != null && !Utils.getUserAddress(MainActivity.this).isEmpty()) {
                AppPreferences.setFirstTimeLogin(true);
                isFirstTimeLogin();
            }
        } else {
            Log.i(TAG, "result is wrong, result code is " + resultCode);
            Utils.showToast(MainActivity.this, getString(R.string.msg_user_addr_failed));
        }
    }

    /**
     * First time user login dialog
     * 1. If yes navigates to Huawei Identity module
     * 2. Fetches address details
     */
    private void isFirstTimeLogin() {
        showDialogCount = 1;
        if (AppPreferences.isFirstTimeLogin()) {
            AppPreferences.setFirstTimeLogin(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.address_title);
            builder.setMessage(R.string.choose_address);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton(R.string.select_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getUserAddress();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    /**
     * Gets the user address from identity kit.
     */
    private void getUserAddress() {
        UserAddressRequest req = new UserAddressRequest();
        Task<GetUserAddressResult> task = Address.getAddressClient(this).getUserAddress(req);
        task.addOnSuccessListener(result -> {
            Log.i(TAG, "onSuccess result code:" + result.getReturnCode());
            try {
                startActivityForResult(result);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }).addOnFailureListener(e -> {
            Log.i(TAG, "on Failed result code:" + e.getMessage());
            if (e instanceof ApiException) {
                switch (((ApiException) e).getStatusCode()) {
                    case AppConstants.USER_COUNTRY_NOT_SUPPORTED:
                        Utils.showToast(MainActivity.this, getString(R.string.country_not_supported_identity));
                        break;
                    case AppConstants.CHILDREN_ACC_NOT_SUPPORTED:
                        Utils.showToast(MainActivity.this, getString(R.string.child_account_not_supported_identity));
                        break;
                    default:
                        Utils.showToast(MainActivity.this, getString(R.string.msg_data_failed));
                }
            } else {
                Utils.showToast(MainActivity.this, getString(R.string.msg_unknown_error));
            }
        });
    }

    /**
     * To fetch User address result
     *
     * @param result result of user address
     */
    private void startActivityForResult(GetUserAddressResult result) throws IntentSender.SendIntentException {
        Status status = result.getStatus();
        if (result.getReturnCode() == 0 && status.hasResolution()) {
            Log.i(TAG, "the result had resolution.");
            status.startResolutionForResult(this, AppConstants.LOGIN_GET_ADDRESS_REQUESTCODE);
        } else {
            Log.i(TAG, "the response is wrong, the return code is " + result.getReturnCode());
        }
    }

}
