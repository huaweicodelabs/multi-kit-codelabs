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

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.agconnect.auth.SignInResult;
import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.clouddb.CloudDBZoneWrapper;
import com.huawei.hms.urbanhomeservices.java.clouddb.LoginHelper;
import com.huawei.hms.urbanhomeservices.java.clouddb.ServiceType;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;
import com.huawei.hms.urbanhomeservices.java.utils.AppPreferences;
import com.huawei.hms.urbanhomeservices.java.utils.Utils;
import com.huawei.hms.urbanhomeservices.java.viewmodel.AddServiceViewModel;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.huawei.hms.urbanhomeservices.java.utils.Utils.getTimeStamp;

/**
 * Following operation are performed in this activity
 * 1: Select user details
 * 2: Validate user details
 * 3: Add the details to the CloudDb
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class AddServiceActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, CloudDBZoneWrapper.UiCallBack<ServiceType>, LoginHelper.OnLoginEventCallBack {

    public static final String TAG = AddServiceActivity.class.getSimpleName();
    private AddServiceViewModel addServiceViewModel;
    private ServiceType serviceType;
    private boolean isServiceEdited = false;
    private String countryName;
    private String cityName;
    private String stateName;
    private EditText etName;
    private EditText etPhone;
    private EditText etEmail;
    private Spinner countrySpinner;
    private Spinner stateSpinner;
    private Spinner citySpinner;
    private Spinner serviceTypeSpinner;
    private int id;
    private CloudDBZoneWrapper<ServiceType> mCloudDBZoneWrapper;
    private Button loginSericeProviderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_service_layout);
        etName = findViewById(R.id.etServiceName);
        etPhone = findViewById(R.id.etPhoneNum);
        etEmail = findViewById(R.id.etEmailId);
        countrySpinner = findViewById(R.id.selectCountrySpn);
        stateSpinner = findViewById(R.id.selectStateSpn);
        citySpinner = findViewById(R.id.selectCitySpn);
        countrySpinner.setOnItemSelectedListener(this);
        stateSpinner.setOnItemSelectedListener(this);
        citySpinner.setOnItemSelectedListener(this);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        loginSericeProviderBtn = findViewById(R.id.loginSericeProviderBtn);
        initViewModel();
        serviceType = new ServiceType();
        serviceTypeSpinner = findViewById(R.id.selectServiceSpinner);
        String[] serviceTypes = getResources().getStringArray(R.array.select_service);
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, serviceTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceTypeSpinner.setAdapter(adapter);
        Toolbar toolbar = findViewById(R.id.toolbar_add_service);
        setSupportActionBar(toolbar);
        serviceTypeSpinner.setOnItemSelectedListener(this);
        initCloudZone();
        countrySpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utils.hideKeyboard(AddServiceActivity.this);
                return false;
            }
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etName.getText().toString().isEmpty()) {
                    etName.setError(getString(R.string.enter_service_name));
                } else {
                    etName.setError(null);
                }
                enableDisableButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etPhone.getText().toString().isEmpty()) {
                    etPhone.setError(getString(R.string.enter_phone_num));
                } else {
                    etPhone.setError(null);
                }
                enableDisableButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etEmail.getText().toString().isEmpty()) {
                    etEmail.setError(getString(R.string.enter_email_id));
                } else {
                    etEmail.setError(null);
                }
                enableDisableButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        loginSericeProviderBtn.setOnClickListener(this);
        if (getIntent().hasExtra(AppConstants.CATEGORY_NAME)) {
            if (getIntent().getExtras() != null) {
                toolbarTitle.setText(getString(R.string.edit_service_title));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                isServiceEdited = true;
                etEmail.setText(getIntent().getStringExtra(AppConstants.PROVIDER_MAIL_ID));
                etPhone.setText(String.valueOf(getIntent().getLongExtra(AppConstants.PROVIDER_PH_NUM, 0)));
                etName.setText(getIntent().getStringExtra(AppConstants.PROVIDER_NAME));
                countryName = getIntent().getStringExtra(AppConstants.PROVIDER_COUNTRY);
                stateName = getIntent().getStringExtra(AppConstants.PROVIDER_STATE);
                cityName = getIntent().getStringExtra(AppConstants.PROVIDER_CITY);
                id = getIntent().getIntExtra(AppConstants.PROVIDER_ID, 0);
                for (int i = 0; i < serviceTypes.length; i++) {
                    if (getIntent().getStringExtra(AppConstants.CATEGORY_NAME).equals(serviceTypes[i])) {
                        serviceTypeSpinner.setSelection(i);
                    }
                }
            }
        } else {
            toolbarTitle.setText(getString(R.string.add_services));
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        try {
            populateCountries();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        String phoneText = etPhone.getText().toString();
        String emailText = etEmail.getText().toString();
        if (v.getId() == loginSericeProviderBtn.getId()) {
            if (serviceTypeSpinner.getSelectedItem().toString().equals(AppConstants.CHOOSE_SERVICE)) {
                ((TextView) serviceTypeSpinner.getSelectedView()).setError(getString(R.string.service_type_err_msg));
            } else if (TextUtils.isEmpty(etName.getText().toString())) {
                etName.setError(getString(R.string.enter_service_name));
            } else if (TextUtils.isEmpty(phoneText)) {
                etPhone.setError(getString(R.string.enter_phone_num));
            } else if (!isValidMobile(phoneText)) {
                etPhone.setError(getString(R.string.enter_valid_phone_num));
            } else if (TextUtils.isEmpty(emailText)) {
                etEmail.setError(getString(R.string.enter_email_id));
            } else if (!isValidEmail(emailText)) {
                etEmail.setError(getString(R.string.enter_valid_email_id));
            } else if (countrySpinner.getSelectedItem().toString().equals(getString(R.string.choose_country))) {
                ((TextView) countrySpinner.getSelectedView()).setError(getString(R.string.select_country));
            } else if (stateSpinner.getSelectedItem().toString().equals(getString(R.string.choose_state))) {
                ((TextView) stateSpinner.getSelectedView()).setError(getString(R.string.select_state));
            } else if (citySpinner.getSelectedItem().toString().equals(getString(R.string.choose_city))) {
                ((TextView) citySpinner.getSelectedView()).setError(getString(R.string.select_city));
            } else {
                processAddAction();
            }
        }
    }

    /**
     * Initialize View model
     */
    private void initViewModel() {
        addServiceViewModel = new ViewModelProvider(this).get(AddServiceViewModel.class);
    }

    /**
     * To enable or disable "Save button"
     */
    private void enableDisableButton() {
        if (etName.getText().toString().isEmpty()
                || etPhone.getText().toString().isEmpty()
                || etEmail.getText().toString().isEmpty()
                || serviceTypeSpinner.getSelectedItem().toString().equals(AppConstants.CHOOSE_SERVICE)
                || countrySpinner.getSelectedItem().toString().equals(getString(R.string.choose_country))
                || stateSpinner.getSelectedItem().toString().equals(getString(R.string.choose_state))
                || citySpinner.getSelectedItem().toString().equals(getString(R.string.choose_city))) {

            loginSericeProviderBtn.setBackground(getDrawable(R.drawable.rounded_corner_gray_btn));
        } else {
            loginSericeProviderBtn.setBackground(getDrawable(R.drawable.rounded_corner_green_btn));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View arg1, int position, long id) {
        parent.getItemAtPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }

    private void populateCountries() throws JSONException {
        Utils.hideKeyboard(this);
        addServiceViewModel.fetchCountryData();
        addServiceViewModel.countries.observe(this, list -> {
            ArrayAdapter countryListAdapter = null;
            int counteryIndex = 0;
            if (isServiceEdited) {
                counteryIndex = list.indexOf(countryName);
            }
            countryListAdapter = new ArrayAdapter(AddServiceActivity.this, android.R.layout.simple_spinner_dropdown_item, list);
            countrySpinner.setAdapter(countryListAdapter);
            countrySpinner.setPrompt(getString(R.string.choose_country));
            countrySpinner.setSelection(counteryIndex);
            countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position > 0) {
                        addServiceViewModel.fetchStatesData(position - 1);
                    } else {
                        addServiceViewModel.fetchStatesData(position);
                    }
                    addServiceViewModel.states.observe(AddServiceActivity.this, new Observer<ArrayList<String>>() {
                        @Override
                        public void onChanged(ArrayList<String> list) {
                            int stateIndex = 0;
                            if (isServiceEdited) {
                                stateIndex = list.indexOf(stateName);
                            }
                            ArrayAdapter<String> stateListAdapter = new ArrayAdapter<String>(
                                    AddServiceActivity.this, android.R.layout.simple_spinner_dropdown_item, list);
                            stateSpinner.setAdapter(stateListAdapter);
                            stateSpinner.setPrompt(getString(R.string.choose_state));
                            stateSpinner.setSelection(stateIndex);
                            stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                    if (position > 0) {
                                        addServiceViewModel.fetchCitiesData(position - 1);
                                    } else {
                                        addServiceViewModel.fetchCitiesData(position);
                                    }
                                    addServiceViewModel.cities.observe(AddServiceActivity.this, list1 -> {
                                        citySpinner.setVisibility(View.VISIBLE);
                                        int cityIndex = 0;
                                        if (isServiceEdited) {
                                            cityIndex = list1.indexOf(cityName);
                                        }
                                        ArrayAdapter<String> cityListAdapter = new ArrayAdapter<String>(
                                                AddServiceActivity.this, android.R.layout.simple_spinner_dropdown_item, list1);
                                        citySpinner.setAdapter(cityListAdapter);
                                        citySpinner.setSelection(cityIndex);
                                        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                enableDisableButton();
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {
                                            }
                                        });
                                    });
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                        }
                    });
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        });
    }

    /**
     * Init cloud db Zone
     * for fetching data from cloud
     */
    private void initCloudZone() {
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mCloudDBZoneWrapper.setCloudObject(serviceType);
    }

    @Override
    public void onAddOrQuery(List<ServiceType> dbZoneList) {
    }

    @Override
    public void onSubscribe(List<ServiceType> dbZoneList) {
    }

    @Override
    public void onDelete(List<ServiceType> dbZoneList) {
    }

    @Override
    public void updateUiOnError(String errorMessage) {
    }

    @Override
    public void onInitCloud() {
        mCloudDBZoneWrapper.insertDbZoneInfo(serviceType);
    }

    @Override
    public void onInsertSuccess(Integer cloudDBZoneResult) {
        if (isServiceEdited) {
            Utils.showToast(AddServiceActivity.this, getString(R.string.msg_data_updated));
            finish();
        } else {
            Utils.showToast(AddServiceActivity.this, getString(R.string.msg_data_updated));
            Intent intent = new Intent(AddServiceActivity.this, AddServiceActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onLogin(boolean showLoginUserInfo, SignInResult signInResult) {
        mCloudDBZoneWrapper.setmUiCallBack(AddServiceActivity.this);
        mCloudDBZoneWrapper.createObjectType();
        mCloudDBZoneWrapper.openCloudDBZoneV2();
    }

    @Override
    public void onLogOut(boolean showLoginUserInfo) {
        Log.w(TAG, "onLogout");
    }

    /**
     * Adds user details to CloudDB
     */
    private void processAddAction() {
        String name = etName.getText().toString();
        String phNo = PhoneNumberUtils.formatNumber(etPhone.getText().toString());
        String emailId = etEmail.getText().toString();
        LoginHelper loginHelper = new LoginHelper(AddServiceActivity.this);
        loginHelper.addLoginCallBack(AddServiceActivity.this);
        loginHelper.login();
        if (!isServiceEdited) {
            id = getTimeStamp();
        }
        serviceType.setCatName(serviceTypeSpinner.getSelectedItem().toString());
        serviceType.setId(id);
        serviceType.setUserName(AppPreferences.getUserName());
        serviceType.setCountry(countrySpinner.getSelectedItem().toString());
        serviceType.setState(stateSpinner.getSelectedItem().toString());
        serviceType.setCity(citySpinner.getSelectedItem().toString());
        serviceType.setEmailId(emailId);
        serviceType.setPhoneNumber(Long.parseLong(phNo));
        serviceType.setServiceProviderName(name);
    }

    public void onBackPressed() {
        Utils.logoutDialog(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.provider_menu, menu);
        return true;
    }

    /**
     * On select menu option
     * it will navigate to LogOut or Manage Services
     *
     * @param item menu item
     * @return boolean selection of menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.provider_item_logout:
                Utils.logoutDialog(this);
                return true;
            case R.id.manageService:
                Intent intent = new Intent(this, ManageServiceActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * To validate Email
     *
     * @param email user email
     * @return boolean check email is valid or not
     */
    private boolean isValidEmail(CharSequence email) {
        if (!TextUtils.isEmpty(email)) {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
        return false;
    }

    /**
     * To validate Module number
     *
     * @param phone  phone number
     * @return boolean validation of mobile phone
     */
    private boolean isValidMobile(String phone) {
        return Patterns.PHONE.matcher(phone).matches();
    }
}

