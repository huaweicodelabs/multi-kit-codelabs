package com.huawei.codelabs.splitbill.ui.main.models;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

@PrimaryKeys({"id"})
public class Bill extends CloudDBZoneObject {
    protected Bill(Class<? extends CloudDBZoneObject> clazz) {
        super(clazz);
    }

    private Integer id;    private String description;
    private Integer expense_id;
    private String attachment_url;
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

    public String getAttachment_url() {
        return attachment_url;
    }

    public void setAttachment_url(String attachment_url) {
        this.attachment_url = attachment_url;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
