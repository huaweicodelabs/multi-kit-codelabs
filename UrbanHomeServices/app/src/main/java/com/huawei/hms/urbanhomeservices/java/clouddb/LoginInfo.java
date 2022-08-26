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

package com.huawei.hms.urbanhomeservices.java.clouddb;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

/**
 * Definition of ObjectType LoginInfo.
 *
 * @author : Huawei
 * @since : 2021-02-08
 */

@PrimaryKeys({"user_email"})
public class LoginInfo extends CloudDBZoneObject {
    public Integer user_id;
    public String user_email;
    public String user_name;
    public String user_phone;
    public String photo_uri;
    public String device_token;

    @DefaultValue(booleanValue = true)
    private Boolean shadow_flag;

    public LoginInfo() {
        super(LoginInfo.class);
        this.shadow_flag = true;
    }

    public void setUserId(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getUserId(int userIdRandomRange) {
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

    public void setUserPhone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getUserPhone() {
        return user_phone;
    }

    public void setPhotoUri(String photo_uri) {
        this.photo_uri = photo_uri;
    }

    public String getPhotoUri(String s) {
        return photo_uri;
    }

    public void setDeviceToken(String device_token) {
        this.device_token = device_token;
    }

    public String getDeviceToken() {
        return device_token;
    }

    public void setShadowFlag(Boolean shadow_flag) {
        this.shadow_flag = shadow_flag;
    }

    public Boolean getShadowFlag() {
        return shadow_flag;
    }

}
