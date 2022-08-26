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

package com.huawei.hms.urbanhomeservices.kotlin.utils

/**
 * It's a App constants class
 *
 *  @author: Huawei
 *  @since : 20-01-2021
 */

object AppConstants {
    const val SERVICE_PROVIDER_TYPE = "ServiceProviderType"
    const val CATEGORY_NAME = "catName"
    const val PROVIDER_MAIL_ID = "emailId"
    const val PROVIDER_PH_NUM = "phoneNumber"
    const val PROVIDER_NAME = "serviceproviderName"
    const val PROVIDER_ID = "id"
    const val PROVIDER_COUNTRY = "country"
    const val PROVIDER_STATE = "state"
    const val PROVIDER_CITY = "city"
    const val COUNTRY_DATA = "Countries"
    const val COUNTRY_NAME = "CountryName"
    const val STATE_NAME = "StateName"
    const val STATE_DATA = "States"
    const val CITY_DATA = "Cities"
    const val INTIAL_VALUE = 0
    const val LOGIN_AUTH_CODE = 1002
    const val LOGIN_FACEBOOK_RESULTCODE = 64206
    const val LOGIN_EMAIL_SCOPE = "email"
    const val LOGIN_FACEBOOK_PROFILE = "public_profile"
    const val LOGIN_GET_ADDRESS_REQUESTCODE = 1000
    const val LOGIN_USER_TYPE = "type"
    const val LOGIN_CONSUMER_TYPE = "ConsumerType"
    const val LOGIN_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION"
    const val LOGIN_FACEBOOK_FIELDS_KEY = "fields"
    const val FACEBOOK_FIELD_FIRST_NAME = "first_name"
    const val FACEBOOK_FIELD_LAST_NAME = "last_name"
    const val FACEBOOK_FIELD_EMAIL = "email"
    const val FACEBOOK_FIELD_ID = "id"
    const val USER_NAME_KEY = "user_name"
    const val SERVICE_TYPE = "serviceType"
    const val REQUEST_QUERY = "requestQuery"
    const val SERVICE_PH_URI = "tel:"
    const val SERVICE_LAT_KEY = "latitude"
    const val SERVICE_LNG_KEY = "longtitue"
    const val SERVICE_STORE_NAME_KEY = "storeName"
    const val SERVICE_ADDR_KEY = "address"
    const val SERVICE_DRAWABLE_KEY = "drawable"
    const val SERVICE_NAME_KEY = "serviceName"
    const val PROVIDER_NAME_KEY = "providerName"
    const val PROVIDER_EMAIL_KEY = "serviceProviderEmail"
    const val PROVIDER_SHOP_NAME_KEY = "serviceProviderShopName"
    const val PROVIDER_PH_NUM_KEY = "serviceProviderMo"
    const val PROVIDER_IMAGE_KEY = "serviceImg"
    const val PROVIDER_SUB_VALUE = "subject"
    const val PROVIDER_MSG_TYPE = "message/rfc822"
    const val SERVICE_TYPE_PLUMBER = "Plumber"
    const val SERVICE_TYPE_ELECTRICIAN = "Electrician"
    const val SERVICE_TYPE_HOUSEKEEPER = "Housekeeper"
    const val SERVICE_TYPE_PAINTER = "Painter"
    const val SERVICE_TYPE_CARPENTER = "Carpenter"
    const val SERVICE_TYPE_APPLIANCE_REPAIR = "ApplianceRepair"
    const val STRING_FORMATTER_DISTANCE = "%.2f"
    const val DISTANCE_CONVERT_KM = 1000
    const val SEARCH_NAME_KEY = "name"
    const val PLUMBE = "Plumbe"
    const val ELECTRICAL = "Electrical"
    const val CLEANER = "cleaner"

    //Add your API Key from your AGC Console > My Projects > Project Settings
    const val API_KEY = "please enter api_key"
    const val DRIVE_PLAN_KEY = "https://mapapi.cloud.huawei.com/mapApi/v1/routeService/driving"
    const val DEFAULT_LAT_LNG_VALUE = 0.0
    const val INITIAL_VALUE_ONE = 1
    const val INITIAL_VALUE_TWO = 2
    const val SHADOW_FLG = "shadow_flag"
    const val URBAN_HOME_SERVICES = "UrbanHomeServices"
    const val LOCATION_REQ_INTERVAL = 10000L
    const val LOCATION_FASTEST_INTERVAL = 5000L
    const val LATITUDE_KEY = "lat"
    const val LONGITUDE_KEY = "lng"
    const val ORIGIN_LOC_KEY = "origin"
    const val DESTINATION_LOC_KEY = "destination"
    const val ERROR_MSG_KEY = "errorMsg"
    const val ROUTES_KEY = "routes"
    const val BOUNDS_KEY = "bounds"
    const val BOUNDS_NORTHEAST_KEY = "northeast"
    const val BOUNDS_SOUTHWEST_KEY = "southwest"
    const val PATHS_KEY = "paths"
    const val STEPS_KEY = "steps"
    const val POLYLINE_KEY = "polyline"
    const val USER_COUNTRY_NOT_SUPPORTED = 60054
    const val CHILDREN_ACC_NOT_SUPPORTED = 60055
    const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    const val MAX_TIMES = 10
    const val ENCODE_FORMAT = "utf-8"
    const val CHOOSE_SERVICE = "Choose Service"
    const val COUNTRY_STR = "country"
    const val CAT_NAME = "cat_name"
    const val NEW_LINE = "\n"
}
