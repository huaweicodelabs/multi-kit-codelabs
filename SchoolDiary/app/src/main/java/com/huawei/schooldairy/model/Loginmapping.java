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

import java.util.Date;

/**
 * Definition of ObjectType Loginmapping.
 *
 * @since 2022-08-29
 */
@PrimaryKeys({"StudentID", "TeacherID", "TeacherEmail"})
public final class Loginmapping extends CloudDBZoneObject {
    private String StudentID;

    private String StudentName;

    private String StudentEmail;

    private String TeacherID;

    private String TeacherName;

    private String TeacherEmail;

    private Integer UserType;

    private Date MappedDate;

    public Loginmapping() {
        super(Loginmapping.class);
    }

    public void setStudentID(String StudentID) {
        this.StudentID = StudentID;
    }

    public String getStudentID() {
        return StudentID;
    }

    public void setStudentName(String StudentName) {
        this.StudentName = StudentName;
    }

    public String getStudentName() {
        return StudentName;
    }

    public void setStudentEmail(String StudentEmail) {
        this.StudentEmail = StudentEmail;
    }

    public String getStudentEmail() {
        return StudentEmail;
    }

    public void setTeacherID(String TeacherID) {
        this.TeacherID = TeacherID;
    }

    public String getTeacherID() {
        return TeacherID;
    }

    public void setTeacherName(String TeacherName) {
        this.TeacherName = TeacherName;
    }

    public String getTeacherName() {
        return TeacherName;
    }

    public void setTeacherEmail(String TeacherEmail) {
        this.TeacherEmail = TeacherEmail;
    }

    public String getTeacherEmail() {
        return TeacherEmail;
    }

    public void setUserType(Integer UserType) {
        this.UserType = UserType;
    }

    public Integer getUserType() {
        return UserType;
    }

    public void setMappedDate(Date MappedDate) {
        this.MappedDate = MappedDate;
    }

    public Date getMappedDate() {
        return MappedDate;
    }

}
