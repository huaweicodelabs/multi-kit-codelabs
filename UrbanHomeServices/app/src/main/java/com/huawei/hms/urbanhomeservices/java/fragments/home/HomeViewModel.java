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

package com.huawei.hms.urbanhomeservices.java.fragments.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * Fetch location from Huawei HMS Location Kit using live data
 *
 * @author: Huawei
 * @since 20-01-21
 */

public class HomeViewModel extends AndroidViewModel {

    private final com.huawei.hms.urbanhomeservices.kotlin.fragments.home.LocationLiveData locationData;

    @NotNull
    public final com.huawei.hms.urbanhomeservices.kotlin.fragments.home.LocationLiveData getLocationData() {
        return this.locationData;
    }

    public HomeViewModel(Application application) {
        super(application);
        this.locationData = new com.huawei.hms.urbanhomeservices.kotlin.fragments.home.LocationLiveData(application);
    }

}