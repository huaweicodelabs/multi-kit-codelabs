package com.huawei.codelabs.splitbill.ui.main.models;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

import java.util.Date;

@PrimaryKeys({"id"})
public class Comments extends CloudDBZoneObject {
    protected Comments(Class<? extends CloudDBZoneObject> clazz) {
        super(clazz);
    }

    private Integer id;
    private String description;
    private Integer expense_id;
    private Integer user_id;
    private Date date;
    private String time;
    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getExpense_id() {
        return expense_id;
    }

    public void setExpense_id(Integer expense_id) {
        this.expense_id = expense_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
