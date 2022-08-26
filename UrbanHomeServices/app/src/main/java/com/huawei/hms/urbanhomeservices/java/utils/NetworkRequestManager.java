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

import android.util.Log;

import com.huawei.hms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

/**
 * NetworkRequestManager
 *
 * @author: Huawei
 * @since : 20-01-2021
 */
public class NetworkRequestManager {

    private static final String TAG = "NetworkRequestManager";

    /**
     * get driving route planning
     *
     * @param latLng1  origin latitude and longitude
     * @param latLng2  destination latitude and longitude
     * @param listener network listener
     */
    public static void getDrivingRoutePlanningResult(final LatLng latLng1, final LatLng latLng2, final OnNetworkListener listener) {
        getDrivingRoutePlanningResult(latLng1, latLng2, listener, 0, false);
    }

    /**
     * get driving route
     *
     * @param latLng1    origin latitude and longitude
     * @param latLng2    destination latitude and longitude
     * @param listener   network listener
     * @param count      last number of retries
     * @param needEncode dose the api key need to be encoded
     */
    private static void getDrivingRoutePlanningResult(final LatLng latLng1, final LatLng latLng2, final OnNetworkListener listener, int count, final boolean needEncode) {
        final int curCount = ++count;
        Log.e(TAG, "current count: " + curCount);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response =
                        NetClient.getNetClient().getDrivingRoutePlanningResult(latLng1, latLng2, needEncode);
                if (null != response && null != response.body() && response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        if (null != listener) {
                            listener.requestSuccess(result);
                        }
                        return;
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }

                String returnCode = "";
                String returnDesc = "";
                boolean need = needEncode;

                try {
                    String result = response.body().string();
                    JSONObject jsonObject = new JSONObject(result);
                    returnCode = jsonObject.optString("returnCode");
                    returnDesc = jsonObject.optString("returnDesc");
                } catch (NullPointerException e) {
                    returnDesc = "Request Fail!";
                } catch (IOException e) {
                    returnDesc = "Request Fail!";
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }

                if (curCount >= AppConstants.MAX_TIMES) {
                    if (null != listener) {
                        listener.requestFail(returnDesc);
                    }
                    return;
                }

                if (returnCode.equals("6")) {
                    need = true;
                }
                getDrivingRoutePlanningResult(latLng1, latLng2, listener, curCount, need);
            }
        }).start();
    }

    public interface OnNetworkListener {
        void requestSuccess(String result);

        void requestFail(String errorMsg);
    }
}
