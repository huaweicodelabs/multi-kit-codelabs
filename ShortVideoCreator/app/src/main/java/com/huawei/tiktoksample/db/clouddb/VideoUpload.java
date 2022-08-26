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

package com.huawei.tiktoksample.db.clouddb;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.annotations.Indexes;
import com.huawei.agconnect.cloud.database.annotations.NotNull;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

/**
 * Definition of ObjectType VideoUpload.
 *
 * @since 2021-11-26
 */
@PrimaryKeys({"video_id"})
@Indexes({"video_id_user_phone:video_id,user_phone", "video_id_created:video_id,video_created_time", "video_id:video_id"})
public final class VideoUpload extends CloudDBZoneObject {
    private Long video_id;

    private String video_name;

    private Long video_duration;

    private Long video_created_time;

    @DefaultValue(intValue = 0)
    private Integer video_no_of_likes;

    @DefaultValue(intValue = 0)
    private Integer video_no_of_comments;

    private String user_phone;

    private String user_name;

    @NotNull
    @DefaultValue(stringValue = "null")
    private String user_email;

    private String user_profile_pic;

    @DefaultValue(booleanValue = true)
    private Boolean video_shadow_flag;

    @NotNull
    @DefaultValue(stringValue = "null")
    private String video_upload_link;

    public VideoUpload() {
        super(VideoUpload.class);
        this.video_no_of_likes = 0;
        this.video_no_of_comments = 0;
        this.user_email = "null";
        this.video_shadow_flag = true;
        this.video_upload_link = "null";
    }

    public void setVideoId(Long video_id) {
        this.video_id = video_id;
    }

    public Long getVideoId() {
        return video_id;
    }

    public void setVideoName(String video_name) {
        this.video_name = video_name;
    }

    public String getVideoName() {
        return video_name;
    }

    public void setVideoDuration(Long video_duration) {
        this.video_duration = video_duration;
    }

    public Long getVideoDuration() {
        return video_duration;
    }

    public void setVideoCreatedTime(Long video_created_time) {
        this.video_created_time = video_created_time;
    }

    public Long getVideoCreatedTime() {
        return video_created_time;
    }

    public void setVideoNoOfLikes(Integer video_no_of_likes) {
        this.video_no_of_likes = video_no_of_likes;
    }

    public Integer getVideoNoOfLikes() {
        return video_no_of_likes;
    }

    public void setVideoNoOfComments(Integer video_no_of_comments) {
        this.video_no_of_comments = video_no_of_comments;
    }

    public Integer getVideoNoOfComments() {
        return video_no_of_comments;
    }

    public void setUserPhone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getUserPhone() {
        return user_phone;
    }

    public void setUserName(String user_name) {
        this.user_name = user_name;
    }

    public String getUserName() {
        return user_name;
    }

    public void setUserEmail(String user_email) {
        this.user_email = user_email;
    }

    public String getUserEmail() {
        return user_email;
    }

    public void setUserProfilePic(String user_profile_pic) {
        this.user_profile_pic = user_profile_pic;
    }

    public String getUserProfilePic() {
        return user_profile_pic;
    }

    public void setVideoShadowFlag(Boolean video_shadow_flag) {
        this.video_shadow_flag = video_shadow_flag;
    }

    public Boolean getVideoShadowFlag() {
        return video_shadow_flag;
    }

    public void setVideoUploadLink(String video_upload_link) {
        this.video_upload_link = video_upload_link;
    }

    public String getVideoUploadLink() {
        return video_upload_link;
    }

}
