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

package com.huawei.hms.urbanhomeservices.kotlin.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * This model call is used to fetch ServiceCategory list from Cloud DB.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */
@Parcelize
data class ServiceType(
        @SerializedName("cat_name")
        @Expose
        var catName: String,
        @SerializedName("id")
        @Expose
        var id: String,
        @SerializedName("name")
        @Expose
        var name: String,
        @SerializedName("image_name")
        @Expose
        var imageName: String
) : Parcelable
