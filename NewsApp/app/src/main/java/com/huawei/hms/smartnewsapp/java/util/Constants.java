package com.huawei.hms.smartnewsapp.java.util;

/*
 *
 *  * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

/**
 * Contants
 */
public class Constants {
    /**
     * For splash screen delay
     */
    public static final int DELAY_MILLIS = 3000;

    /**
     * URL to fetch news
     */
    public static final String BASE_URL = "https://";


    /**
     * country code for location news
     */
    public static final String COUNTRY_CODE = "in";

    /**
     * status value for news from server
     */
    public static final String STATUS_FAILED = "FAILED";

    /**
     * status value for news from server
     */
    public static final String STATUS_OK = "ok";

    /**
     * login request code
     */
    public static final int REQUEST_CODE = 200;

    /**
     * Shared preference name
     */
    public static final String MY_PREFS_NAME = "smartNews_pref";

    public static final int CONNECT_TIMEOUT = 10000;

    public static final int TIMEOUT = 10000;

    public static final int WRITE_TIMEOUT = 10000;

    public static final String GRANT_TYPE = "client_credentials";
    public static final String CLIENT_SECRET = "e65995697cb3925045ebc162aea194b37aadce0e0503552e3287df936f05643e";
    public static final String CLIENT_ID = "103207661";
}
