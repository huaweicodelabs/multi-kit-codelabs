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

package com.huawei.hms.urbanhomeservices.java.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * This model call is used to fetch ServiceCategory list from Cloud DB.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */
public class ServiceDataType implements Parcelable {

    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("servicetype")
    @Expose
    private List<ServiceType> serviceType = new ArrayList<>();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<ServiceType> getServiceType() {
        return serviceType;
    }

    public void setServiceType(List<ServiceType> serviceType) {
        this.serviceType = serviceType;
    }

    protected ServiceDataType(Parcel in) {
        version = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(version);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Creator<ServiceDataType> CREATOR = new Creator<ServiceDataType>() {
        @Override
        public ServiceDataType createFromParcel(Parcel in) {
            return new ServiceDataType(in);
        }

        @Override
        public ServiceDataType[] newArray(int size) {
            return new ServiceDataType[size];
        }
    };


}
