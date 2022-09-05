/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
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
package com.huawei.schooldairy.model;

import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;
import com.huawei.agconnect.cloud.database.CloudDBZoneObject;

/**
 * Definition of ObjectType UserData.
 *
 * @since 2022-08-29
 */
@PrimaryKeys({"UserID", "UserType"})
public final class UserData extends CloudDBZoneObject {
    private String UserID;

    private String UserType;

    private String UserName;

    private String TeacherId;

    public UserData() {
        super(UserData.class);
    }

    public void setUserID(String UserID) {
        this.UserID = UserID;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserType(String UserType) {
        this.UserType = UserType;
    }

    public String getUserType() {
        return UserType;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public String getUserName() {
        return UserName;
    }

    public void setTeacherId(String TeacherId) {
        this.TeacherId = TeacherId;
    }

    public String getTeacherId() {
        return TeacherId;
    }

}
