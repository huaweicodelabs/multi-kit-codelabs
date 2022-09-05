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
import com.huawei.agconnect.cloud.database.Text;

import java.util.Date;

/**
 * Definition of ObjectType TaskItem.
 *
 * @since 2022-08-29
 */
@PrimaryKeys({"TaskID"})
public final class TaskItem extends CloudDBZoneObject {
    private String TaskID;

    private String TaskName;

    private String TaskDescription;

    private Date CreatedDate;

    private Date DueDate;

    private String CreadtedBy;

    private String StudentID;

    private Integer Status;

    private Date SubmittedDate;

    private String group_id;

    private Text AttachmentUrl;

    public TaskItem() {
        super(TaskItem.class);
    }

    public void setTaskID(String TaskID) {
        this.TaskID = TaskID;
    }

    public String getTaskID() {
        return TaskID;
    }

    public void setTaskName(String TaskName) {
        this.TaskName = TaskName;
    }

    public String getTaskName() {
        return TaskName;
    }

    public void setTaskDescription(String TaskDescription) {
        this.TaskDescription = TaskDescription;
    }

    public String getTaskDescription() {
        return TaskDescription;
    }

    public void setCreatedDate(Date CreatedDate) {
        this.CreatedDate = CreatedDate;
    }

    public Date getCreatedDate() {
        return CreatedDate;
    }

    public void setDueDate(Date DueDate) {
        this.DueDate = DueDate;
    }

    public Date getDueDate() {
        return DueDate;
    }

    public void setCreadtedBy(String CreadtedBy) {
        this.CreadtedBy = CreadtedBy;
    }

    public String getCreadtedBy() {
        return CreadtedBy;
    }

    public void setStudentID(String StudentID) {
        this.StudentID = StudentID;
    }

    public String getStudentID() {
        return StudentID;
    }

    public void setStatus(Integer Status) {
        this.Status = Status;
    }

    public Integer getStatus() {
        return Status;
    }

    public void setSubmittedDate(Date SubmittedDate) {
        this.SubmittedDate = SubmittedDate;
    }

    public Date getSubmittedDate() {
        return SubmittedDate;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setAttachmentUrl(Text AttachmentUrl) {
        this.AttachmentUrl = AttachmentUrl;
    }

    public Text getAttachmentUrl() {
        return AttachmentUrl;
    }

}
