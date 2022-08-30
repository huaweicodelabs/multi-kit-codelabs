package com.huawei.hms.smartnewsapp.java.ui.ViewModel;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class NewsResource<T> {
    @NonNull
    public final NewsStatus status;

    @Nullable
    public final T data;

    @Nullable
    public final String message;

    public NewsResource(@NonNull NewsStatus status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> NewsResource<T> success(@Nullable T data) {
        return new NewsResource<>(NewsStatus.SUCCESS, data, null);
    }

    public static <T> NewsResource<T> error(@NonNull String msg, @Nullable T data) {
        return new NewsResource<>(NewsStatus.ERROR, data, msg);
    }

    public enum NewsStatus {
        SUCCESS,
        ERROR
    }
}
