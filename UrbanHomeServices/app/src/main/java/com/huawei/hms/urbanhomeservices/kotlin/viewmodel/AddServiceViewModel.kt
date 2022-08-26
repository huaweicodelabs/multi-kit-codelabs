/*
 *
 *  * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.huawei.hms.urbanhomeservices.kotlin.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.huawei.hms.urbanhomeservices.R
import com.huawei.hms.urbanhomeservices.kotlin.utils.AppConstants
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

/**
 * To populate countries, states and cities into Spinners
 * Using Live data
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

class AddServiceViewModel(application: Application) : AndroidViewModel(application) {
    var context: Context = application
    var listOfCounty = MutableLiveData<MutableList<String?>>()
    var listOfstate = MutableLiveData<MutableList<String?>>()
    var listOfCity = MutableLiveData<MutableList<String?>>()
    private var jsonStateArray: JSONArray? = null
    private val jsonCountryArray: JSONArray =
            JSONObject(loadJSONFromAsset()).getJSONArray(AppConstants.COUNTRY_DATA)
    private val tag = "AddServiceViewModel"

    /**
     *  Fetch countries data from Json file
     */
    fun fetchCountryData() {
        val countryList: MutableList<String?> = mutableListOf()
        countryList.add(context.getString(R.string.choose_country))
        for (i in 0 until jsonCountryArray.length()) {
            countryList.add(jsonCountryArray.optJSONObject(i).optString(AppConstants.COUNTRY_NAME))
        }
        listOfCounty.value = countryList
    }

    /**
     * Fetch states data from Json file
     */
    fun fetchStateData(position: Int) {
        val stateList: MutableList<String?> = mutableListOf()
        stateList.add(context.getString(R.string.choose_state))
        jsonStateArray =
                jsonCountryArray.optJSONObject((position)).optJSONArray(AppConstants.STATE_DATA)
        jsonStateArray?.let {
            for (i in 0 until it.length()) {
                stateList.add(
                        it.optJSONObject(i)?.optString(AppConstants.STATE_NAME)
                )
            }
            listOfstate.value = stateList
        }
    }

    /**
     * Fetch cities data from Json file
     */
    fun fetchCityData(position: Int) {
        listOfCity.value?.clear()
        val cityList: MutableList<String?> = mutableListOf()
        cityList.add(context.getString(R.string.choose_city))
        val jsonCityArray: JSONArray? = jsonStateArray?.optJSONObject((position))
                ?.optJSONArray(AppConstants.CITY_DATA)
        jsonCityArray?.let {
            for (i in 0 until jsonCityArray.length()) {
                cityList.add(jsonCityArray[i].toString())
                listOfCity.value = cityList
            }
        }
    }
    @Suppress("TooGenericExceptionCaught")
    /**
     * Load json file from Assets folder
     */
    private fun loadJSONFromAsset(): String {
        val json: String?
        try {
            val inputStream: InputStream = context.assets.open("countries.json")
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            Log.i(tag, "JSONException")
            return ""
        }
        return json
    }
}
