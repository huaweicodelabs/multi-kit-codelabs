/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */

package com.huawei.hms.urbanhomeservices.kotlin.clouddb;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

/**
 * Definition of ObjectType ServiceCategory.
 *
 * @author : Huawei
 * @since : 2021-02-08
 */

@PrimaryKeys({"service_id"})
public class ServiceCategory extends CloudDBZoneObject {
    private Integer service_id;
    private String service_name;
    private String service_category;
    @DefaultValue(booleanValue = true)
    private Boolean shadow_flag;
    private String image_name;

    public ServiceCategory() {
        super(ServiceCategory.class);
        this.shadow_flag = true;
    }

    public void setServiceId(Integer service_id) {
        this.service_id = service_id;
    }

    public Integer getServiceId() {
        return service_id;
    }

    public void setServiceName(String service_name) {
        this.service_name = service_name;
    }

    public String getServiceName() {
        return service_name;
    }

    public void setServiceCategory(String service_category) {
        this.service_category = service_category;
    }

    public String getServiceCategory() {
        return service_category;
    }

    public void setShadowFlag(Boolean shadow_flag) {
        this.shadow_flag = shadow_flag;
    }

    public Boolean getShadowFlag() {
        return shadow_flag;
    }

    public void setImageName(String image_name) {
        this.image_name = image_name;
    }

    public String getImageName() {
        return image_name;
    }

}
