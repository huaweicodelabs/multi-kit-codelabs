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

package com.huawei.hms.urbanhomeservices.java.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.huawei.hms.urbanhomeservices.R;
import com.huawei.hms.urbanhomeservices.java.utils.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * To populate countries, states and cities into Spinners
 * Using Live data
 *
 * @author: Huawei
 * @since : 20-01-2021
 */
public class AddServiceViewModel extends AndroidViewModel {

    private Context context = getApplication().getApplicationContext();
    String jsonString;
    private JSONArray jsonArray;
    public MutableLiveData<ArrayList<String>> countries = new MutableLiveData<ArrayList<String>>();
    public MutableLiveData<ArrayList<String>> states = new MutableLiveData<ArrayList<String>>();
    public MutableLiveData<ArrayList<String>> cities = new MutableLiveData<ArrayList<String>>();
    private JSONArray jsonStateArray = null;
    private JSONArray jsonCountryArray;

    {
        try {
            jsonCountryArray = new JSONObject(loadJSONFromAsset(context)).getJSONArray(AppConstants.COUNTRY_DATA);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public AddServiceViewModel(@NonNull Application application) {
        super(application);
        context = application;
    }

    /**
     * Load json file from Assets folder
     *
     * @param context app context
     * @return String return JSON string
     */

    public String loadJSONFromAsset(Context context) {
        try {
            InputStream is = context.getAssets().open("countries.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return jsonString;
    }

    /**
     * Fetch countries data from Json file
     *
     * @throws json exception
     */
    public void fetchCountryData() throws JSONException {
        ArrayList<String> countryList = new ArrayList();
        countryList.add(context.getString(R.string.choose_country));
        for (int i = 0; i < jsonCountryArray.length(); i++) {
            countryList.add(jsonCountryArray.optJSONObject(i).optString(AppConstants.COUNTRY_NAME));
        }
        countries.postValue(countryList);
    }

    /**
     * Fetch states data from Json file
     *
     * @param position state
     */
    public void fetchStatesData(int position) {
        ArrayList<String> stateList = new ArrayList();
        stateList.add(context.getString(R.string.choose_state));
        jsonStateArray =
                jsonCountryArray.optJSONObject((position)).optJSONArray(AppConstants.STATE_DATA);
        if (jsonStateArray != null) {
            for (int i = 0; i < jsonStateArray.length(); i++) {
                stateList.add(jsonStateArray.optJSONObject(i).optString(AppConstants.STATE_NAME));
            }
        }
        states.postValue(stateList);
    }

    /**
     * Fetch cities data from Json file
     *
     * @param position cities
     */
    public void fetchCitiesData(int position) {
        ArrayList<String> cityList = new ArrayList();
        cityList.add(context.getString(R.string.choose_city));
        JSONArray jsonCityArray =
                jsonStateArray.optJSONObject((position)).optJSONArray(AppConstants.CITY_DATA);
        if (jsonCityArray != null) {
            for (int i = 0; i < jsonCityArray.length(); i++) {
                try {
                    cityList.add(jsonCityArray.get(i).toString());
                    cities.postValue(cityList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
