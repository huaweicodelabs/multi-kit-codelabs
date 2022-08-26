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

package com.huawei.tiktoksample.model;

import com.google.gson.annotations.SerializedName;

public class VideoModel {
    @SerializedName("id")
    public int id;
    @SerializedName("video_url")
    public String video_url;
    @SerializedName("video_name")
    public String video_name;
    @SerializedName("like_count")
    public String video_like;
    @SerializedName("comment_count")
    public String video_commentes;
    public int getId() {
        return id;
    }
    public String getVideoUrl() {
        return video_url;
    }
    public String getVideoName() {
        return video_name;
    }
    public String getVideoLike() {
        return video_like;
    }
    public String getVideoCommentes() {
        return video_commentes;
    }
}
