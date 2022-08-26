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

/**
 * This model call is used to fetch ServiceCategory list from Cloud DB.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */
public class ServiceCatList implements Parcelable {

    @SerializedName("resources")
    @Expose
    private ServiceDataType resources;
    @SerializedName("download_url")
    @Expose
    private String downloadUrl;
    @SerializedName("ser_country")
    @Expose
    private String serCountry;

    public ServiceDataType getResources() {
        return resources;
    }

    public void setResources(ServiceDataType resources) {
        this.resources = resources;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getSerCountry() {
        return serCountry;
    }

    public void setSerCountry(String serCountry) {
        this.serCountry = serCountry;
    }


    protected ServiceCatList(Parcel in) {
        resources = in.readParcelable(ServiceDataType.class.getClassLoader());
        downloadUrl = in.readString();
        serCountry = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(resources, flags);
        dest.writeString(downloadUrl);
        dest.writeString(serCountry);
    }

    public static final Creator<ServiceCatList> CREATOR = new Creator<ServiceCatList>() {
        @Override
        public ServiceCatList createFromParcel(Parcel in) {
            return new ServiceCatList(in);
        }

        @Override
        public ServiceCatList[] newArray(int size) {
            return new ServiceCatList[size];
        }
    };

}
