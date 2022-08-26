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
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

/**
 * Definition of ObjectType VideoComments.
 *
 * @since 2021-11-26
 */
@PrimaryKeys({"comment_id"})
@Indexes({"comment_id:comment_id", "comment_id_email:comment_id,user_email"})
public final class VideoComments extends CloudDBZoneObject {
    private Long comment_id;

    private String comment_text;

    private Long comment_create_time;

    private String user_email;

    private String user_profile_pic;

    private Long video_id;

    @DefaultValue(booleanValue = true)
    private Boolean comment_shadow_flag;

    public VideoComments() {
        super(VideoComments.class);
        this.comment_shadow_flag = true;
    }

    public void setCommentId(Long comment_id) {
        this.comment_id = comment_id;
    }

    public Long getCommentId() {
        return comment_id;
    }

    public void setCommentText(String comment_text) {
        this.comment_text = comment_text;
    }

    public String getCommentText() {
        return comment_text;
    }

    public void setCommentCreateTime(Long comment_create_time) {
        this.comment_create_time = comment_create_time;
    }

    public Long getCommentCreateTime() {
        return comment_create_time;
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

    public void setVideoId(Long video_id) {
        this.video_id = video_id;
    }

    public Long getVideoId() {
        return video_id;
    }

    public void setCommentShadowFlag(Boolean comment_shadow_flag) {
        this.comment_shadow_flag = comment_shadow_flag;
    }

    public Boolean getCommentShadowFlag() {
        return comment_shadow_flag;
    }
}
