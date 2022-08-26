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

package com.huawei.hms.urbanhomeservices.java.utils;

import com.huawei.hms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Fetching root path
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class NetClient {
    private static final String TAG = NetClient.class.getSimpleName();
    private static final String DEFAULT_KEY = AppConstants.API_KEY;
    private static final String DRIVING_ROUTE_PLANNING_URL = AppConstants.DRIVE_PLAN_KEY;
    private static OkHttpClient client;
    private static NetClient netClient;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private NetClient() {
        client = initOkHttpClient();
    }

    public OkHttpClient initOkHttpClient() {
        if (client == null) {
            client = new OkHttpClient.Builder().readTimeout(AppConstants.LOCATION_REQ_INTERVAL, TimeUnit.MILLISECONDS)
                    // Set the read timeout.
                    .connectTimeout(AppConstants.LOCATION_REQ_INTERVAL, TimeUnit.MILLISECONDS)
                    // Set the connect timeout.
                    .build();
        }
        return client;
    }

    public static NetClient getNetClient() {
        if (netClient == null) {
            netClient = new NetClient();
        }
        return netClient;
    }

    /**
     * get driving route plan result
     *
     * @param latLng1    origin latitude and longitude
     * @param latLng2    destination latitude and longitude
     * @param needEncode dose the api key need to be encoded
     * @return response network api response
     *
     */
    public Response getDrivingRoutePlanningResult(LatLng latLng1, LatLng latLng2, boolean needEncode) {
        String key = DEFAULT_KEY;
        if (needEncode) {
            try {
                key = URLEncoder.encode(DEFAULT_KEY, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String url = DRIVING_ROUTE_PLANNING_URL + "?key=" + key;

        Response response = null;
        JSONObject origin = new JSONObject();
        JSONObject destination = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            origin.put("lat", latLng1.latitude);
            origin.put("lng", latLng1.longitude);

            destination.put("lat", latLng2.latitude);
            destination.put("lng", latLng2.longitude);

            json.put("origin", origin);
            json.put("destination", destination);

            RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));
            Request request = new Request.Builder().url(url).post(requestBody).build();
            response = getNetClient().initOkHttpClient().newCall(request).execute();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return response;
    }

}
