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
 * Definition of ObjectType User.
 *
 * @since 2021-11-26
 */
@PrimaryKeys({"user_email"})
@Indexes({"user_id_email:user_email,user_id", "user_email:user_email"})
public final class User extends CloudDBZoneObject {
    private Integer user_id;

    private String user_email;

    private String user_name;

    private String user_profile_pic;

    @DefaultValue(booleanValue = true)
    private Boolean user_shadow_flag;

    private String user_phone;

    public User() {
        super(User.class);
        this.user_shadow_flag = true;
    }

    public void setUserId(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getUserId() {
        return user_id;
    }

    public void setUserEmail(String user_email) {
        this.user_email = user_email;
    }

    public String getUserEmail() {
        return user_email;
    }

    public void setUserName(String user_name) {
        this.user_name = user_name;
    }

    public String getUserName() {
        return user_name;
    }

    public void setUserProfilePic(String user_profile_pic) {
        this.user_profile_pic = user_profile_pic;
    }

    public String getUserProfilePic() {
        return user_profile_pic;
    }

    public void setUserShadowFlag(Boolean user_shadow_flag) {
        this.user_shadow_flag = user_shadow_flag;
    }

    public Boolean getUserShadowFlag() {
        return user_shadow_flag;
    }

    public void setUserPhone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getUserPhone() {
        return user_phone;
    }

}
